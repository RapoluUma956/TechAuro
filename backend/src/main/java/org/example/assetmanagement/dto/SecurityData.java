package org.example.assetmanagement.dto;

import lombok.Data;

@Data
public class SecurityData {

    private Boolean bitlockerEnabled;
    private String bitlockerProtectionStatus;
    private String bitlockerVolumeStatus;
    private Double bitlockerEncryptionPct;
    private String bitlockerEncryptionMethod;
    private String bitlockerKeyProtector;
    private String bitlockerVolume;

    private Boolean secureBootEnabled;

    private Boolean tpmPresent;
    private Boolean tpmEnabled;
    private String tpmVersion;
}