package org.example.agent.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.agent.config.AgentConfig;
import org.example.agent.model.AssetReport;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

/**
 * Handles serializing and transmitting device telemetry reports to the central backend.
 * Uses centralized AgentConfig for cloud Render URLs, custom endpoints, and resilient timeout handling.
 */
public class AgentSender {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AgentSender() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(AgentConfig.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Submits an AssetReport payload to the configured cloud backend.
     * Retries target URLs and logs diagnostic information for IT administrators.
     *
     * @param report Collected telemetry report for this employee machine
     */
    public void sendReport(AssetReport report) {
        if (report == null) {
            System.err.println("[AgentSender] Cannot send report: report object is null");
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(report);

            System.out.println();
            System.out.println("========== REPORT SUBMISSION ==========");
            System.out.println("HTTP Method  : POST");
            System.out.println("Content-Type : application/json");
            System.out.println("JSON size    : " + json.length() + " characters");
            System.out.println("Asset Tag    : " + report.getAssetTag());
            System.out.println("Serial Number: " + report.getSerialNumber());
            System.out.println("Hostname     : " +
                    (report.getOs() == null ? "N/A" : report.getOs().getHostname()));

            List<String> targetUrls = AgentConfig.getReportTargetUrls();
            boolean delivered = false;

            for (String targetUrl : targetUrls) {
                System.out.println("Attempting connection to: " + targetUrl);
                long startTime = System.currentTimeMillis();

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(targetUrl))
                            .timeout(AgentConfig.getRequestTimeout())
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("User-Agent", "AssetMonitoringAgent/" + AgentConfig.AGENT_VERSION + " (Windows)")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    long elapsed = System.currentTimeMillis() - startTime;
                    int statusCode = response.statusCode();

                    System.out.println("Target       : " + targetUrl);
                    System.out.println("HTTP Status  : " + statusCode);
                    System.out.println("Time taken   : " + elapsed + " ms");
                    System.out.println("Response body: " + response.body());

                    if (statusCode >= 200 && statusCode < 300) {
                        System.out.println("RESULT: REPORT ACCEPTED (Status " + statusCode + ")");
                        delivered = true;
                        break;
                    } else if (statusCode == 502 || statusCode == 503 || statusCode == 504) {
                        System.err.println("RESULT: BACKEND WARMING UP (HTTP " + statusCode + ")");
                        System.err.println("Notice: Render free-tier instance is cold-starting. Next cycle will retry automatically.");
                    } else {
                        System.err.println("RESULT: REPORT REJECTED (HTTP " + statusCode + ")");
                        System.err.println("Response details: " + response.body());
                    }

                } catch (ConnectException e) {
                    System.err.println("Connection failed to " + targetUrl + ": " + e.getMessage());
                } catch (HttpConnectTimeoutException e) {
                    System.err.println("Connection timed out to " + targetUrl + ": " + e.getMessage());
                } catch (HttpTimeoutException e) {
                    System.err.println("Request timed out waiting for response from " + targetUrl + " ("
                            + AgentConfig.REQUEST_TIMEOUT_SECONDS + "s limit): " + e.getMessage());
                    System.err.println("Notice: Cloud backend may still be spinning up from sleep. The agent will retry on next cycle.");
                } catch (IOException e) {
                    System.err.println("I/O error communicating with " + targetUrl + ": " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Request interrupted during submission to " + targetUrl);
                    return;
                } catch (Exception e) {
                    System.err.println("Unexpected transmission error for " + targetUrl + ": " + e.getMessage());
                }
            }

            if (!delivered) {
                System.err.println("WARNING: Could not deliver report in this cycle to: " + targetUrls);
                System.err.println("The agent will automatically retry in " + (AgentConfig.MONITORING_INTERVAL_MS / 1000) + " seconds.");
            }

            System.out.println("=======================================");

        } catch (Exception e) {
            System.err.println("REPORT SERIALIZATION/UNEXPECTED ERROR");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
