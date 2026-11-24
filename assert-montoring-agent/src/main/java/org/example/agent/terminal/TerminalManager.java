package org.example.agent.terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Manages an interactive local Windows shell process (PowerShell or CMD)
 * using Java's standard ProcessBuilder.
 */
public class TerminalManager {

    private final String shellType;
    private final Consumer<String> outputCallback;
    private final Runnable onProcessExit;

    private Process process;
    private OutputStream processInput;
    private Thread outputReaderThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TerminalManager(String shellType, Consumer<String> outputCallback, Runnable onProcessExit) {
        this.shellType = (shellType != null && "cmd".equalsIgnoreCase(shellType)) ? "cmd" : "powershell";
        this.outputCallback = outputCallback;
        this.onProcessExit = onProcessExit;
    }

    public synchronized boolean start() {
        if (running.get()) {
            return true;
        }

        try {
            ProcessBuilder pb;
            if ("cmd".equals(shellType)) {
                pb = new ProcessBuilder("cmd.exe", "/Q", "/K");
            } else {
                pb = new ProcessBuilder(
                        "powershell.exe",
                        "-NoLogo",
                        "-NoExit",
                        "-ExecutionPolicy", "Bypass",
                        "-Command", "-"
                );
            }

            // Merge stdout and stderr so admin sees errors inline
            pb.redirectErrorStream(true);

            // Start in user home directory
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                pb.directory(new java.io.File(userHome));
            }

            process = pb.start();
            processInput = process.getOutputStream();
            running.set(true);

            // Dedicated async reader thread
            outputReaderThread = new Thread(this::readProcessOutput, "TerminalOutputReader-" + shellType);
            outputReaderThread.setDaemon(true);
            outputReaderThread.start();

            // Process exit monitor thread
            Thread exitMonitorThread = new Thread(() -> {
                try {
                    process.waitFor();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    running.set(false);
                    if (onProcessExit != null) {
                        onProcessExit.run();
                    }
                }
            }, "TerminalExitMonitor-" + shellType);
            exitMonitorThread.setDaemon(true);
            exitMonitorThread.start();

            return true;
        } catch (Exception e) {
            System.err.println("[TerminalManager] Failed to start shell process: " + e.getMessage());
            e.printStackTrace();
            running.set(false);
            return false;
        }
    }

    public void writeInput(String input) {
        if (!running.get() || processInput == null || input == null) {
            return;
        }

        try {
            // Write input bytes using system default or UTF-8 charset
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            processInput.write(bytes);
            processInput.flush();
        } catch (Exception e) {
            System.err.println("[TerminalManager] Error writing input to shell: " + e.getMessage());
        }
    }

    public synchronized void close() {
        running.set(false);

        if (processInput != null) {
            try {
                processInput.close();
            } catch (Exception ignored) {}
            processInput = null;
        }

        if (process != null && process.isAlive()) {
            try {
                process.destroyForcibly();
            } catch (Exception ignored) {}
            process = null;
        }

        if (outputReaderThread != null && outputReaderThread.isAlive()) {
            outputReaderThread.interrupt();
            outputReaderThread = null;
        }
    }

    public boolean isRunning() {
        return running.get() && process != null && process.isAlive();
    }

    private void readProcessOutput() {
        byte[] buffer = new byte[2048];
        Charset charset = StandardCharsets.UTF_8;

        try (InputStream in = process.getInputStream()) {
            int bytesRead;
            while (running.get() && (bytesRead = in.read(buffer)) != -1) {
                if (bytesRead > 0) {
                    String text = new String(buffer, 0, bytesRead, charset);
                    if (outputCallback != null) {
                        outputCallback.accept(text);
                    }
                }
            }
        } catch (Exception e) {
            if (running.get()) {
                System.err.println("[TerminalManager] Output reader error: " + e.getMessage());
            }
        }
    }
}

