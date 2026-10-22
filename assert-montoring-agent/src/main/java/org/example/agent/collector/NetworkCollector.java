package org.example.agent.collector;

import org.example.agent.model.NetworkInfo;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkCollector {

    private final SystemInfo systemInfo;

    public NetworkCollector() {
        systemInfo = new SystemInfo();
    }

    public List<NetworkInfo> collect() {

        List<NetworkInfo> networks = new ArrayList<>();

        try {

            System.out.println();
            System.out.println("===== JAVA NETWORK COLLECTION =====");

            Map<String, NetworkRoute> routes =
                    getDefaultRoutes();

            Map<String, List<String>> dnsServers =
                    getDnsServers();

            String wifiName =
                    getWifiName();

            List<NetworkIF> oshiInterfaces =
                    systemInfo
                            .getHardware()
                            .getNetworkIFs();

            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            if (interfaces == null) {
                return networks;
            }

            while (interfaces.hasMoreElements()) {

                NetworkInterface networkInterface =
                        interfaces.nextElement();

                if (!isUsable(networkInterface)) {
                    continue;
                }

                NetworkInfo network =
                        new NetworkInfo();

                String interfaceName =
                        networkInterface.getDisplayName();

                network.setInterfaceName(
                        interfaceName
                );

                network.setInterfaceType(
                        detectInterfaceType(
                                networkInterface
                        )
                );

                // ==========================================
                // MAC ADDRESS
                // ==========================================

                byte[] mac =
                        networkInterface.getHardwareAddress();

                network.setMacAddress(
                        formatMacAddress(mac)
                );

                // ==========================================
                // IP ADDRESSES
                // ==========================================

                String ipv4 = null;
                String ipv6 = null;
                String subnetMask = null;

                for (
                        InterfaceAddress interfaceAddress
                        : networkInterface.getInterfaceAddresses()
                ) {

                    InetAddress address =
                            interfaceAddress.getAddress();

                    if (address instanceof Inet4Address) {

                        ipv4 =
                                address.getHostAddress();

                        short prefixLength =
                                interfaceAddress
                                        .getNetworkPrefixLength();

                        subnetMask =
                                prefixLengthToSubnetMask(
                                        prefixLength
                                );
                    }

                    else if (address instanceof Inet6Address) {

                        String addressText =
                                address.getHostAddress();

                        // Ignore link-local IPv6
                        if (!addressText.startsWith("fe80:")) {

                            ipv6 = addressText;
                        }
                    }
                }

                network.setIpv4Address(ipv4);
                network.setIpv6Address(ipv6);
                network.setSubnetMask(subnetMask);

                // ==========================================
                // ROUTE / GATEWAY
                // ==========================================

                NetworkRoute route =
                        routes.get(ipv4);

                if (route != null) {

                    network.setGateway(
                            route.gateway
                    );

                    network.setPrimary(true);

                    network.setConnectionStatus(
                            "CONNECTED"
                    );
                }

                else {

                    network.setPrimary(false);

                    network.setConnectionStatus(
                            networkInterface.isUp()
                                    ? "CONNECTED"
                                    : "DISCONNECTED"
                    );
                }

                // ==========================================
                // WI-FI SSID
                // ==========================================

                if ("WIRELESS".equals(
                        network.getInterfaceType()
                )) {

                    network.setNetworkName(
                            wifiName
                    );
                }

                // ==========================================
                // DNS SERVERS
                // ==========================================

                if (ipv4 != null) {

                    List<String> dns =
                            dnsServers.get(
                                    networkInterface.getName()
                            );

                    if (dns == null) {
                        dns = new ArrayList<>();
                    }

                    network.setDnsServers(dns);
                }

                // ==========================================
                // SPEED FROM OSHI
                // ==========================================

                Long speedMbps =
                        findSpeed(
                                networkInterface,
                                oshiInterfaces
                        );

                network.setSpeedMbps(speedMbps);

                // ==========================================
                // LAST SEEN
                // ==========================================

                network.setLastSeenAt(
                        LocalDateTime.now()
                );

                networks.add(network);
            }

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Network collection error:"
            );

            e.printStackTrace();
        }

        System.out.println();
        System.out.println(
                "Networks created: " +
                        networks.size()
        );

        System.out.println(
                "=================================="
        );

        return networks;
    }


    // =====================================================
    // CHECK WHETHER NETWORK INTERFACE IS USABLE
    // =====================================================

    private boolean isUsable(
            NetworkInterface networkInterface
    ) throws Exception {

        if (networkInterface.isLoopback()) {
            return false;
        }

        if (networkInterface.isVirtual()) {
            return false;
        }

        if (!networkInterface.isUp()) {
            return false;
        }

        Enumeration<InetAddress> addresses =
                networkInterface.getInetAddresses();

        while (addresses.hasMoreElements()) {

            InetAddress address =
                    addresses.nextElement();

            if (address instanceof Inet4Address) {
                return true;
            }
        }

        return false;
    }


    // =====================================================
    // DETECT INTERFACE TYPE
    // =====================================================

    private String detectInterfaceType(
            NetworkInterface networkInterface
    ) {

        String name =
                networkInterface
                        .getDisplayName()
                        .toLowerCase();

        String interfaceName =
                networkInterface
                        .getName()
                        .toLowerCase();

        if (
                name.contains("wi-fi")
                        ||
                        name.contains("wifi")
                        ||
                        name.contains("wireless")
                        ||
                        name.contains("802.11")
                        ||
                        interfaceName.contains("wi-fi")
                        ||
                        interfaceName.contains("wifi")
        ) {

            return "WIRELESS";
        }

        if (
                name.contains("ethernet")
                        ||
                        name.contains("gigabit")
                        ||
                        name.contains("realtek")
                        ||
                        name.contains("intel(r) ethernet")
        ) {

            return "WIRED";
        }

        return "OTHER";
    }


    // =====================================================
    // FORMAT MAC ADDRESS
    // =====================================================

    private String formatMacAddress(
            byte[] mac
    ) {

        if (mac == null || mac.length == 0) {
            return null;
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0; i < mac.length; i++) {

            if (i > 0) {
                result.append("-");
            }

            result.append(
                    String.format(
                            "%02X",
                            mac[i] & 0xFF
                    )
            );
        }

        return result.toString();
    }


    // =====================================================
    // FIND NETWORK SPEED
    // =====================================================

    private Long findSpeed(
            NetworkInterface javaInterface,
            List<NetworkIF> oshiInterfaces
    ) {

        if (oshiInterfaces == null) {
            return null;
        }

        for (NetworkIF networkIF :
                oshiInterfaces) {

            String displayName =
                    networkIF.getDisplayName();

            String name =
                    networkIF.getName();

            if (
                    displayName != null
                            &&
                            displayName.equalsIgnoreCase(
                                    javaInterface.getDisplayName()
                            )
            ) {

                return convertSpeed(
                        networkIF.getSpeed()
                );
            }

            if (
                    name != null
                            &&
                            name.equalsIgnoreCase(
                                    javaInterface.getName()
                            )
            ) {

                return convertSpeed(
                        networkIF.getSpeed()
                );
            }
        }

        return null;
    }


    // =====================================================
    // CONVERT SPEED
    // =====================================================

    private Long convertSpeed(
            long bitsPerSecond
    ) {

        if (bitsPerSecond <= 0) {
            return null;
        }

        return Math.round(
                bitsPerSecond / 1_000_000.0
        );
    }


    // =====================================================
    // GET DEFAULT ROUTES
    // =====================================================

    private Map<String, NetworkRoute>
    getDefaultRoutes() {

        Map<String, NetworkRoute> routes =
                new HashMap<>();

        try {

            String output =
                    executeCommand(
                            "route",
                            "print",
                            "-4"
                    );

            String[] lines =
                    output.split("\\R");

            for (String line : lines) {

                String trimmed =
                        line.trim();

                if (!trimmed.startsWith(
                        "0.0.0.0"
                )) {
                    continue;
                }

                String[] parts =
                        trimmed.split("\\s+");

                /*
                 * Typical Windows line:
                 *
                 * 0.0.0.0
                 * 0.0.0.0
                 * 172.16.0.1
                 * 172.16.92.175
                 * 55
                 */

                if (parts.length >= 4) {

                    String gateway =
                            parts[2];

                    String interfaceIp =
                            parts[3];

                    routes.put(
                            interfaceIp,
                            new NetworkRoute(
                                    gateway
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not read Windows routes."
            );
        }

        return routes;
    }


    // =====================================================
    // GET DNS SERVERS
    // =====================================================

    private Map<String, List<String>>
    getDnsServers() {

        Map<String, List<String>> result =
                new HashMap<>();

        try {

            String output =
                    executeCommand(
                            "ipconfig",
                            "/all"
                    );

            String[] lines =
                    output.split("\\R");

            String currentAdapter = null;

            List<String> dns =
                    new ArrayList<>();

            for (String line : lines) {

                String trimmed =
                        line.trim();

                /*
                 * Adapter section:
                 *
                 * Wireless LAN adapter Wi-Fi:
                 * Ethernet adapter Ethernet:
                 */

                if (
                        line.contains("adapter ")
                                &&
                                line.trim().endsWith(":")
                ) {

                    // Store previous adapter DNS servers
                    if (
                            currentAdapter != null
                                    &&
                                    !dns.isEmpty()
                    ) {

                        result.put(
                                currentAdapter,
                                new ArrayList<>(dns)
                        );
                    }

                    currentAdapter =
                            extractAdapterName(line);

                    dns.clear();

                    continue;
                }

                // ==========================================
                // DNS SERVERS
                // ==========================================

                if (
                        trimmed.startsWith(
                                "DNS Servers"
                        )
                ) {

                    String[] parts =
                            trimmed.split(
                                    ":",
                                    2
                            );

                    if (parts.length == 2) {

                        String server =
                                parts[1].trim();

                        if (!server.isEmpty()) {
                            dns.add(server);
                        }
                    }

                    continue;
                }

                /*
                 * Continuation DNS addresses
                 */

                if (
                        !dns.isEmpty()
                                &&
                                (
                                        trimmed.matches(
                                                "\\d+\\.\\d+\\.\\d+\\.\\d+"
                                        )
                                                ||
                                                trimmed.contains(":")
                                )
                ) {

                    dns.add(trimmed);
                }
            }

            // Store final adapter
            if (
                    currentAdapter != null
                            &&
                            !dns.isEmpty()
            ) {

                result.put(
                        currentAdapter,
                        new ArrayList<>(dns)
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not read DNS information."
            );
        }

        return result;
    }


    // =====================================================
    // EXTRACT ADAPTER NAME
    // =====================================================

    private String extractAdapterName(
            String line
    ) {

        int adapterIndex =
                line.indexOf("adapter ");

        if (adapterIndex < 0) {
            return null;
        }

        String value =
                line.substring(
                        adapterIndex + 8
                ).trim();

        if (value.endsWith(":")) {

            value =
                    value.substring(
                            0,
                            value.length() - 1
                    );
        }

        return value;
    }


    // =====================================================
    // GET WI-FI NAME
    // =====================================================

    private String getWifiName() {

        try {

            String output =
                    executeCommand(
                            "netsh",
                            "wlan",
                            "show",
                            "interfaces"
                    );

            String[] lines =
                    output.split("\\R");

            for (String line : lines) {

                String trimmed =
                        line.trim();

                if (
                        trimmed.startsWith(
                                "SSID"
                        )
                                &&
                                !trimmed.startsWith(
                                        "BSSID"
                                )
                ) {

                    String[] parts =
                            trimmed.split(
                                    ":",
                                    2
                            );

                    if (parts.length == 2) {

                        return parts[1].trim();
                    }
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not read Wi-Fi name."
            );
        }

        return null;
    }


    // =====================================================
    // EXECUTE WINDOWS COMMAND
    // =====================================================

    private String executeCommand(
            String... command
    ) throws Exception {

        ProcessBuilder builder =
                new ProcessBuilder(command);

        builder.redirectErrorStream(true);

        Process process =
                builder.start();

        StringBuilder output =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                output.append(line);
                output.append(System.lineSeparator());
            }
        }

        process.waitFor();

        return output.toString();
    }


    // =====================================================
    // PREFIX LENGTH → SUBNET MASK
    // =====================================================

    private String prefixLengthToSubnetMask(
            short prefixLength
    ) {

        int prefix =
                prefixLength;

        if (prefix < 0 || prefix > 32) {
            return null;
        }

        if (prefix == 0) {
            return "0.0.0.0";
        }

        int mask =
                (int) (
                        0xFFFFFFFFL
                                << (32 - prefix)
                );

        return
                ((mask >>> 24) & 255)
                        + "." +
                        ((mask >>> 16) & 255)
                        + "." +
                        ((mask >>> 8) & 255)
                        + "." +
                        (mask & 255);
    }


    // =====================================================
    // NETWORK ROUTE MODEL
    // =====================================================

    private static class NetworkRoute {

        private final String gateway;

        private NetworkRoute(
                String gateway
        ) {

            this.gateway = gateway;
        }
    }
}