/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

import java.time.LocalDateTime;

public class OsInfo {
    private String name;
    private String version;
    private String build;
    private String architecture;
    private String hostname;
    private LocalDateTime bootTime;
    private Long uptimeSeconds;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return this.build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getArchitecture() {
        return this.architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public LocalDateTime getBootTime() {
        return this.bootTime;
    }

    public void setBootTime(LocalDateTime bootTime) {
        this.bootTime = bootTime;
    }

    public Long getUptimeSeconds() {
        return this.uptimeSeconds;
    }

    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public String toString() {
        return "OsInfo{name='" + this.name + "', version='" + this.version + "', build='" + this.build + "', architecture='" + this.architecture + "', hostname='" + this.hostname + "', bootTime=" + String.valueOf(this.bootTime) + ", uptimeSeconds=" + this.uptimeSeconds + "}";
    }
}

