/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 */
package org.example.agent.collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.example.agent.model.SecurityInfo;
import oshi.SystemInfo;

public class SecurityCollector {
    private final SystemInfo systemInfo = new SystemInfo();

    public SecurityInfo collect() {
        SecurityInfo securityInfo = new SecurityInfo();
        this.collectBitLocker(securityInfo);
        this.collectSecureBoot(securityInfo);
        this.collectTpm(securityInfo);
        return securityInfo;
    }

    private void collectBitLocker(SecurityInfo securityInfo) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("manage-bde.exe", "-status", "C:");
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            process.waitFor();
            String result = output.toString();
            this.parseBitLockerOutput(result, securityInfo);
        }
        catch (Exception e) {
            System.out.println("Unable to collect BitLocker information: " + e.getMessage());
        }
    }

    private void parseBitLockerOutput(String output, SecurityInfo securityInfo) {
        String[] lines = output.split("\\R");
        boolean volumeFound = false;
        for (String line : lines) {
            String value;
            if ((line = line.trim()).isEmpty()) continue;
            if (line.startsWith("Volume")) {
                String volume;
                int colonIndex = line.indexOf(":");
                if (colonIndex < 0 || !(volume = line.substring(colonIndex + 1).trim()).equalsIgnoreCase("C:")) continue;
                securityInfo.setBitlockerVolume("C:");
                volumeFound = true;
                continue;
            }
            if (line.startsWith("Volume Status")) {
                String value2 = this.getValueAfterColon(line);
                securityInfo.setBitlockerVolumeStatus(value2);
                if (value2 == null || value2.equalsIgnoreCase("FullyDecrypted")) continue;
                securityInfo.setBitlockerEnabled(true);
                continue;
            }
            if (line.startsWith("Percentage Encrypted")) {
                String value3 = this.getValueAfterColon(line);
                if (value3 == null) continue;
                value3 = value3.replace("%", "").trim();
                try {
                    double percentage = Double.parseDouble(value3);
                    securityInfo.setBitlockerEncryptionPct(percentage);
                }
                catch (NumberFormatException percentage) {}
                continue;
            }
            if (line.startsWith("Encryption Percentage")) {
                String value4 = this.getValueAfterColon(line);
                if (value4 == null) continue;
                value4 = value4.replace("%", "").trim();
                try {
                    double percentage = Double.parseDouble(value4);
                    securityInfo.setBitlockerEncryptionPct(percentage);
                }
                catch (NumberFormatException numberFormatException) {}
                continue;
            }
            if (line.startsWith("Encryption Method")) {
                String value5 = this.getValueAfterColon(line);
                securityInfo.setBitlockerEncryptionMethod(value5);
                continue;
            }
            if (line.startsWith("Protection Status")) {
                String value6 = this.getValueAfterColon(line);
                securityInfo.setBitlockerProtectionStatus(value6);
                continue;
            }
            if (line.startsWith("Key Protectors")) {
                String value7 = this.getValueAfterColon(line);
                if (value7 == null) continue;
                value7 = value7.replace("{", "").replace("}", "").trim();
                securityInfo.setBitlockerKeyProtector(value7);
                continue;
            }
            if (!line.startsWith("Key Protector") || (value = this.getValueAfterColon(line)) == null) continue;
            value = value.replace("{", "").replace("}", "").trim();
            securityInfo.setBitlockerKeyProtector(value);
        }
        if (securityInfo.getBitlockerEncryptionPct() != null && securityInfo.getBitlockerEncryptionPct() > 0.0) {
            securityInfo.setBitlockerEnabled(true);
        }
        if (securityInfo.getBitlockerVolumeStatus() != null && securityInfo.getBitlockerVolumeStatus().equalsIgnoreCase("FullyEncrypted")) {
            securityInfo.setBitlockerEnabled(true);
        }
        if (!volumeFound) {
            securityInfo.setBitlockerVolume("C:");
        }
    }

    private void collectSecureBoot(SecurityInfo securityInfo) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", "Confirm-SecureBootUEFI");
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line.trim());
                }
            }
            process.waitFor();
            String result = output.toString().trim();
            if (result.equalsIgnoreCase("True")) {
                securityInfo.setSecureBootEnabled(true);
            } else if (result.equalsIgnoreCase("False")) {
                securityInfo.setSecureBootEnabled(false);
            } else {
                securityInfo.setSecureBootEnabled(null);
            }
        }
        catch (Exception e) {
            System.out.println("Unable to collect Secure Boot information: " + e.getMessage());
        }
    }

    private void collectTpm(SecurityInfo securityInfo) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", "Get-Tpm | Select-Object TpmPresent,TpmReady,TpmEnabled,TpmActivated,ManufacturerVersion");
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            process.waitFor();
            this.parseTpmOutput(output.toString(), securityInfo);
        }
        catch (Exception e) {
            System.out.println("Unable to collect TPM information: " + e.getMessage());
        }
    }

    private void parseTpmOutput(String output, SecurityInfo securityInfo) {
        String[] lines;
        for (String line : lines = output.split("\\R")) {
            String value;
            if ((line = line.trim()).startsWith("TpmPresent")) {
                value = this.getValueAfterColon(line);
                securityInfo.setTpmPresent(this.parseBoolean(value));
                continue;
            }
            if (line.startsWith("TpmReady")) {
                value = this.getValueAfterColon(line);
                securityInfo.setTpmEnabled(this.parseBoolean(value));
                continue;
            }
            if (line.startsWith("TpmEnabled")) {
                value = this.getValueAfterColon(line);
                Boolean enabled = this.parseBoolean(value);
                if (enabled == null) continue;
                securityInfo.setTpmEnabled(enabled);
                continue;
            }
            if (!line.startsWith("ManufacturerVersion")) continue;
            value = this.getValueAfterColon(line);
            securityInfo.setTpmVersion(value);
        }
    }

    private String getValueAfterColon(String line) {
        int colonIndex = line.indexOf(":");
        if (colonIndex < 0) {
            return null;
        }
        String value = line.substring(colonIndex + 1).trim();
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("True")) {
            return true;
        }
        if (value.equalsIgnoreCase("False")) {
            return false;
        }
        return null;
    }
}

