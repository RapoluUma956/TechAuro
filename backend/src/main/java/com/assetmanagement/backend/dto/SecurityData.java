package com.assetmanagement.backend.dto;

import lombok.Data;

@Data
public class SecurityData {

    private Boolean bitlockerEnabled;
    private String bitlockerProtectionStatus;
    private String bitlockerVolumeStatus;
    private Double bitlockerEncryptionPercent;
    private String bitlockerEncryptionMethod;
    private String bitlockerKeyProtector;
    private String bitlockerVolume;

    private Boolean secureBootEnabled;

    private Boolean tpmPresent;
    private Boolean tpmEnabled;
    private String tpmVersion;

    // Compatibility getter/setter
    public Double getBitlockerEncryptionPct() {
        return bitlockerEncryptionPercent;
    }

    public void setBitlockerEncryptionPct(Double bitlockerEncryptionPct) {
        this.bitlockerEncryptionPercent = bitlockerEncryptionPct;
    }
}