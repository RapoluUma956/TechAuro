/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.example.agent.model.BiosInfo;

public class BiosCollector {
    public BiosInfo collect() {
        BiosInfo biosInfo = new BiosInfo();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", "$bios = Get-CimInstance Win32_BIOS | Select-Object -First 1; [PSCustomObject]@{Manufacturer=$bios.Manufacturer;Version=$bios.SMBIOSBIOSVersion;ReleaseDate=$bios.ReleaseDate.ToString('yyyy-MM-dd')} | ConvertTo-Json -Compress");
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            process.waitFor();
            String result = output.toString().trim();
            if (!result.isEmpty()) {
                String releaseDate;
                String version;
                String manufacturer = this.extractStringValue(result, "Manufacturer");
                if (manufacturer != null && !manufacturer.isBlank()) {
                    biosInfo.setManufacturer(manufacturer);
                }
                if ((version = this.extractStringValue(result, "Version")) != null && !version.isBlank()) {
                    biosInfo.setVersion(version);
                }
                if ((releaseDate = this.extractStringValue(result, "ReleaseDate")) != null && !releaseDate.isBlank()) {
                    try {
                        biosInfo.setReleaseDate(LocalDate.parse(releaseDate));
                    }
                    catch (Exception e) {
                        System.out.println("Unable to parse BIOS release date: " + releaseDate);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Unable to collect BIOS information: " + e.getMessage());
        }
        if (biosInfo.getManufacturer() == null) {
            biosInfo.setManufacturer("UNKNOWN");
        }
        if (biosInfo.getVersion() == null) {
            biosInfo.setVersion("UNKNOWN");
        }
        return biosInfo;
    }

    private String extractStringValue(String json, String propertyName) {
        try {
            String search = "\"" + propertyName + "\"";
            int propertyIndex = json.indexOf(search);
            if (propertyIndex < 0) {
                return null;
            }
            int colonIndex = json.indexOf(":", propertyIndex);
            if (colonIndex < 0) {
                return null;
            }
            int firstQuote = json.indexOf("\"", colonIndex + 1);
            if (firstQuote < 0) {
                return null;
            }
            int secondQuote = json.indexOf("\"", firstQuote + 1);
            if (secondQuote < 0) {
                return null;
            }
            return json.substring(firstQuote + 1, secondQuote);
        }
        catch (Exception e) {
            return null;
        }
    }
}

