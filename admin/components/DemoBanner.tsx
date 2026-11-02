export function DemoBanner() {
  return (
    <div className="rounded-xl border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900">
      <strong className="font-semibold">Demo mode.</strong> Supabase is not configured, so this
      dashboard shows fictional sample data. Actions are simulated and are not saved.
    </div>
  );
}
