/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.hardware.NetworkIF
 */
package org.example.agent.collector;

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
import org.example.agent.model.NetworkInfo;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

public class NetworkCollector {
    private final SystemInfo systemInfo = new SystemInfo();

    public List<NetworkInfo> collect() {
        ArrayList<NetworkInfo> networks = new ArrayList<NetworkInfo>();
        try {
            System.out.println();
            System.out.println("===== JAVA NETWORK COLLECTION =====");
            Map<String, NetworkRoute> routes = this.getDefaultRoutes();
            Map<String, List<String>> dnsServers = this.getDnsServers();
            String wifiName = this.getWifiName();
            List oshiInterfaces = this.systemInfo.getHardware().getNetworkIFs();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return networks;
            }
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!this.isUsable(networkInterface)) continue;
                NetworkInfo network = new NetworkInfo();
                String interfaceName = networkInterface.getDisplayName();
                network.setInterfaceName(interfaceName);
                network.setInterfaceType(this.detectInterfaceType(networkInterface));
                byte[] mac = networkInterface.getHardwareAddress();
                network.setMacAddress(this.formatMacAddress(mac));
                String ipv4 = null;
                String ipv6 = null;
                String subnetMask = null;
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    String addressText;
                    InetAddress address = interfaceAddress.getAddress();
                    if (address instanceof Inet4Address) {
                        ipv4 = address.getHostAddress();
                        short prefixLength = interfaceAddress.getNetworkPrefixLength();
                        subnetMask = this.prefixLengthToSubnetMask(prefixLength);
                        continue;
                    }
                    if (!(address instanceof Inet6Address) || (addressText = address.getHostAddress()).startsWith("fe80:")) continue;
                    ipv6 = addressText;
                }
                network.setIpv4Address(ipv4);
                network.setIpv6Address(ipv6);
                network.setSubnetMask(subnetMask);
                NetworkRoute route = routes.get(ipv4);
                if (route != null) {
                    network.setGateway(route.gateway);
                    network.setPrimary(true);
                    network.setConnectionStatus("CONNECTED");
                } else {
                    network.setPrimary(false);
                    network.setConnectionStatus(networkInterface.isUp() ? "CONNECTED" : "DISCONNECTED");
                }
                if ("WIRELESS".equals(network.getInterfaceType())) {
                    network.setNetworkName(wifiName);
                }
                if (ipv4 != null) {
                    List<String> dns = dnsServers.get(networkInterface.getName());
                    if (dns == null) {
                        dns = new ArrayList<String>();
                    }
                    network.setDnsServers(dns);
                }
                Long speedMbps = this.findSpeed(networkInterface, oshiInterfaces);
                network.setSpeedMbps(speedMbps);
                network.setLastSeenAt(LocalDateTime.now());
                networks.add(network);
            }
        }
        catch (Exception e) {
            System.out.println();
            System.out.println("Network collection error:");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Networks created: " + networks.size());
        System.out.println("==================================");
        return networks;
    }

    private boolean isUsable(NetworkInterface networkInterface) throws Exception {
        if (networkInterface.isLoopback()) {
            return false;
        }
        if (networkInterface.isVirtual()) {
            return false;
        }
        if (!networkInterface.isUp()) {
            return false;
        }
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (!(address instanceof Inet4Address)) continue;
            return true;
        }
        return false;
    }

    private String detectInterfaceType(NetworkInterface networkInterface) {
        String name = networkInterface.getDisplayName().toLowerCase();
        String interfaceName = networkInterface.getName().toLowerCase();
        if (name.contains("wi-fi") || name.contains("wifi") || name.contains("wireless") || name.contains("802.11") || interfaceName.contains("wi-fi") || interfaceName.contains("wifi")) {
            return "WIRELESS";
        }
        if (name.contains("ethernet") || name.contains("gigabit") || name.contains("realtek") || name.contains("intel(r) ethernet")) {
            return "WIRED";
        }
        return "OTHER";
    }

    private String formatMacAddress(byte[] mac) {
        if (mac == null || mac.length == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mac.length; ++i) {
            if (i > 0) {
                result.append("-");
            }
            result.append(String.format("%02X", mac[i] & 0xFF));
        }
        return result.toString();
    }

    private Long findSpeed(NetworkInterface javaInterface, List<NetworkIF> oshiInterfaces) {
        if (oshiInterfaces == null) {
            return null;
        }
        for (NetworkIF networkIF : oshiInterfaces) {
            String displayName = networkIF.getDisplayName();
            String name = networkIF.getName();
            if (displayName != null && displayName.equalsIgnoreCase(javaInterface.getDisplayName())) {
                return this.convertSpeed(networkIF.getSpeed());
            }
            if (name == null || !name.equalsIgnoreCase(javaInterface.getName())) continue;
            return this.convertSpeed(networkIF.getSpeed());
        }
        return null;
    }

    private Long convertSpeed(long bitsPerSecond) {
        if (bitsPerSecond <= 0L) {
            return null;
        }
        return Math.round((double)bitsPerSecond / 1000000.0);
    }

    private Map<String, NetworkRoute> getDefaultRoutes() {
        HashMap<String, NetworkRoute> routes = new HashMap<String, NetworkRoute>();
        try {
            String[] lines;
            String output = this.executeCommand("route", "print", "-4");
            for (String line : lines = output.split("\\R")) {
                String[] parts;
                String trimmed = line.trim();
                if (!trimmed.startsWith("0.0.0.0") || (parts = trimmed.split("\\s+")).length < 4) continue;
                String gateway = parts[2];
                String interfaceIp = parts[3];
                routes.put(interfaceIp, new NetworkRoute(gateway));
            }
        }
        catch (Exception e) {
            System.out.println("Could not read Windows routes.");
        }
        return routes;
    }

    private Map<String, List<String>> getDnsServers() {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        try {
            String output = this.executeCommand("ipconfig", "/all");
            String[] lines = output.split("\\R");
            String currentAdapter = null;
            ArrayList<String> dns = new ArrayList<String>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (line.contains("adapter ") && line.trim().endsWith(":")) {
                    if (currentAdapter != null && !dns.isEmpty()) {
                        result.put(currentAdapter, new ArrayList(dns));
                    }
                    currentAdapter = this.extractAdapterName(line);
                    dns.clear();
                    continue;
                }
                if (trimmed.startsWith("DNS Servers")) {
                    String server;
                    String[] parts = trimmed.split(":", 2);
                    if (parts.length != 2 || (server = parts[1].trim()).isEmpty()) continue;
                    dns.add(server);
                    continue;
                }
                if (dns.isEmpty() || !trimmed.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") && !trimmed.contains(":")) continue;
                dns.add(trimmed);
            }
            if (currentAdapter != null && !dns.isEmpty()) {
                result.put(currentAdapter, new ArrayList(dns));
            }
        }
        catch (Exception e) {
            System.out.println("Could not read DNS information.");
        }
        return result;
    }

    private String extractAdapterName(String line) {
        int adapterIndex = line.indexOf("adapter ");
        if (adapterIndex < 0) {
            return null;
        }
        String value = line.substring(adapterIndex + 8).trim();
        if (value.endsWith(":")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String getWifiName() {
        try {
            String[] lines;
            String output = this.executeCommand("netsh", "wlan", "show", "interfaces");
            for (String line : lines = output.split("\\R")) {
                String[] parts;
                String trimmed = line.trim();
                if (!trimmed.startsWith("SSID") || trimmed.startsWith("BSSID") || (parts = trimmed.split(":", 2)).length != 2) continue;
                return parts[1].trim();
            }
        }
        catch (Exception e) {
            System.out.println("Could not read Wi-Fi name.");
        }
        return null;
    }

    private String executeCommand(String ... command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append(System.lineSeparator());
            }
        }
        process.waitFor();
        return output.toString();
    }

    private String prefixLengthToSubnetMask(short prefixLength) {
        short prefix = prefixLength;
        if (prefix < 0 || prefix > 32) {
            return null;
        }
        if (prefix == 0) {
            return "0.0.0.0";
        }
        int mask = (int)(0xFFFFFFFFL << 32 - prefix);
        return (mask >>> 24 & 0xFF) + "." + (mask >>> 16 & 0xFF) + "." + (mask >>> 8 & 0xFF) + "." + (mask & 0xFF);
    }

    private static class NetworkRoute {
        private final String gateway;

        private NetworkRoute(String gateway) {
            this.gateway = gateway;
        }
    }
}

