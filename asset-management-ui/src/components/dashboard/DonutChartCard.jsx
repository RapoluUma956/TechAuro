import React from "react";
import DonutChart from "../common/DonutChart";

/**
 * Dashboard DonutChartCard
 * 
 * Powered by the common reusable DonutChart component.
 */
export default function DonutChartCard(props) {
  return <DonutChart asCard={true} {...props} />;
}
