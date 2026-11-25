package com.assetmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "remote_access_sessions",
        indexes = {
                @Index(name = "idx_ras_session_id", columnList = "session_id"),
                @Index(name = "idx_ras_device_id", columnList = "device_id"),
                @Index(name = "idx_ras_serial_number", columnList = "serial_number"),
                @Index(name = "idx_ras_status", columnList = "status"),
                @Index(name = "idx_ras_requested_at", columnList = "requested_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RemoteAccessSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "admin_email", length = 150)
    private String adminEmail;

    @Column(name = "admin_name", length = 150)
    private String adminName;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING_APPROVAL, ACTIVE, REJECTED, CLOSED, DISCONNECTED, FAILED

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "termination_reason", length = 255)
    private String terminationReason;

    @Column(name = "connection_type", length = 50)
    private String connectionType = "WEBSOCKET_STREAM";
}
