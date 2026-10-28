/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.example.agent.model.PerformanceInfo;

public class PerformanceCollector {
    public PerformanceInfo collect() {
        System.out.println();
        System.out.println("======================================");
        System.out.println("       PERFORMANCE COLLECTION");
        System.out.println("======================================");
        PerformanceInfo performance = new PerformanceInfo();
        try {
            String threadOutput;
            String processOutput;
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "(Get-Process).Count");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                processOutput = reader.readLine();
            }
            process.waitFor();
            int processCount = Integer.parseInt(processOutput.trim());
            performance.setProcessCount(processCount);
            ProcessBuilder threadProcessBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "$total = 0\n\nGet-Process | ForEach-Object {\n    try {\n        $total += $_.Threads.Count\n    }\n    catch {\n    }\n}\n\nWrite-Output $total\n");
            threadProcessBuilder.redirectErrorStream(true);
            Process threadProcess = threadProcessBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(threadProcess.getInputStream(), StandardCharsets.UTF_8));){
                threadOutput = reader.readLine();
            }
            threadProcess.waitFor();
            int threadCount = Integer.parseInt(threadOutput.trim());
            performance.setThreadCount(threadCount);
        }
        catch (Exception e) {
            System.out.println("Performance collection failed.");
            e.printStackTrace();
        }
        System.out.println("Process Count : " + performance.getProcessCount());
        System.out.println("Thread Count  : " + performance.getThreadCount());
        System.out.println("======================================");
        return performance;
    }
}

