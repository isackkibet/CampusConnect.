-- CampusConnect initial schema, RLS, triggers and seed data.
-- Run with: supabase db push
-- All seeded campus content is sample data and must not be presented as official.

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------------
-- Reference data
-- ---------------------------------------------------------------------------
create table if not exists public.institutions (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  code text not null unique,
  created_at timestamptz not null default now()
);

create table if not exists public.departments (
  id uuid primary key default gen_random_uuid(),
  institution_id uuid not null references public.institutions(id) on delete cascade,
  name text not null,
  created_at timestamptz not null default now(),
  unique (institution_id, name)
);

create table if not exists public.courses (
  id uuid primary key default gen_random_uuid(),
  department_id uuid not null references public.departments(id) on delete cascade,
  name text not null,
  created_at timestamptz not null default now(),
  unique (department_id, name)
);

-- ---------------------------------------------------------------------------
-- Profiles (1:1 with auth.users)
-- ---------------------------------------------------------------------------
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  email text not null default '',
  role text not null default 'student' check (role in ('student', 'moderator', 'admin')),
  institution_id uuid references public.institutions(id) on delete set null,
  institution_name text not null default '',
  department text not null default '',
  course text not null default '',
  year_of_study int not null default 1 check (year_of_study between 1 and 8),
  bio text not null default '' check (char_length(bio) <= 280),
  phone text not null default '',
  avatar_url text,
  verified boolean not null default false,
  suspended boolean not null default false,
  followers int not null default 0,
  following int not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create or replace function public.is_staff()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.profiles
    where id = auth.uid() and role in ('admin', 'moderator') and not suspended
  );
$$;

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.profiles
    where id = auth.uid() and role = 'admin' and not suspended
  );
$$;

-- Guard against privilege escalation from the client. Staff may change roles;
-- ordinary users may only edit their own descriptive fields.
create or replace function public.guard_profile_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() = old.id and not public.is_staff() then
    new.role := old.role;
    new.verified := old.verified;
    new.suspended := old.suspended;
    new.institution_id := old.institution_id;
    new.institution_name := old.institution_name;
    new.email := old.email;
    new.followers := old.followers;
    new.following := old.following;
  end if;
  new.updated_at := now();
  return new;
end;
$$;

drop trigger if exists profiles_guard_update on public.profiles;
create trigger profiles_guard_update
before update on public.profiles
for each row execute function public.guard_profile_update();

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, email, institution_id, institution_name)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'full_name', ''),
    coalesce(new.email, ''),
    case
      when (new.raw_user_meta_data->>'institution_id') ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
        then (new.raw_user_meta_data->>'institution_id')::uuid
      else null
    end,
    coalesce(new.raw_user_meta_data->>'institution_name', '')
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------------
-- Community
-- ---------------------------------------------------------------------------
create table if not exists public.groups (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  description text not null default '',
  institution_id uuid references public.institutions(id) on delete set null,
  created_by uuid references public.profiles(id) on delete set null,
  member_count int not null default 0,
  created_at timestamptz not null default now()
);

