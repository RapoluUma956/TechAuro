package org.example.agent.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.agent.model.AssetReport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AgentSender {

    private static final String SERVER_URL =
            "http://192.168.140.128:8080/api/agent/report";


    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;


    public AgentSender() {

        httpClient =
                HttpClient.newHttpClient();


        objectMapper =
                new ObjectMapper();


        JavaTimeModule javaTimeModule =
                new JavaTimeModule();


        objectMapper.registerModule(
                javaTimeModule
        );


        objectMapper.disable(
                SerializationFeature
                        .WRITE_DATES_AS_TIMESTAMPS
        );
    }


    public void sendReport(
            AssetReport report) {

        try {

            System.out.println();
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "       SENDING REPORT TO SPRING BOOT"
            );

            System.out.println(
                    "=============================================="
            );


            String json =
                    objectMapper
                            .writeValueAsString(
                                    report
                            );


            System.out.println();
            System.out.println(
                    "Report JSON size: "
                            + json.length()
                            + " characters"
            );


            /*
             * Debug identity before sending.
             */

            System.out.println();
            System.out.println(
                    "Identity inside JSON:"
            );


            System.out.println(
                    "Asset Tag     : "
                            + report.getAssetTag()
            );


            System.out.println(
                    "Serial Number : "
                            + report.getSerialNumber()
            );


            System.out.println(
                    "Brand         : "
                            + report.getBrand()
            );


            System.out.println(
                    "Model         : "
                            + report.getModel()
            );


            HttpRequest request =
                    HttpRequest
                            .newBuilder()
                            .uri(
                                    URI.create(
                                            SERVER_URL
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .ofString(
                                                    json
                                            )
                            )
                            .build();


            System.out.println();
            System.out.println(
                    "Sending request to:"
            );


            System.out.println(
                    SERVER_URL
            );


            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    );


            System.out.println();
            System.out.println(
                    "Spring Boot Response Status: "
                            + response.statusCode()
            );


            System.out.println();
            System.out.println(
                    "Spring Boot Response Body:"
            );


            System.out.println(
                    response.body()
            );


            if (response.statusCode() >= 200 &&
                    response.statusCode() < 300) {


                System.out.println();
                System.out.println(
                        "REPORT SENT SUCCESSFULLY"
                );

            } else {

                System.out.println();
                System.out.println(
                        "REPORT SENDING FAILED"
                );
            }


            System.out.println(
                    "=============================================="
            );


        } catch (IOException e) {

            System.out.println();
            System.out.println(
                    "ERROR SENDING REPORT"
            );


            System.out.println(
                    "Message: "
                            + e.getMessage()
            );


            e.printStackTrace();


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();


            System.out.println();
            System.out.println(
                    "REPORT SENDING INTERRUPTED"
            );


            System.out.println(
                    "Message: "
                            + e.getMessage()
            );
        }
    }
}