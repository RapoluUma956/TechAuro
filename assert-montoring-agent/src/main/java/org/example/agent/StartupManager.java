package org.example.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Manages Windows Startup folder registration and single-instance process locking
 * for the Asset Monitoring Agent.
 */
public class StartupManager {

    public static final String SHORTCUT_NAME = "AssetMonitoringAgent.lnk";
    private static final String LOCK_FILE_NAME_PREFIX = "asset_monitoring_agent_";

    private static FileLock instanceLock;
    private static FileChannel lockChannel;
    private static RandomAccessFile lockRaf;

    // =========================================================================
    // 1. SINGLE-INSTANCE LOCKING
    // =========================================================================

    /**
     * Attempts to acquire an exclusive OS-level file lock for the current user.
     * If an existing instance is already running, returns false.
     * When the process terminates (even abnormally or via Task Manager),
     * the operating system automatically releases the file lock.
     *
     * @return true if this process acquired the lock and may proceed; false if another instance is running
     */
    public static synchronized boolean acquireInstanceLock() {
        try {
            String username = System.getProperty("user.name", "default");
            String safeUser = username.replaceAll("[^a-zA-Z0-9_.-]", "_");
            File tempDir = new File(System.getProperty("java.io.tmpdir", "."));
            File lockFile = new File(tempDir, LOCK_FILE_NAME_PREFIX + safeUser + ".lock");

            lockRaf = new RandomAccessFile(lockFile, "rw");
            lockChannel = lockRaf.getChannel();

            try {
                instanceLock = lockChannel.tryLock();
            } catch (OverlappingFileLockException ofle) {
                instanceLock = null;
            }

            if (instanceLock == null) {
                System.out.println("[SingleInstance] Another instance of Asset Monitoring Agent is already running for user '"
                        + username + "'. Exiting duplicate process.");
                try {
                    lockChannel.close();
                    lockRaf.close();
                } catch (Exception ignored) {}
                return false;
            }

            // Write current PID to lock file for administrative diagnostics
            try {
                long pid = ProcessHandle.current().pid();
                lockFile.deleteOnExit();
                lockRaf.setLength(0);
                lockRaf.writeBytes("PID=" + pid + "\nSTARTED=" + java.time.Instant.now() + "\nUSER=" + username + "\n");
            } catch (Exception ignored) {}

            // Register shutdown hook for clean release
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (instanceLock != null && instanceLock.isValid()) {
                        instanceLock.release();
                    }
                    if (lockChannel != null && lockChannel.isOpen()) {
                        lockChannel.close();
                    }
                    if (lockRaf != null) {
                        lockRaf.close();
                    }
                } catch (Exception ignored) {}
            }));

            return true;
        } catch (Exception e) {
            System.err.println("[SingleInstance] WARNING: Could not establish instance lock: " + e.getMessage());
            // In case of unexpected I/O error on lock file, do not block the agent from running
            return true;
        }
    }

    // =========================================================================
    // 2. WINDOWS STARTUP REGISTRATION
    // =========================================================================

    /**
     * Checks and registers the Asset Monitoring Agent in the Windows Startup folder.
     * Creates or updates %APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\AssetMonitoringAgent.lnk.
     *
     * @return true if registered or already up-to-date, false if skipped or failed
     */
    public static boolean registerStartup() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            System.out.println("[StartupManager] Non-Windows operating system detected (" + os + "). Startup registration skipped.");
            return false;
        }

        System.out.println();
        System.out.println("[StartupManager] ----------------------------------------------");
        System.out.println("[StartupManager] Checking Windows Startup registration...");

        try {
            // A. Detect actual application executable
            File exeFile = detectApplicationExecutable();
            if (exeFile == null || !exeFile.exists()) {
                System.out.println("[StartupManager] Development environment detected (no standalone launcher .exe found).");
                System.out.println("[StartupManager] Automatic Windows startup shortcut will be created when packaged as .exe.");
                System.out.println("[StartupManager] ----------------------------------------------");
                return false;
            }

            System.out.println("[StartupManager] Application executable detected: " + exeFile.getAbsolutePath());
            File workingDir = exeFile.getParentFile();
            System.out.println("[StartupManager] Application working directory:  " + (workingDir != null ? workingDir.getAbsolutePath() : "—"));

            // B. Detect Windows Startup folder
            File startupFolder = getWindowsStartupFolder();
            if (startupFolder == null || !startupFolder.exists()) {
                System.err.println("[StartupManager] ERROR: Unable to locate or access Windows Startup directory.");
                System.out.println("[StartupManager] ----------------------------------------------");
                return false;
            }

            System.out.println("[StartupManager] Windows Startup directory:      " + startupFolder.getAbsolutePath());
            File shortcutFile = new File(startupFolder, SHORTCUT_NAME);

            // C. Check existing shortcut to avoid duplicate or re-creating identical shortcuts
            if (shortcutFile.exists()) {
                System.out.println("[StartupManager] Existing startup shortcut found at: " + shortcutFile.getAbsolutePath());
                ShortcutInfo existingInfo = inspectShortcut(shortcutFile);

                if (existingInfo != null && isMatchingPath(existingInfo.targetPath, exeFile.getAbsolutePath())
                        && (workingDir == null || isMatchingPath(existingInfo.workingDirectory, workingDir.getAbsolutePath()))) {
                    System.out.println("[StartupManager] Startup shortcut is already up-to-date and points to active executable.");
                    System.out.println("[StartupManager] Target: " + existingInfo.targetPath);
                    System.out.println("[StartupManager] Working directory: " + existingInfo.workingDirectory);
                    System.out.println("[StartupManager] No changes needed.");
                    System.out.println("[StartupManager] ----------------------------------------------");
                    return true;
                }

                if (existingInfo != null && !existingInfo.targetPath.isBlank()) {
                    System.out.println("[StartupManager] Outdated startup shortcut detected!");
                    System.out.println("[StartupManager] Previous target:  " + existingInfo.targetPath);
                    System.out.println("[StartupManager] New active target: " + exeFile.getAbsolutePath());
                } else {
                    System.out.println("[StartupManager] Existing shortcut could not be read or is invalid. Re-creating...");
                }
            } else {
                System.out.println("[StartupManager] No existing startup shortcut found. Creating initial registration...");
            }

            // D. Create or update shortcut
            boolean success = createOrUpdateShortcut(shortcutFile, exeFile, workingDir);
            if (success && shortcutFile.exists() && shortcutFile.length() > 0) {
                System.out.println("[StartupManager] SUCCESS: Windows startup shortcut registered successfully.");
                System.out.println("[StartupManager] Shortcut file:  " + shortcutFile.getAbsolutePath());
                System.out.println("[StartupManager] Target command: " + exeFile.getAbsolutePath());
                System.out.println("[StartupManager] The agent will now launch automatically on Windows login.");
            } else {
                System.err.println("[StartupManager] WARNING: Shortcut creation process completed without verifying .lnk file.");
            }
            System.out.println("[StartupManager] ----------------------------------------------");
            return success;

        } catch (Exception e) {
            System.err.println("[StartupManager] ERROR during startup registration: " + e.getMessage());
            e.printStackTrace();
            System.out.println("[StartupManager] ----------------------------------------------");
            // Non-fatal: do not crash the agent
            return false;
        }
    }

    // =========================================================================
    // 3. EXECUTABLE AND FOLDER DETECTION
    // =========================================================================

    /**
     * Resolves the actual standalone application launcher executable (AssetMonitoringAgent.exe / Agent.exe).
     * Never returns java.exe or javaw.exe.
     *
     * @return File pointing to the launcher executable, or null if running in raw dev mode
     */
    public static File detectApplicationExecutable() {
        // 1. ProcessHandle.current().info().command() - In Java 9+, returns the native launcher
        try {
            Optional<String> cmdOpt = ProcessHandle.current().info().command();
            if (cmdOpt.isPresent()) {
                String cmd = cmdOpt.get();
                File f = new File(cmd);
                String name = f.getName().toLowerCase();
                if (f.exists() && f.isFile() && name.endsWith(".exe")
                        && !name.equals("java.exe") && !name.equals("javaw.exe")) {
                    return toCanonical(f);
                }
            }
        } catch (Exception ignored) {}

        // 2. jpackage layout: System.getProperty("java.home") is <app_dir>\runtime
        try {
            String javaHome = System.getProperty("java.home");
            if (javaHome != null) {
                File runtimeDir = new File(javaHome);
                File appDir = runtimeDir.getParentFile();
                if (appDir != null && appDir.isDirectory()) {
                    File exe = searchForLauncher(appDir);
                    if (exe != null) return toCanonical(exe);
                }
            }
        } catch (Exception ignored) {}

        // 3. JAR location: <app_dir>\app\asset-monitoring-agent-1.0-SNAPSHOT.jar
        try {
            File jarFile = new File(StartupManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File parent = jarFile.getParentFile();
            if (parent != null && parent.getName().equalsIgnoreCase("app")) {
                File appDir = parent.getParentFile();
                if (appDir != null && appDir.isDirectory()) {
                    File exe = searchForLauncher(appDir);
                    if (exe != null) return toCanonical(exe);
                }
            }
        } catch (Exception ignored) {}

        // 4. Current working directory fallback
        try {
            File cwd = new File(".").getAbsoluteFile();
            File exe = searchForLauncher(cwd);
            if (exe != null) return toCanonical(exe);
        } catch (Exception ignored) {}

        return null;
    }

    private static File toCanonical(File f) {
        if (f == null) return null;
        try {
            return f.getCanonicalFile();
        } catch (Exception e) {
            return f.getAbsoluteFile();
        }
    }

    /**
     * Looks for known launcher names (AssetMonitoringAgent.exe, Agent.exe) or any non-java .exe in a directory.
     */
    private static File searchForLauncher(File dir) {
        if (dir == null || !dir.isDirectory()) return null;

        File primary = new File(dir, "AssetMonitoringAgent.exe");
        if (primary.exists() && primary.isFile()) return primary;

        File secondary = new File(dir, "Agent.exe");
        if (secondary.exists() && secondary.isFile()) return secondary;

        File[] exes = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".exe") && !lower.equals("java.exe") && !lower.equals("javaw.exe");
        });
        if (exes != null && exes.length > 0) {
            return exes[0];
        }

        return null;
    }

    /**
     * Returns the Windows Startup directory for the currently logged-in user.
     * %APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
     */
    public static File getWindowsStartupFolder() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                File fallback = new File(userHome, "AppData\\Roaming");
                if (fallback.exists()) {
                    appData = fallback.getAbsolutePath();
                }
            }
        }

        if (appData == null || appData.isBlank()) {
            return null;
        }

        File startup = new File(appData, "Microsoft\\Windows\\Start Menu\\Programs\\Startup");
        if (!startup.exists()) {
            startup.mkdirs();
        }
        return startup;
    }

    // =========================================================================
    // 4. SHORTCUT CREATION & INSPECTION VIA WINDOWS SCRIPT HOST (POWERSHELL)
    // =========================================================================

    public static class ShortcutInfo {
        public final String targetPath;
        public final String workingDirectory;

        public ShortcutInfo(String targetPath, String workingDirectory) {
            this.targetPath = targetPath != null ? targetPath.trim() : "";
            this.workingDirectory = workingDirectory != null ? workingDirectory.trim() : "";
        }
    }

    /**
     * Reads target and working directory of an existing .lnk shortcut via WScript.Shell.
     */
    public static ShortcutInfo inspectShortcut(File shortcutFile) {
        if (shortcutFile == null || !shortcutFile.exists()) return null;

        String safeLnkPath = escapeForPowerShell(shortcutFile.getAbsolutePath());
        String script = "$ws = New-Object -ComObject WScript.Shell; "
                + "$s = $ws.CreateShortcut('" + safeLnkPath + "'); "
                + "[Console]::WriteLine('TARGET:' + $s.TargetPath); "
                + "[Console]::WriteLine('WORKDIR:' + $s.WorkingDirectory);";

        try {
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            String targetPath = null;
            String workingDir = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("TARGET:")) {
                        targetPath = line.substring("TARGET:".length()).trim();
                    } else if (line.startsWith("WORKDIR:")) {
                        workingDir = line.substring("WORKDIR:".length()).trim();
                    }
                }
            }
            p.waitFor();

            if (targetPath != null) {
                return new ShortcutInfo(targetPath, workingDir != null ? workingDir : "");
            }
        } catch (Exception e) {
            System.err.println("[StartupManager] Notice: Could not inspect existing shortcut: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates or overwrites a Windows .lnk shortcut pointing to targetExe with workingDir.
     */
    public static boolean createOrUpdateShortcut(File shortcutFile, File targetExe, File workingDir) {
        if (shortcutFile == null || targetExe == null) return false;

        String safeLnkPath = escapeForPowerShell(shortcutFile.getAbsolutePath());
        String safeExePath = escapeForPowerShell(targetExe.getAbsolutePath());
        String safeWorkDir = workingDir != null ? escapeForPowerShell(workingDir.getAbsolutePath()) : "";

        String script = "$ws = New-Object -ComObject WScript.Shell; "
                + "$s = $ws.CreateShortcut('" + safeLnkPath + "'); "
                + "$s.TargetPath = '" + safeExePath + "'; "
                + (safeWorkDir.isBlank() ? "" : "$s.WorkingDirectory = '" + safeWorkDir + "'; ")
                + "$s.Description = 'Asset Monitoring Agent Background Telemetry Service'; "
                + "$s.Save();";

        try {
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isBlank()) {
                        System.out.println("[StartupManager/PowerShell] " + line);
                    }
                }
            }
            int exitCode = p.waitFor();
            return exitCode == 0 && shortcutFile.exists();
        } catch (Exception e) {
            System.err.println("[StartupManager] ERROR creating shortcut via PowerShell: " + e.getMessage());
            return false;
        }
    }

    private static String escapeForPowerShell(String str) {
        if (str == null) return "";
        return str.replace("'", "''");
    }

    private static boolean isMatchingPath(String path1, String path2) {
        if (path1 == null || path2 == null) return false;
        try {
            File f1 = new File(path1).getCanonicalFile();
            File f2 = new File(path2).getCanonicalFile();
            return f1.equals(f2);
        } catch (Exception e) {
            return path1.trim().equalsIgnoreCase(path2.trim());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== StartupManager Diagnostics ===");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("User: " + System.getProperty("user.name"));
        System.out.println("Detected Exe: " + detectApplicationExecutable());
        System.out.println("Startup Folder: " + getWindowsStartupFolder());
        boolean registered = registerStartup();
        System.out.println("Registration Result: " + registered);
    }
}
