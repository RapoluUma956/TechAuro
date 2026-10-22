import DonutChartCard from "./DonutChartCard";

/**
 * License Expiry Donut Card
 * Uses the common DonutChartCard component.
 */
export default function LicenseExpiryDonutCard({ data, ...rest }) {
  return <DonutChartCard data={data} {...rest} />;
}
