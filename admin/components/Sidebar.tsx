"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "/dashboard", label: "Overview" },
  { href: "/users", label: "Users" },
  { href: "/posts", label: "Community posts" },
  { href: "/marketplace", label: "Marketplace" },
  { href: "/opportunities", label: "Opportunities" },
  { href: "/reports", label: "Reports" },
  { href: "/announcements", label: "Announcements" },
];

export function Sidebar() {
  const pathname = usePathname();
  return (
    <aside className="hidden w-64 shrink-0 flex-col bg-navy-950 p-5 text-slate-300 lg:flex">
      <div className="mb-8">
        <p className="text-lg font-bold tracking-tight text-white">CampusConnect</p>
        <p className="text-xs text-slate-400">Admin console</p>
      </div>
      <nav className="space-y-1">
        {links.map((link) => {
          const active = pathname === link.href;
          return (
            <Link
              key={link.href}
              href={link.href}
              className={`nav-link ${active ? "nav-link-active" : ""}`}
            >
              <span>{link.label}</span>
              {active ? <span className="h-1.5 w-1.5 rounded-full bg-teal-500" /> : null}
            </Link>
          );
        })}
      </nav>
      <div className="mt-auto rounded-xl bg-white/5 p-3 text-xs leading-relaxed text-slate-400">
        All seeded campus content is sample data. Confirm official information with the institution.
      </div>
    </aside>
  );
}
