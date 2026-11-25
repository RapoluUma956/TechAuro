package com.assetmanagement.backend.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TerminalMessage {
    private String messageType;
    private String sessionId;
    private Long deviceId;
    private String serialNumber;
    private String payload;
    private Integer cols;
    private Integer rows;
    private Long timestamp;

    public static TerminalMessage error(String sessionId, String errorMessage) {
        return TerminalMessage.builder()
                .messageType("TERMINAL_ERROR")
                .sessionId(sessionId)
                .payload(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

