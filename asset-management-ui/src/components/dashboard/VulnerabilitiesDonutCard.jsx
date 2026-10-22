import DonutChartCard from "./DonutChartCard";

/**
 * Vulnerabilities Donut Card
 * Uses the common DonutChartCard component.
 */
export default function VulnerabilitiesDonutCard({ data, ...rest }) {
  return <DonutChartCard data={data} {...rest} />;
}
