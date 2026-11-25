package com.assetmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "remote_terminal_sessions",
        indexes = {
                @Index(name = "idx_rts_session_id", columnList = "session_id"),
                @Index(name = "idx_rts_device_id", columnList = "device_id"),
                @Index(name = "idx_rts_serial_number", columnList = "serial_number"),
                @Index(name = "idx_rts_status", columnList = "status"),
                @Index(name = "idx_rts_started_at", columnList = "started_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RemoteTerminalSession {

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

    @Column(name = "shell_type", length = 30)
    private String shellType;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}

