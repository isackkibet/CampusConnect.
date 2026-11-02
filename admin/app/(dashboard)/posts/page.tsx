import { getPosts } from "@/lib/data";
import { formatDate, statusTone } from "@/lib/format";
import { ActionButtons, type AdminAction } from "@/components/ActionButtons";

export const dynamic = "force-dynamic";

export default async function PostsPage() {
  const posts = await getPosts();

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Community posts</h1>
        <p className="text-sm text-slate-500">
          Review reports, hide harmful posts and remove content that breaks community guidelines.
        </p>
      </div>

      <div className="space-y-3">
        {posts.map((post) => {
          const actions: AdminAction[] = [];
          if (post.status !== "published") {
            actions.push({
              label: "Publish",
              action: "moderate_post",
              payload: { status: "published" },
            });
          }
          if (post.status !== "hidden") {
            actions.push({
              label: "Hide",
              action: "moderate_post",
              payload: { status: "hidden" },
              tone: "neutral",
            });
          }
          actions.push({
            label: "Delete",
            action: "delete_post",
            tone: "danger",
            confirm: "Permanently delete this post and its comments?",
          });

          return (
            <article key={post.id} className="card p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-semibold text-slate-900">{post.author_name || "Unknown author"}</p>
                    <span className={`badge ${statusTone(post.status)}`}>{post.status}</span>
                    <span className="text-xs text-slate-500">{formatDate(post.created_at)}</span>
                  </div>
                  <p className="mt-2 whitespace-pre-wrap text-sm text-slate-700">{post.content}</p>
                  <p className="mt-2 text-xs text-slate-500">
                    {post.like_count} likes · {post.comment_count} comments
                  </p>
                </div>
                <ActionButtons id={post.id} actions={actions} />
              </div>
            </article>
          );
        })}
        {posts.length === 0 ? (
          <div className="card p-8 text-center text-sm text-slate-500">No posts found.</div>
        ) : null}
      </div>
    </div>
  );
}
