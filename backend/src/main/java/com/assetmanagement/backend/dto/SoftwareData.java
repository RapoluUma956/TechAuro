package com.assetmanagement.backend.dto;

import lombok.Data;

@Data
public class SoftwareData {

    private String softwareName;
    private String version;
    private String publisher;
    private String installLocation;
    private String installedAt;
    private String lastSeenAt;
    private String status;
}