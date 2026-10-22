package org.example.assetmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NetworkData {

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
}