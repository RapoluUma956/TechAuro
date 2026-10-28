/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

import java.time.LocalDateTime;
import java.util.List;

public class NetworkInfo {
    private String interfaceName;
    private String interfaceType;
    private String macAddress;
    private String ipv4Address;
    private String ipv6Address;
    private String subnetMask;
    private String gateway;
    private List<String> dnsServers;
    private String networkName;
    private String connectionStatus;
    private boolean primary;
    private Long speedMbps;
    private LocalDateTime lastSeenAt;

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceType() {
        return this.interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpv4Address() {
        return this.ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public String getIpv6Address() {
        return this.ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public String getSubnetMask() {
        return this.subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getGateway() {
        return this.gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public List<String> getDnsServers() {
        return this.dnsServers;
    }

    public void setDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
    }

    public String getNetworkName() {
        return this.networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getConnectionStatus() {
        return this.connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public Long getSpeedMbps() {
        return this.speedMbps;
    }

    public void setSpeedMbps(Long speedMbps) {
        this.speedMbps = speedMbps;
    }

    public LocalDateTime getLastSeenAt() {
        return this.lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String toString() {
        return "NetworkInfo{interfaceName='" + this.interfaceName + "', interfaceType='" + this.interfaceType + "', macAddress='" + this.macAddress + "', ipv4Address='" + this.ipv4Address + "', ipv6Address='" + this.ipv6Address + "', subnetMask='" + this.subnetMask + "', gateway='" + this.gateway + "', dnsServers=" + String.valueOf(this.dnsServers) + ", networkName='" + this.networkName + "', connectionStatus='" + this.connectionStatus + "', primary=" + this.primary + ", speedMbps=" + this.speedMbps + ", lastSeenAt=" + String.valueOf(this.lastSeenAt) + "}";
    }
}

