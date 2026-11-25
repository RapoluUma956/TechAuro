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
public class RemoteDesktopMessage {
    private String messageType;
    private String sessionId;
    private Long deviceId;
    private String serialNumber;
    private String adminName;
    private String adminEmail;
    private String payload; // text, base64 dataUrl, or json payload

    // Screen info
    private Integer screenWidth;
    private Integer screenHeight;

    // Remote Input info
    private String inputType; // MOUSE_MOVE, MOUSE_DOWN, MOUSE_UP, CLICK, DOUBLE_CLICK, SCROLL, KEY_DOWN, KEY_UP
    private Double x;         // Normalized X (0.0 to 1.0)
    private Double y;         // Normalized Y (0.0 to 1.0)
    private Integer button;   // 0 = left, 1 = middle, 2 = right
    private Integer deltaY;   // scroll delta
    private Integer keyCode;  // key event code
    private String key;       // key character

    // File transfer info
    private String fileName;
    private Long fileSize;
    private Integer chunkIndex;
    private Integer totalChunks;

    private Long timestamp;

    public static RemoteDesktopMessage error(String sessionId, String errorMessage) {
        return RemoteDesktopMessage.builder()
                .messageType("ERROR")
                .sessionId(sessionId)
                .payload(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
