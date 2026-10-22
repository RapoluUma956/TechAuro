package org.example.agent.model;

public class SecurityInfo {

    // ==================================================
    // BITLOCKER
    // ==================================================

    private Boolean bitlockerEnabled;

    private String bitlockerProtectionStatus;

    private String bitlockerVolumeStatus;

    private Double bitlockerEncryptionPct;

    private String bitlockerEncryptionMethod;

    private String bitlockerKeyProtector;

    private String bitlockerVolume;

    // ==================================================
    // SECURE BOOT
    // ==================================================

    private Boolean secureBootEnabled;

    // ==================================================
    // TPM
    // ==================================================

    private Boolean tpmPresent;

    private Boolean tpmEnabled;

    private String tpmVersion;

    public SecurityInfo() {
    }

    // ==================================================
    // BITLOCKER GETTERS / SETTERS
    // ==================================================

    public Boolean getBitlockerEnabled() {
        return bitlockerEnabled;
    }

    public void setBitlockerEnabled(
            Boolean bitlockerEnabled) {

        this.bitlockerEnabled =
                bitlockerEnabled;
    }

    public String getBitlockerProtectionStatus() {
        return bitlockerProtectionStatus;
    }

    public void setBitlockerProtectionStatus(
            String bitlockerProtectionStatus) {

        this.bitlockerProtectionStatus =
                bitlockerProtectionStatus;
    }

    public String getBitlockerVolumeStatus() {
        return bitlockerVolumeStatus;
    }

    public void setBitlockerVolumeStatus(
            String bitlockerVolumeStatus) {

        this.bitlockerVolumeStatus =
                bitlockerVolumeStatus;
    }

    public Double getBitlockerEncryptionPct() {
        return bitlockerEncryptionPct;
    }

    public void setBitlockerEncryptionPct(
            Double bitlockerEncryptionPct) {

        this.bitlockerEncryptionPct =
                bitlockerEncryptionPct;
    }

    public String getBitlockerEncryptionMethod() {
        return bitlockerEncryptionMethod;
    }

    public void setBitlockerEncryptionMethod(
            String bitlockerEncryptionMethod) {

        this.bitlockerEncryptionMethod =
                bitlockerEncryptionMethod;
    }

    public String getBitlockerKeyProtector() {
        return bitlockerKeyProtector;
    }

    public void setBitlockerKeyProtector(
            String bitlockerKeyProtector) {

        this.bitlockerKeyProtector =
                bitlockerKeyProtector;
    }

    public String getBitlockerVolume() {
        return bitlockerVolume;
    }

    public void setBitlockerVolume(
            String bitlockerVolume) {

        this.bitlockerVolume =
                bitlockerVolume;
    }

    // ==================================================
    // SECURE BOOT
    // ==================================================

    public Boolean getSecureBootEnabled() {
        return secureBootEnabled;
    }

    public void setSecureBootEnabled(
            Boolean secureBootEnabled) {

        this.secureBootEnabled =
                secureBootEnabled;
    }

    // ==================================================
    // TPM
    // ==================================================

    public Boolean getTpmPresent() {
        return tpmPresent;
    }

    public void setTpmPresent(
            Boolean tpmPresent) {

        this.tpmPresent =
                tpmPresent;
    }

    public Boolean getTpmEnabled() {
        return tpmEnabled;
    }

    public void setTpmEnabled(
            Boolean tpmEnabled) {

        this.tpmEnabled =
                tpmEnabled;
    }

    public String getTpmVersion() {
        return tpmVersion;
    }

    public void setTpmVersion(
            String tpmVersion) {

        this.tpmVersion =
                tpmVersion;
    }

    @Override
    public String toString() {

        return "SecurityInfo{" +
                "bitlockerEnabled=" +
                bitlockerEnabled +
                ", bitlockerProtectionStatus='" +
                bitlockerProtectionStatus + '\'' +
                ", bitlockerVolumeStatus='" +
                bitlockerVolumeStatus + '\'' +
                ", bitlockerEncryptionPct=" +
                bitlockerEncryptionPct +
                ", bitlockerEncryptionMethod='" +
                bitlockerEncryptionMethod + '\'' +
                ", bitlockerKeyProtector='" +
                bitlockerKeyProtector + '\'' +
                ", bitlockerVolume='" +
                bitlockerVolume + '\'' +
                ", secureBootEnabled=" +
                secureBootEnabled +
                ", tpmPresent=" +
                tpmPresent +
                ", tpmEnabled=" +
                tpmEnabled +
                ", tpmVersion='" +
                tpmVersion + '\'' +
                '}';
    }
}