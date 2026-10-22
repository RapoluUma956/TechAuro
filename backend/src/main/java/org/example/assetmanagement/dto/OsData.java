package org.example.assetmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OsData {

    private String name;

    private String version;

    private String build;

    private String architecture;

    private String hostname;

    private LocalDateTime bootTime;

    private Long uptimeSeconds;
}