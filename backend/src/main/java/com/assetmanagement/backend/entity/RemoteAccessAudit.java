package com.assetmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "remote_access_audit",
        indexes = {
                @Index(name = "idx_raa_session_id", columnList = "session_id"),
                @Index(name = "idx_raa_device_id", columnList = "device_id"),
                @Index(name = "idx_raa_admin_user_id", columnList = "admin_user_id"),
                @Index(name = "idx_raa_recorded_at", columnList = "recorded_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RemoteAccessAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action; // SESSION_REQUESTED, APPROVED, REJECTED, MOUSE_CONTROL, KEYBOARD_CONTROL, CLIPBOARD_SHARED, FILE_TRANSFERRED, DISCONNECTED

    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
