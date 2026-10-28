/*
 * Decompiled with CFR 0.152.
 */
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
        return this.softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getInstallLocation() {
        return this.installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public LocalDateTime getInstalledAt() {
        return this.installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public LocalDateTime getLastSeenAt() {
        return this.lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString() {
        return "SoftwareInfo{softwareName='" + this.softwareName + "', version='" + this.version + "', publisher='" + this.publisher + "', installLocation='" + this.installLocation + "', installedAt=" + String.valueOf(this.installedAt) + ", lastSeenAt=" + String.valueOf(this.lastSeenAt) + ", status='" + this.status + "'}";
    }
}

