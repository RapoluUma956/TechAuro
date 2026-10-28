/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.agent.model.SoftwareInfo;

public class SoftwareCollector {
    private boolean registryCollectionFailed;

    public List<SoftwareInfo> collect() {
        this.registryCollectionFailed = false;
        System.out.println();
        System.out.println("==============================================");
        System.out.println("           INSTALLED SOFTWARE");
        System.out.println("==============================================");
        LinkedHashMap<String, SoftwareInfo> softwareMap = new LinkedHashMap<String, SoftwareInfo>();
        this.collectFromRegistry(softwareMap, "HKLM64");
        this.collectFromRegistry(softwareMap, "HKLM32");
        this.collectFromRegistry(softwareMap, "HKCU");
        if (this.registryCollectionFailed) {
            throw new IllegalStateException("Installed-software inventory is incomplete because at least one Windows Registry source failed. Skipping this monitoring cycle to avoid falsely marking software as UNINSTALLED.");
        }
        this.collectFromWinget(softwareMap);
        ArrayList<SoftwareInfo> software = new ArrayList<SoftwareInfo>(softwareMap.values());
        System.out.println();
        System.out.println("Total unique software detected: " + software.size());
        return software;
    }

    private void collectFromRegistry(Map<String, SoftwareInfo> softwareMap, String source) {
        String registryPath;
        System.out.println();
        System.out.println("Collecting software from Windows Registry: " + source);
        switch (source) {
            case "HKLM64": {
                registryPath = "HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*";
                break;
            }
            case "HKLM32": {
                registryPath = "HKLM:\\Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*";
                break;
            }
            case "HKCU": {
                registryPath = "HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*";
                break;
            }
            default: {
                return;
            }
        }
        String script = "Get-ItemProperty '" + registryPath + "' | Where-Object { $_.DisplayName } | ForEach-Object { $name=$_.DisplayName; $version=$_.DisplayVersion; $publisher=$_.Publisher; $location=$_.InstallLocation; $name=$name.Replace('|',' '); $version=$version.Replace('|',' '); $publisher=$publisher.Replace('|',' '); $location=$location.Replace('|',' '); Write-Output ($name+'|'+$version+'|'+$publisher+'|'+$location) }";
        try {
            String output = this.executePowerShell(script);
            if (output == null || output.isBlank()) {
                System.out.println("Software found from " + source + ": 0");
                return;
            }
            int count = 0;
            for (String line : output.split("\\R")) {
                String[] parts;
                if (line == null || line.isBlank() || (parts = line.split("\\|", -1)).length < 4) continue;
                String name = this.clean(parts[0]);
                String version = this.clean(parts[1]);
                String publisher = this.clean(parts[2]);
                String location = this.clean(parts[3]);
                if (name == null) continue;
                this.addSoftware(softwareMap, name, version, publisher, location);
                ++count;
            }
            System.out.println("Software found from " + source + ": " + count);
        }
        catch (Exception e) {
            this.registryCollectionFailed = true;
            System.out.println("Registry collection failed for " + source + ": " + e.getMessage());
        }
    }

    private void collectFromWinget(Map<String, SoftwareInfo> softwareMap) {
        System.out.println();
        System.out.println("Collecting software using winget...");
        String script = "winget list --accept-source-agreements";
        try {
            String output = this.executeCommand(script);
            if (output == null || output.isBlank()) {
                System.out.println("No software returned by winget.");
                return;
            }
            int count = 0;
            for (String line : output.split("\\R")) {
                ParsedWingetSoftware parsed;
                String trimmed;
                if (line == null || line.isBlank() || (trimmed = line.trim()).isEmpty() || trimmed.startsWith("Name") || trimmed.startsWith("---") || trimmed.startsWith("Updating") || trimmed.startsWith("Name ") || (parsed = this.parseWingetLine(trimmed)) == null || parsed.name == null) continue;
                this.addSoftware(softwareMap, parsed.name, parsed.version, parsed.publisher, null);
                ++count;
                System.out.println("Detected: " + parsed.name + " | Version: " + parsed.version + " | Publisher: " + parsed.publisher);
            }
            System.out.println();
            System.out.println("Software returned by winget: " + count);
        }
        catch (Exception e) {
            System.out.println("winget collection failed: " + e.getMessage());
        }
    }

    private ParsedWingetSoftware parseWingetLine(String line) {
        String[] parts = line.split("\\s{2,}");
        if (parts.length < 2) {
            return null;
        }
        String name = this.clean(parts[0]);
        if (name == null) {
            return null;
        }
        ArrayList<String> values = new ArrayList<String>();
        for (int i = 1; i < parts.length; ++i) {
            String value = this.clean(parts[i]);
            if (value == null) continue;
            values.add(value);
        }
        if (values.isEmpty()) {
            return null;
        }
        String version = null;
        String publisher = null;
        for (String value : values) {
            if (version == null && this.looksLikeVersion(value)) {
                version = value;
                continue;
            }
            if (publisher != null || this.looksLikeIdentifier(value) || this.looksLikeVersion(value)) continue;
            publisher = value;
        }
        if (version == null && !values.isEmpty()) {
            version = (String)values.get(0);
        }
        if (publisher != null && this.looksLikeIdentifier(publisher)) {
            publisher = null;
        }
        return new ParsedWingetSoftware(name, version, publisher);
    }

    private boolean looksLikeVersion(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches("\\d+(\\.\\d+)+([._-][A-Za-z0-9]+)*");
    }

    private boolean looksLikeIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches("[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)+");
    }

    private void addSoftware(Map<String, SoftwareInfo> softwareMap, String name, String version, String publisher, String location) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = name.trim().toLowerCase();
        SoftwareInfo existing = softwareMap.get(key);
        if (existing == null) {
            SoftwareInfo software = new SoftwareInfo();
            software.setSoftwareName(name.trim());
            software.setVersion(version);
            software.setPublisher(publisher);
            software.setInstallLocation(location);
            software.setInstalledAt(null);
            software.setLastSeenAt(LocalDateTime.now());
            software.setStatus("INSTALLED");
            softwareMap.put(key, software);
            return;
        }
        if (this.isBlank(existing.getVersion()) && !this.isBlank(version)) {
            existing.setVersion(version);
        }
        if (this.isBlank(existing.getPublisher()) && !this.isBlank(publisher)) {
            existing.setPublisher(publisher);
        }
        if (this.isBlank(existing.getInstallLocation()) && !this.isBlank(location)) {
            existing.setInstallLocation(location);
        }
        existing.setLastSeenAt(LocalDateTime.now());
    }

    private String executePowerShell(String script) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-Command", script);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append(System.lineSeparator());
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("PowerShell exited with code: " + exitCode + "\nOutput:\n" + String.valueOf(output));
        }
        return output.toString().trim();
    }

    private String executeCommand(String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append(System.lineSeparator());
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command exited with code: " + exitCode + "\nOutput:\n" + String.valueOf(output));
        }
        return output.toString().trim();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class ParsedWingetSoftware {
        private final String name;
        private final String version;
        private final String publisher;

        private ParsedWingetSoftware(String name, String version, String publisher) {
            this.name = name;
            this.version = version;
            this.publisher = publisher;
        }
    }
}

