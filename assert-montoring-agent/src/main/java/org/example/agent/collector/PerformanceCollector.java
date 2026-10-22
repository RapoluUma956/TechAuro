package org.example.agent.collector;

import org.example.agent.model.PerformanceInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PerformanceCollector {

    public PerformanceInfo collect() {

        System.out.println();
        System.out.println("======================================");
        System.out.println("       PERFORMANCE COLLECTION");
        System.out.println("======================================");

        PerformanceInfo performance =
                new PerformanceInfo();

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-Command",
                            "(Get-Process).Count"
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String processOutput;

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {
                processOutput = reader.readLine();
            }

            process.waitFor();

            int processCount =
                    Integer.parseInt(
                            processOutput.trim()
                    );

            performance.setProcessCount(
                    processCount
            );

            // --------------------------------------------------------
            // THREAD COUNT
            // --------------------------------------------------------

            ProcessBuilder threadProcessBuilder =
                    new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-Command",
                            """
                            $total = 0

                            Get-Process | ForEach-Object {
                                try {
                                    $total += $_.Threads.Count
                                }
                                catch {
                                }
                            }

                            Write-Output $total
                            """
                    );

            threadProcessBuilder.redirectErrorStream(true);

            Process threadProcess =
                    threadProcessBuilder.start();

            String threadOutput;

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            threadProcess.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {
                threadOutput = reader.readLine();
            }

            threadProcess.waitFor();

            int threadCount =
                    Integer.parseInt(
                            threadOutput.trim()
                    );

            performance.setThreadCount(
                    threadCount
            );

        } catch (Exception e) {

            System.out.println(
                    "Performance collection failed."
            );

            e.printStackTrace();
        }

        System.out.println(
                "Process Count : " +
                        performance.getProcessCount()
        );

        System.out.println(
                "Thread Count  : " +
                        performance.getThreadCount()
        );

        System.out.println(
                "======================================"
        );

        return performance;
    }
}