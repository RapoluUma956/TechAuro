package org.example.agent.collector;

import org.example.agent.model.SecurityInfo;
import oshi.SystemInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SecurityCollector {

    private final SystemInfo systemInfo;

    public SecurityCollector() {
        this.systemInfo = new SystemInfo();
    }

    public SecurityInfo collect() {

        SecurityInfo securityInfo = new SecurityInfo();

        collectBitLocker(securityInfo);
        collectSecureBoot(securityInfo);
        collectTpm(securityInfo);

        return securityInfo;
    }

    // =========================================================
    // BITLOCKER
    // =========================================================

    private void collectBitLocker(SecurityInfo securityInfo) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "manage-bde.exe",
                            "-status",
                            "C:"
                    );

            Process process =
                    processBuilder.start();

            StringBuilder output =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    output.append(line)
                            .append(System.lineSeparator());
                }
            }

            process.waitFor();

            String result =
                    output.toString();

            parseBitLockerOutput(
                    result,
                    securityInfo
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to collect BitLocker information: "
                            + e.getMessage()
            );
        }
    }

    private void parseBitLockerOutput(
            String output,
            SecurityInfo securityInfo) {

        String[] lines =
                output.split("\\R");

        boolean volumeFound = false;

        for (String line : lines) {

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            // ---------------------------------------------
            // Volume
            // ---------------------------------------------

            if (line.startsWith("Volume")) {

                int colonIndex =
                        line.indexOf(":");

                if (colonIndex >= 0) {

                    String volume =
                            line.substring(
                                    colonIndex + 1
                            ).trim();

                    if (volume.equalsIgnoreCase("C:")) {

                        securityInfo.setBitlockerVolume(
                                "C:"
                        );

                        volumeFound = true;
                    }
                }
            }

            // ---------------------------------------------
            // Volume Status
            // ---------------------------------------------

            else if (line.startsWith("Volume Status")) {

                String value =
                        getValueAfterColon(line);

                securityInfo.setBitlockerVolumeStatus(
                        value
                );

                if (value != null
                        && !value.equalsIgnoreCase(
                        "FullyDecrypted")) {

                    securityInfo.setBitlockerEnabled(
                            true
                    );
                }
            }

            // ---------------------------------------------
            // Encryption Percentage
            // ---------------------------------------------

            else if (line.startsWith(
                    "Percentage Encrypted")) {

                String value =
                        getValueAfterColon(line);

                if (value != null) {

                    value =
                            value.replace(
                                    "%",
                                    ""
                            ).trim();

                    try {

                        double percentage =
                                Double.parseDouble(
                                        value
                                );

                        securityInfo
                                .setBitlockerEncryptionPct(
                                        percentage
                                );

                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Some versions of Windows may expose:
            // Encryption Percentage
            else if (line.startsWith(
                    "Encryption Percentage")) {

                String value =
                        getValueAfterColon(line);

                if (value != null) {

                    value =
                            value.replace(
                                    "%",
                                    ""
                            ).trim();

                    try {

                        double percentage =
                                Double.parseDouble(
                                        value
                                );

                        securityInfo
                                .setBitlockerEncryptionPct(
                                        percentage
                                );

                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // ---------------------------------------------
            // Encryption Method
            // ---------------------------------------------

            else if (line.startsWith(
                    "Encryption Method")) {

                String value =
                        getValueAfterColon(line);

                securityInfo.setBitlockerEncryptionMethod(
                        value
                );
            }

            // ---------------------------------------------
            // Protection Status
            // ---------------------------------------------

            else if (line.startsWith(
                    "Protection Status")) {

                String value =
                        getValueAfterColon(line);

                securityInfo.setBitlockerProtectionStatus(
                        value
                );
            }

            // ---------------------------------------------
            // Key Protectors
            // ---------------------------------------------

            else if (line.startsWith(
                    "Key Protectors")) {

                String value =
                        getValueAfterColon(line);

                if (value != null) {

                    value =
                            value.replace(
                                            "{",
                                            ""
                                    )
                                    .replace(
                                            "}",
                                            ""
                                    )
                                    .trim();

                    securityInfo.setBitlockerKeyProtector(
                            value
                    );
                }
            }

            // Some Windows output may use singular form.
            else if (line.startsWith(
                    "Key Protector")) {

                String value =
                        getValueAfterColon(line);

                if (value != null) {

                    value =
                            value.replace(
                                            "{",
                                            ""
                                    )
                                    .replace(
                                            "}",
                                            ""
                                    )
                                    .trim();

                    securityInfo.setBitlockerKeyProtector(
                            value
                    );
                }
            }
        }

        // -----------------------------------------------------
        // Determine whether BitLocker is enabled
        // -----------------------------------------------------

        if (securityInfo.getBitlockerEncryptionPct()
                != null
                && securityInfo
                .getBitlockerEncryptionPct() > 0) {

            securityInfo.setBitlockerEnabled(
                    true
            );
        }

        if (securityInfo.getBitlockerVolumeStatus()
                != null
                && securityInfo
                .getBitlockerVolumeStatus()
                .equalsIgnoreCase(
                        "FullyEncrypted")) {

            securityInfo.setBitlockerEnabled(
                    true
            );
        }

        if (!volumeFound) {

            securityInfo.setBitlockerVolume(
                    "C:"
            );
        }
    }

    // =========================================================
    // SECURE BOOT
    // =========================================================

    private void collectSecureBoot(
            SecurityInfo securityInfo) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-NonInteractive",
                            "-Command",
                            "Confirm-SecureBootUEFI"
                    );

            Process process =
                    processBuilder.start();

            StringBuilder output =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    output.append(line.trim());
                }
            }

            process.waitFor();

            String result =
                    output.toString().trim();

            if (result.equalsIgnoreCase("True")) {

                securityInfo.setSecureBootEnabled(
                        true
                );

            } else if (result.equalsIgnoreCase("False")) {

                securityInfo.setSecureBootEnabled(
                        false
                );

            } else {

                securityInfo.setSecureBootEnabled(
                        null
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to collect Secure Boot information: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // TPM
    // =========================================================

    private void collectTpm(
            SecurityInfo securityInfo) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-NonInteractive",
                            "-Command",
                            "Get-Tpm | " +
                                    "Select-Object " +
                                    "TpmPresent," +
                                    "TpmReady," +
                                    "TpmEnabled," +
                                    "TpmActivated," +
                                    "ManufacturerVersion"
                    );

            Process process =
                    processBuilder.start();

            StringBuilder output =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    output.append(line)
                            .append(System.lineSeparator());
                }
            }

            process.waitFor();

            parseTpmOutput(
                    output.toString(),
                    securityInfo
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to collect TPM information: "
                            + e.getMessage()
            );
        }
    }

    private void parseTpmOutput(
            String output,
            SecurityInfo securityInfo) {

        String[] lines =
                output.split("\\R");

        for (String line : lines) {

            line = line.trim();

            if (line.startsWith("TpmPresent")) {

                String value =
                        getValueAfterColon(line);

                securityInfo.setTpmPresent(
                        parseBoolean(value)
                );
            }

            else if (line.startsWith("TpmReady")) {

                String value =
                        getValueAfterColon(line);

                securityInfo.setTpmEnabled(
                        parseBoolean(value)
                );
            }

            else if (line.startsWith(
                    "TpmEnabled")) {

                String value =
                        getValueAfterColon(line);

                Boolean enabled =
                        parseBoolean(value);

                if (enabled != null) {

                    securityInfo.setTpmEnabled(
                            enabled
                    );
                }
            }

            else if (line.startsWith(
                    "ManufacturerVersion")) {

                String value =
                        getValueAfterColon(line);

                securityInfo.setTpmVersion(
                        value
                );
            }
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String getValueAfterColon(
            String line) {

        int colonIndex =
                line.indexOf(":");

        if (colonIndex < 0) {
            return null;
        }

        String value =
                line.substring(
                        colonIndex + 1
                ).trim();

        if (value.isEmpty()) {
            return null;
        }

        return value;
    }

    private Boolean parseBoolean(
            String value) {

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