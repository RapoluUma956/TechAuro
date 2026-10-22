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

    public OsInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public LocalDateTime getBootTime() {
        return bootTime;
    }

    public void setBootTime(LocalDateTime bootTime) {
        this.bootTime = bootTime;
    }

    public Long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    @Override
    public String toString() {
        return "OsInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", build='" + build + '\'' +
                ", architecture='" + architecture + '\'' +
                ", hostname='" + hostname + '\'' +
                ", bootTime=" + bootTime +
                ", uptimeSeconds=" + uptimeSeconds +
                '}';
    }
}