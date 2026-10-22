package org.example.assetmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "device_networks",
        indexes = {
                @Index(
                        name = "idx_device_networks_device",
                        columnList = "device_id"
                )
        }
)
public class DeviceNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "network_id")
    private Long networkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "device_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_device_networks_device")
    )
    private Device device;

    @Column(name = "interface_name", length = 100)
    private String interfaceName;

    @Column(name = "interface_type", length = 50)
    private String interfaceType;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(name = "ipv4_address", length = 45)
    private String ipv4Address;

    @Column(name = "ipv6_address", length = 45)
    private String ipv6Address;

    @Column(name = "subnet_mask", length = 45)
    private String subnetMask;

    @Column(name = "gateway", length = 45)
    private String gateway;

    @Column(name = "dns_servers", columnDefinition = "TEXT")
    private String dnsServers;

    @Column(name = "network_name", length = 150)
    private String networkName;

    @Column(name = "connection_status", length = 30)
    private String connectionStatus;

    @Column(name = "is_primary")
    private Boolean primary;

    @Column(name = "speed_mbps")
    private Long speedMbps;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;


    // Getters and Setters

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

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

    public String getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(String dnsServers) {
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

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
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


    @PrePersist
    @PreUpdate
    protected void updateLastSeen() {
        lastSeenAt = LocalDateTime.now();
    }
}