create table if not exists public.posts (
  id uuid primary key default gen_random_uuid(),
  author_id uuid not null references public.profiles(id) on delete cascade,
  author_name text not null default '',
  institution_id uuid references public.institutions(id) on delete set null,
  group_id uuid references public.groups(id) on delete set null,
  content text not null check (char_length(content) between 1 and 2000),
  image_url text,
  status text not null default 'published' check (status in ('published', 'pending', 'hidden', 'removed')),
  like_count int not null default 0,
  comment_count int not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.comments (
  id uuid primary key default gen_random_uuid(),
  post_id uuid not null references public.posts(id) on delete cascade,
  author_id uuid not null references public.profiles(id) on delete cascade,
  author_name text not null default '',
  content text not null check (char_length(content) between 1 and 1000),
  status text not null default 'published' check (status in ('published', 'hidden', 'removed')),
  created_at timestamptz not null default now()
);

create table if not exists public.post_likes (
  post_id uuid not null references public.posts(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (post_id, user_id)
);

create table if not exists public.group_members (
  group_id uuid not null references public.groups(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  member_role text not null default 'member' check (member_role in ('member', 'moderator', 'owner')),
  created_at timestamptz not null default now(),
  primary key (group_id, user_id)
);

create or replace function public.sync_post_like_count()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.posts p
  set like_count = (select count(*) from public.post_likes l where l.post_id = p.id)
  where p.id = coalesce(new.post_id, old.post_id);
  return null;
end;
$$;

drop trigger if exists post_likes_sync on public.post_likes;
create trigger post_likes_sync
after insert or delete on public.post_likes
for each row execute function public.sync_post_like_count();

create or replace function public.sync_comment_count()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.posts p
  set comment_count = (
    select count(*) from public.comments c
    where c.post_id = p.id and c.status = 'published'
  )
  where p.id = coalesce(new.post_id, old.post_id);
  return null;
end;
$$;

drop trigger if exists comments_sync on public.comments;
create trigger comments_sync
after insert or update or delete on public.comments
for each row execute function public.sync_comment_count();

-- ---------------------------------------------------------------------------
-- Marketplace
-- ---------------------------------------------------------------------------
create table if not exists public.listings (
  id uuid primary key default gen_random_uuid(),
  seller_id uuid not null references public.profiles(id) on delete cascade,
  seller_name text not null default '',
  title text not null check (char_length(title) between 4 and 120),
  description text not null check (char_length(description) between 10 and 1500),
  price_kes int not null check (price_kes > 0),
  category_id text not null default 'other',
  category text not null default 'Other',
  image_url text,
  location text not null default 'Campus',
  status text not null default 'pending_review'
    check (status in ('active', 'pending_review', 'sold', 'rejected')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- Opportunities
-- ---------------------------------------------------------------------------
create table if not exists public.opportunities (
  id uuid primary key default gen_random_uuid(),
  title text not null check (char_length(title) between 4 and 120),
  organization text not null default '',
  type text not null check (type in ('job', 'internship', 'attachment', 'gig')),
  description text not null check (char_length(description) >= 20),
  location text not null default 'Campus',
  deadline timestamptz not null,
  stipend text not null default '',
  remote boolean not null default false,
  eligibility text not null default 'Open to students',
  status text not null default 'published' check (status in ('published', 'draft', 'closed')),
  posted_by uuid references public.profiles(id) on delete set null,
  created_at timestamptz not null default now()
);

create table if not exists public.applications (
  id uuid primary key default gen_random_uuid(),
  opportunity_id uuid not null references public.opportunities(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  status text not null default 'Submitted',
  created_at timestamptz not null default now(),
  unique (opportunity_id, user_id)
);

-- ---------------------------------------------------------------------------
-- Accommodation and events
-- ---------------------------------------------------------------------------
create table if not exists public.accommodations (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text not null default '',
  rent_kes int not null check (rent_kes > 0),
  location text not null default '',
  distance_from_campus text not null default '',
  bedrooms int not null default 1 check (bedrooms between 1 and 20),
  image_url text,
  landlord_name text not null default '',
  landlord_phone text not null default '',
  available boolean not null default true,
  verified boolean not null default false,
  created_by uuid references public.profiles(id) on delete set null,
  created_at timestamptz not null default now()
);

create table if not exists public.events (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text not null default '',
  start_at timestamptz not null,
  venue text not null default '',
  organizer text not null default '',
  image_url text,
  institution_id uuid references public.institutions(id) on delete set null,
  rsvp_count int not null default 0,
  status text not null default 'published' check (status in ('published', 'draft', 'cancelled')),
  created_at timestamptz not null default now()
);

create table if not exists public.event_rsvps (
  event_id uuid not null references public.events(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  reminder boolean not null default false,
  created_at timestamptz not null default now(),
  primary key (event_id, user_id)
);

create or replace function public.sync_rsvp_count()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.events e
  set rsvp_count = (select count(*) from public.event_rsvps r where r.event_id = e.id)
  where e.id = coalesce(new.event_id, old.event_id);
  return null;
end;
$$;

drop trigger if exists event_rsvps_sync on public.event_rsvps;
create trigger event_rsvps_sync
after insert or delete on public.event_rsvps
for each row execute function public.sync_rsvp_count();

-- ---------------------------------------------------------------------------
-- Notifications, announcements, saved items, reports, devices, payments
-- ---------------------------------------------------------------------------
create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  title text not null,
  body text not null,
  type text not null default 'SYSTEM'
    check (type in ('ANNOUNCEMENT', 'MESSAGE', 'EVENT', 'OPPORTUNITY', 'MARKETPLACE', 'SYSTEM')),
  read boolean not null default false,
  target_id text,
  created_at timestamptz not null default now()
);

create table if not exists public.announcements (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text not null,
  audience text not null default 'all' check (audience in ('all', 'students', 'staff')),
  institution_id uuid references public.institutions(id) on delete set null,
  published_by uuid references public.profiles(id) on delete set null,
  published_at timestamptz not null default now(),
  status text not null default 'published' check (status in ('draft', 'published', 'archived'))
);

create table if not exists public.saved_items (
  user_id uuid not null references public.profiles(id) on delete cascade,
  content_type text not null check (content_type in ('listing', 'opportunity', 'accommodation', 'post')),
  content_id text not null,
  created_at timestamptz not null default now(),
  primary key (user_id, content_type, content_id)
);

create table if not exists public.reports (
  id uuid primary key default gen_random_uuid(),
  reporter_id uuid references public.profiles(id) on delete set null,
  content_type text not null check (content_type in ('post', 'comment', 'listing', 'accommodation', 'profile', 'message')),
  content_id text not null,
  reason text not null,
  details text not null default '',
  status text not null default 'open' check (status in ('open', 'resolved', 'dismissed')),
  created_at timestamptz not null default now(),
  resolved_at timestamptz,
  resolved_by uuid references public.profiles(id) on delete set null
);

create table if not exists public.device_tokens (
  user_id uuid not null references public.profiles(id) on delete cascade,
  token text not null,
  platform text not null default 'android',
  updated_at timestamptz not null default now(),
  primary key (user_id, token)
);

create table if not exists public.ai_usage (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now()
);

create index if not exists ai_usage_user_time_idx on public.ai_usage (user_id, created_at desc);

create table if not exists public.payments (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  reference text not null unique,
  amount_kes int not null check (amount_kes > 0),
  purpose text not null,
  status text not null default 'pending'
    check (status in ('pending', 'success', 'failed', 'cancelled')),
  provider_reference text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- Idempotent updated_at helper for tables that need it.
create or replace function public.touch_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

drop trigger if exists posts_touch on public.posts;
create trigger posts_touch before update on public.posts
for each row execute function public.touch_updated_at();

drop trigger if exists listings_touch on public.listings;
create trigger listings_touch before update on public.listings
for each row execute function public.touch_updated_at();

drop trigger if exists payments_touch on public.payments;
create trigger payments_touch before update on public.payments
for each row execute function public.touch_updated_at();

-- ---------------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------------
alter table public.institutions enable row level security;
alter table public.departments enable row level security;
alter table public.courses enable row level security;
alter table public.profiles enable row level security;
alter table public.groups enable row level security;
alter table public.posts enable row level security;
alter table public.comments enable row level security;
alter table public.post_likes enable row level security;
alter table public.group_members enable row level security;
alter table public.listings enable row level security;
alter table public.opportunities enable row level security;
alter table public.applications enable row level security;
alter table public.accommodations enable row level security;
alter table public.events enable row level security;
alter table public.event_rsvps enable row level security;
alter table public.notifications enable row level security;
alter table public.announcements enable row level security;
alter table public.saved_items enable row level security;
alter table public.reports enable row level security;
alter table public.device_tokens enable row level security;
alter table public.payments enable row level security;
alter table public.ai_usage enable row level security;

create policy "reference data is readable" on public.institutions for select using (true);
create policy "departments are readable" on public.departments for select using (true);
create policy "courses are readable" on public.courses for select using (true);

create policy "profiles readable to self and staff" on public.profiles
  for select using (auth.uid() = id or public.is_staff());
create policy "profiles update self or staff" on public.profiles
  for update using (auth.uid() = id or public.is_staff())
  with check (auth.uid() = id or public.is_staff());

create policy "groups readable by authenticated users" on public.groups
  for select to authenticated using (true);
create policy "staff manage groups" on public.groups
  for all using (public.is_staff()) with check (public.is_staff());
create policy "members read own membership" on public.group_members
  for select using (auth.uid() = user_id or public.is_staff());
create policy "members join and leave" on public.group_members
  for insert with check (auth.uid() = user_id);
create policy "members delete own membership" on public.group_members
  for delete using (auth.uid() = user_id or public.is_staff());

create policy "posts readable when published or own or staff" on public.posts
  for select using (status = 'published' or auth.uid() = author_id or public.is_staff());
create policy "users create own posts" on public.posts
  for insert with check (auth.uid() = author_id and status in ('published', 'pending'));
create policy "authors and staff update posts" on public.posts
  for update using (auth.uid() = author_id or public.is_staff())
  with check (auth.uid() = author_id or public.is_staff());
create policy "authors and staff delete posts" on public.posts
  for delete using (auth.uid() = author_id or public.is_staff());

create policy "comments readable with visible post" on public.comments
  for select using (
    status = 'published' and exists (
      select 1 from public.posts p where p.id = post_id and (p.status = 'published' or public.is_staff() or p.author_id = auth.uid())
    )
  );
create policy "users create own comments" on public.comments
  for insert with check (
    auth.uid() = author_id and exists (
      select 1 from public.posts p where p.id = post_id and p.status = 'published'
    )
  );
create policy "authors and staff update comments" on public.comments
  for update using (auth.uid() = author_id or public.is_staff())
  with check (auth.uid() = author_id or public.is_staff());
create policy "authors and staff delete comments" on public.comments
  for delete using (auth.uid() = author_id or public.is_staff());

create policy "likes readable" on public.post_likes for select using (true);
create policy "users like own row" on public.post_likes
  for insert with check (auth.uid() = user_id);
create policy "users unlike own row" on public.post_likes
  for delete using (auth.uid() = user_id);

create policy "listings readable when active sold or own or staff" on public.listings
  for select using (status in ('active', 'sold') or auth.uid() = seller_id or public.is_staff());
create policy "users create own listings" on public.listings
  for insert with check (auth.uid() = seller_id and status = 'pending_review');
create policy "sellers and staff update listings" on public.listings
  for update using (auth.uid() = seller_id or public.is_staff())
  with check (auth.uid() = seller_id or public.is_staff());
create policy "sellers and staff delete listings" on public.listings
  for delete using (auth.uid() = seller_id or public.is_staff());

create policy "published opportunities readable" on public.opportunities
  for select using (status = 'published' or public.is_staff());
create policy "staff manage opportunities" on public.opportunities
  for all using (public.is_staff()) with check (public.is_staff());

create policy "users read own applications or staff" on public.applications
  for select using (auth.uid() = user_id or public.is_staff());
create policy "users apply as self" on public.applications
  for insert with check (
    auth.uid() = user_id and exists (
      select 1 from public.opportunities o where o.id = opportunity_id and o.status = 'published' and o.deadline > now()
    )
  );
create policy "staff update applications" on public.applications
  for update using (public.is_staff()) with check (public.is_staff());

create policy "accommodation readable when available or staff" on public.accommodations
  for select using (available or public.is_staff());
create policy "users create accommodation" on public.accommodations
  for insert with check (auth.uid() = created_by and verified = false);
create policy "creators and staff update accommodation" on public.accommodations
  for update using (auth.uid() = created_by or public.is_staff())
  with check (auth.uid() = created_by or public.is_staff());

create policy "events readable" on public.events
  for select using (status = 'published' or public.is_staff());
create policy "staff manage events" on public.events
  for all using (public.is_staff()) with check (public.is_staff());
create policy "rsvps readable own or staff" on public.event_rsvps
  for select using (auth.uid() = user_id or public.is_staff());
create policy "users manage own rsvp" on public.event_rsvps
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "notifications readable by owner" on public.notifications
  for select using (auth.uid() = user_id or public.is_staff());
create policy "notifications update by owner" on public.notifications
  for update using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "published announcements readable" on public.announcements
  for select using (status = 'published' or public.is_staff());
create policy "staff manage announcements" on public.announcements
  for all using (public.is_staff()) with check (public.is_staff());

create policy "saved items owned" on public.saved_items
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "reports insert own" on public.reports
  for insert with check (auth.uid() = reporter_id);
create policy "reports readable by staff or reporter" on public.reports
  for select using (public.is_staff() or auth.uid() = reporter_id);
create policy "staff resolve reports" on public.reports
  for update using (public.is_staff()) with check (public.is_staff());

create policy "device tokens owned" on public.device_tokens
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "payments readable by owner or staff" on public.payments
  for select using (auth.uid() = user_id or public.is_staff());

create policy "ai usage readable by staff" on public.ai_usage
  for select using (public.is_staff());

-- ---------------------------------------------------------------------------
-- Storage buckets and policies
-- ---------------------------------------------------------------------------
insert into storage.buckets (id, name, public)
values
  ('avatars', 'avatars', true),
  ('posts', 'posts', true),
  ('listings', 'listings', true),
  ('accommodation', 'accommodation', true)
on conflict (id) do nothing;

create policy "public image read" on storage.objects
  for select using (bucket_id in ('avatars', 'posts', 'listings', 'accommodation'));

create policy "users upload own images" on storage.objects
  for insert to authenticated with check (
    bucket_id in ('avatars', 'posts', 'listings', 'accommodation')
    and (storage.foldername(name))[1] = auth.uid()::text
  );

create policy "users update own images" on storage.objects
  for update to authenticated using (
    bucket_id in ('avatars', 'posts', 'listings', 'accommodation')
    and (storage.foldername(name))[1] = auth.uid()::text
  );

create policy "users delete own images" on storage.objects
  for delete to authenticated using (
    bucket_id in ('avatars', 'posts', 'listings', 'accommodation')
    and (storage.foldername(name))[1] = auth.uid()::text
  );

-- ---------------------------------------------------------------------------
-- Column guards: authors/sellers may edit content but cannot grant themselves
-- moderation status, verification or ownership changes.
-- ---------------------------------------------------------------------------
create or replace function public.guard_post_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() = old.author_id and not public.is_staff() then
    new.status := old.status;
    new.author_id := old.author_id;
    new.author_name := old.author_name;
    new.institution_id := old.institution_id;
    new.like_count := old.like_count;
    new.comment_count := old.comment_count;
  end if;
  new.updated_at := now();
  return new;
end;
$$;

drop trigger if exists posts_guard_update on public.posts;
create trigger posts_guard_update before update on public.posts
for each row execute function public.guard_post_update();

create or replace function public.guard_comment_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() = old.author_id and not public.is_staff() then
    new.status := old.status;
    new.author_id := old.author_id;
    new.author_name := old.author_name;
    new.post_id := old.post_id;
  end if;
  return new;
end;
$$;

drop trigger if exists comments_guard_update on public.comments;
create trigger comments_guard_update before update on public.comments
for each row execute function public.guard_comment_update();

create or replace function public.guard_listing_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() = old.seller_id and not public.is_staff() then
    new.seller_id := old.seller_id;
    new.seller_name := old.seller_name;
    if not (old.status = 'active' and new.status = 'sold') then
      new.status := old.status;
    end if;
  end if;
  new.updated_at := now();
  return new;
end;
$$;

drop trigger if exists listings_guard_update on public.listings;
create trigger listings_guard_update before update on public.listings
for each row execute function public.guard_listing_update();

create or replace function public.guard_accommodation_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() = old.created_by and not public.is_staff() then
    new.verified := old.verified;
    new.created_by := old.created_by;
  end if;
  return new;
end;
$$;

drop trigger if exists accommodations_guard_update on public.accommodations;
create trigger accommodations_guard_update before update on public.accommodations
for each row execute function public.guard_accommodation_update();

-- ---------------------------------------------------------------------------
-- Sample reference data (clearly labelled as sample)
-- ---------------------------------------------------------------------------
insert into public.institutions (id, name, code)
values ('11111111-1111-4111-8111-111111111111'::text::uuid, 'Nairobi National Polytechnic (sample)', 'NNP-SAMPLE')
on conflict (code) do nothing;

insert into public.departments (institution_id, name)
values
  ('11111111-1111-4111-8111-111111111111'::uuid, 'Computing and Informatics (sample)'),
  ('11111111-1111-4111-8111-111111111111'::uuid, 'Business Studies (sample)'),
  ('11111111-1111-4111-8111-111111111111'::uuid, 'Engineering (sample)')
on conflict (institution_id, name) do nothing;
