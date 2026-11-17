import { Link, useParams } from "react-router";

export default function DataEntryNavigation() {
  const { restaurantId, periodId } = useParams();

  if (!restaurantId || !periodId) {
    return null;
  }

  const baseUrl = `/restaurants/${restaurantId}/periods/${periodId}`;

  const links = [
    {
      label: "Dados financeiros",
      path: `${baseUrl}/data`,
    },
    {
      label: "Funil de vendas",
      path: `${baseUrl}/funnels`,
    },
    {
      label: "Tráfego pago",
      path: `${baseUrl}/paid-traffic`,
    },
    {
      label: "Dashboard",
      path: `${baseUrl}/dashboard`,
    },
  ];

  return (
    <nav
      aria-label="Navegação dos dados do período"
      className="my-6 flex flex-wrap gap-3"
    >
      {links.map((link) => (
        <Link
          key={link.path}
          to={link.path}
          className="rounded-lg border border-teal-200 bg-white px-4 py-2 text-sm font-medium text-teal-700 transition-colors hover:bg-teal-50"
        >
          {link.label}
        </Link>
      ))}
    </nav>
  );
}
