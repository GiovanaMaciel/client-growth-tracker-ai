interface KpiCardProps {
  label: string;
  value: string;
  description?: string;
}

export default function KpiCard({ label, value, description }: KpiCardProps) {
  return (
    <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
      <p className="text-sm font-medium text-slate-500">{label}</p>

      <h3 className="text-2xl font-bold text-slate-900 mt-3">{value}</h3>

      {description && (
        <p className="text-xs text-slate-400 mt-2">{description}</p>
      )}
    </div>
  );
}
