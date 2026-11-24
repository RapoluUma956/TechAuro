package com.assetmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "remote_terminal_audit",
        indexes = {
                @Index(name = "idx_rta_session_id", columnList = "session_id"),
                @Index(name = "idx_rta_device_id", columnList = "device_id"),
                @Index(name = "idx_rta_admin_user_id", columnList = "admin_user_id"),
                @Index(name = "idx_rta_recorded_at", columnList = "recorded_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RemoteTerminalAudit {

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
    private String action;

    @Column(name = "command_preview", length = 255)
    private String commandPreview;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}

