package org.example.agent.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.agent.model.AssetReport;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class AgentSender {

    private static final String DEFAULT_PRIMARY_URL = "http://localhost:8080/api/agent/report";
    private static final String DEFAULT_CURRENT_IP_URL = "http://192.168.254.135:8080/api/agent/report";
    private static final String DEFAULT_FALLBACK_URL = "http://192.168.140.128:8080/api/agent/report";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<String> serverUrls;

    public AgentSender() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.serverUrls = new ArrayList<>();
        String configuredUrl = System.getenv("AGENT_SERVER_URL");
        if (configuredUrl == null || configuredUrl.isBlank()) {
            configuredUrl = System.getProperty("agent.server.url");
        }

        if (configuredUrl != null && !configuredUrl.isBlank()) {
            serverUrls.add(configuredUrl.trim());
        }

        // Add localhost as default primary
        if (!serverUrls.contains(DEFAULT_PRIMARY_URL)) {
            serverUrls.add(DEFAULT_PRIMARY_URL);
        }

        // Add detected machine IPs
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    if (ni.isLoopback() || !ni.isUp()) continue;
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            String localIpUrl = "http://" + addr.getHostAddress() + ":8080/api/agent/report";
                            if (!serverUrls.contains(localIpUrl)) {
                                serverUrls.add(localIpUrl);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Add current WiFi IP & fallback
        if (!serverUrls.contains(DEFAULT_CURRENT_IP_URL)) {
            serverUrls.add(DEFAULT_CURRENT_IP_URL);
        }
        if (!serverUrls.contains(DEFAULT_FALLBACK_URL)) {
            serverUrls.add(DEFAULT_FALLBACK_URL);
        }
    }

    public void sendReport(AssetReport report) {
        try {
            String json = objectMapper.writeValueAsString(report);

            System.out.println("\n========== REPORT SUBMISSION ==========");
            System.out.println("HTTP Method: POST");
            System.out.println("Content-Type: application/json");
            System.out.println("JSON size: " + json.length() + " characters");
            System.out.println("Asset Tag: " + report.getAssetTag());
            System.out.println("Serial Number: " + report.getSerialNumber());
            System.out.println("Hostname: " +
                    (report.getOs() == null ? "N/A" : report.getOs().getHostname()));

            boolean delivered = false;

            for (String targetUrl : serverUrls) {
                System.out.println("Attempting connection to: " + targetUrl);
                long startTime = System.currentTimeMillis();

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(targetUrl))
                            .timeout(Duration.ofSeconds(60))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("Target: " + targetUrl);
                    System.out.println("HTTP Status: " + response.statusCode());
                    System.out.println("Time taken: " + elapsed + " ms");
                    System.out.println("Response body: " + response.body());

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        System.out.println("RESULT: REPORT ACCEPTED");
                        delivered = true;
                        break;
                    } else {
                        System.err.println("RESULT: REPORT REJECTED (HTTP " + response.statusCode() + ")");
                        System.err.println("Response details: " + response.body());
                    }

                } catch (ConnectException e) {
                    System.err.println("Connection failed to " + targetUrl + ": " + e.getMessage());
                    System.err.println("Trying next target URL if available...");
                } catch (IOException e) {
                    System.err.println("I/O error communicating with " + targetUrl + ": " + e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Request interrupted during submission to " + targetUrl);
                    return;
                }
            }

            if (!delivered) {
                System.err.println("WARNING: Could not deliver report to any configured server URL: " + serverUrls);
            }

            System.out.println("=======================================");

        } catch (Exception e) {
            System.err.println("REPORT SERIALIZATION/UNEXPECTED ERROR");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
