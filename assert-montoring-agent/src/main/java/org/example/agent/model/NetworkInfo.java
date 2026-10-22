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


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }


    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }


    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }


    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }


    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }


    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }


    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }


    public List<String> getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
    }


    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }


    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }


    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }


    public Long getSpeedMbps() {
        return speedMbps;
    }

    public void setSpeedMbps(Long speedMbps) {
        this.speedMbps = speedMbps;
    }


    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }


    @Override
    public String toString() {

        return "NetworkInfo{" +
                "interfaceName='" + interfaceName + '\'' +
                ", interfaceType='" + interfaceType + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", ipv4Address='" + ipv4Address + '\'' +
                ", ipv6Address='" + ipv6Address + '\'' +
                ", subnetMask='" + subnetMask + '\'' +
                ", gateway='" + gateway + '\'' +
                ", dnsServers=" + dnsServers +
                ", networkName='" + networkName + '\'' +
                ", connectionStatus='" + connectionStatus + '\'' +
                ", primary=" + primary +
                ", speedMbps=" + speedMbps +
                ", lastSeenAt=" + lastSeenAt +
                '}';
    }
}