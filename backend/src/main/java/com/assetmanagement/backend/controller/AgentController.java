package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.service.*;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.assetmanagement.backend.dto.AgentReportRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/report")
    public ResponseEntity<String> receiveReport(
            @RequestBody AgentReportRequest request) {

        long startTime = System.currentTimeMillis();

        System.out.println(
                "\n=============================================="
        );
        System.out.println("          AGENT REPORT RECEIVED");
        System.out.println("==============================================");

        System.out.println(
                "Server Time   : "
                        + java.time.LocalDateTime.now()
        );

        System.out.println(
                "Serial Number : "
                        + request.getSerialNumber()
        );

        System.out.println(
                "Asset Tag     : "
                        + request.getAssetTag()
        );

        try {
            agentService.receiveReport(request);

            long duration =
                    System.currentTimeMillis() - startTime;

            System.out.println(
                    "\n=============================================="
            );
            System.out.println("          AGENT REPORT PROCESSED");
            System.out.println("==============================================");

            System.out.println(
                    "Serial Number : "
                            + request.getSerialNumber()
            );

            System.out.println(
                    "Processing Time: "
                            + duration + " ms"
            );

            System.out.println(
                    "Completed At  : "
                            + java.time.LocalDateTime.now()
            );

            System.out.println(
                    "==============================================\n"
            );

            return ResponseEntity.ok(
                    "Agent report received and processed successfully"
            );

        } catch (Exception e) {
            System.err.println(
                    "\n=============================================="
            );
            System.err.println("       AGENT REPORT PROCESSING FAILED");
            System.err.println("==============================================");

            System.err.println(
                    "Serial Number : "
                            + request.getSerialNumber()
            );

            System.err.println(
                    "Failed At     : "
                            + java.time.LocalDateTime.now()
            );

            System.err.println(
                    "Error         : "
                            + e.getMessage()
            );

            e.printStackTrace();

            throw e;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(
            Exception e) {

        String message = e.getMessage();

        if (e instanceof JsonMappingException mappingException) {

            String path = mappingException
                    .getPath()
                    .stream()
                    .map(reference -> {
                        if (reference.getFieldName() != null) {
                            return reference.getFieldName();
                        }

                        return "[" + reference.getIndex() + "]";
                    })
                    .reduce(
                            (first, second) ->
                                    first + " -> " + second
                    )
                    .orElse("unknown");

            message =
                    "JSON mapping error at field: "
                            + path
                            + "\n"
                            + message;
        }

        if (message == null || message.isBlank()) {
            message =
                    "Unknown error while processing agent report";
        }

        return ResponseEntity
                .badRequest()
                .body(
                        "Agent report processing error:\n"
                                + message
                );
    }
}
