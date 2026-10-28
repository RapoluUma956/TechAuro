/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

public class SecurityInfo {
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

    public Boolean getBitlockerEnabled() {
        return this.bitlockerEnabled;
    }

    public void setBitlockerEnabled(Boolean bitlockerEnabled) {
        this.bitlockerEnabled = bitlockerEnabled;
    }

    public String getBitlockerProtectionStatus() {
        return this.bitlockerProtectionStatus;
    }

    public void setBitlockerProtectionStatus(String bitlockerProtectionStatus) {
        this.bitlockerProtectionStatus = bitlockerProtectionStatus;
    }

    public String getBitlockerVolumeStatus() {
        return this.bitlockerVolumeStatus;
    }

    public void setBitlockerVolumeStatus(String bitlockerVolumeStatus) {
        this.bitlockerVolumeStatus = bitlockerVolumeStatus;
    }

    public Double getBitlockerEncryptionPct() {
        return this.bitlockerEncryptionPct;
    }

    public void setBitlockerEncryptionPct(Double bitlockerEncryptionPct) {
        this.bitlockerEncryptionPct = bitlockerEncryptionPct;
    }

    public String getBitlockerEncryptionMethod() {
        return this.bitlockerEncryptionMethod;
    }

    public void setBitlockerEncryptionMethod(String bitlockerEncryptionMethod) {
        this.bitlockerEncryptionMethod = bitlockerEncryptionMethod;
    }

    public String getBitlockerKeyProtector() {
        return this.bitlockerKeyProtector;
    }

    public void setBitlockerKeyProtector(String bitlockerKeyProtector) {
        this.bitlockerKeyProtector = bitlockerKeyProtector;
    }

    public String getBitlockerVolume() {
        return this.bitlockerVolume;
    }

    public void setBitlockerVolume(String bitlockerVolume) {
        this.bitlockerVolume = bitlockerVolume;
    }

    public Boolean getSecureBootEnabled() {
        return this.secureBootEnabled;
    }

    public void setSecureBootEnabled(Boolean secureBootEnabled) {
        this.secureBootEnabled = secureBootEnabled;
    }

    public Boolean getTpmPresent() {
        return this.tpmPresent;
    }

    public void setTpmPresent(Boolean tpmPresent) {
        this.tpmPresent = tpmPresent;
    }

    public Boolean getTpmEnabled() {
        return this.tpmEnabled;
    }

    public void setTpmEnabled(Boolean tpmEnabled) {
        this.tpmEnabled = tpmEnabled;
    }

    public String getTpmVersion() {
        return this.tpmVersion;
    }

    public void setTpmVersion(String tpmVersion) {
        this.tpmVersion = tpmVersion;
    }

    public String toString() {
        return "SecurityInfo{bitlockerEnabled=" + this.bitlockerEnabled + ", bitlockerProtectionStatus='" + this.bitlockerProtectionStatus + "', bitlockerVolumeStatus='" + this.bitlockerVolumeStatus + "', bitlockerEncryptionPct=" + this.bitlockerEncryptionPct + ", bitlockerEncryptionMethod='" + this.bitlockerEncryptionMethod + "', bitlockerKeyProtector='" + this.bitlockerKeyProtector + "', bitlockerVolume='" + this.bitlockerVolume + "', secureBootEnabled=" + this.secureBootEnabled + ", tpmPresent=" + this.tpmPresent + ", tpmEnabled=" + this.tpmEnabled + ", tpmVersion='" + this.tpmVersion + "'}";
    }
}

