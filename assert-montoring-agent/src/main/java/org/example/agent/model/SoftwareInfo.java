package org.example.agent.model;

import java.time.LocalDateTime;

public class SoftwareInfo {

    private String softwareName;
    private String version;
    private String publisher;
    private String installLocation;
    private LocalDateTime installedAt;
    private LocalDateTime lastSeenAt;
    private String status;

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {

        return "SoftwareInfo{" +
                "softwareName='" + softwareName + '\'' +
                ", version='" + version + '\'' +
                ", publisher='" + publisher + '\'' +
                ", installLocation='" + installLocation + '\'' +
                ", installedAt=" + installedAt +
                ", lastSeenAt=" + lastSeenAt +
                ", status='" + status + '\'' +
                '}';
    }
}