export function formatDate(value: string | null | undefined): string {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return date.toLocaleString("en-KE", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formatKes(amount: number): string {
  return `KES ${amount.toLocaleString("en-KE")}`;
}

export function statusTone(status: string): string {
  switch (status) {
    case "active":
    case "published":
    case "resolved":
      return "badge-green";
    case "pending":
    case "pending_review":
    case "draft":
      return "badge-amber";
    case "rejected":
    case "removed":
    case "suspended":
      return "badge-red";
    default:
      return "badge-slate";
  }
}
