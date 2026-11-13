# Asset Monitoring Agent (Silent Background Service)

This is the portable standalone version of the Asset Monitoring Agent.
No Java installation or setup is required.

## Automatic Windows Startup Registration:
- On first launch, the agent automatically registers itself in the Windows Startup folder:
  `%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\AssetMonitoringAgent.lnk`
- Whenever the employee boots/restarts Windows and logs in, the agent starts automatically in the background.
- If the folder is moved to a new path, running the executable automatically updates the Startup shortcut to the new location.
- If another instance is already running, launching the executable again safely exits to prevent duplicate processes.

## Background Execution:
- When you double-click `AssetMonitoringAgent.exe`, it runs silently as a background process.
- No terminal or command prompt window is displayed.
- It continuously collects telemetry (system name, CPU, RAM, disk, battery, network, installed software) and reports to the Asset Management server every 30 seconds.

## How to Stop the Agent:
Because there is no terminal window, the agent will only stop when:
1. Open **Task Manager** (`Ctrl + Shift + Esc`).
2. Under **Background processes** (or Details tab), find **AssetMonitoringAgent.exe**.
3. Select it and click **End task**.
(Or run from Command Prompt: `taskkill /F /IM AssetMonitoringAgent.exe`)

## Checking Logs:
While running, background execution logs are written to `agent.log` in this same folder.

## Changing Server IP / URL:
Edit `server_url.txt` in this folder to point to your backend server URL, e.g.:
https://asset-management-backend-o8dh.onrender.com/api/agent/report

## Folder Structure:
- `AssetMonitoringAgent.exe`: Silent background application launcher
- `server_url.txt`: Server endpoint configuration
- `agent.log`: Live telemetry logs (auto-generated when running)
- `README.txt`: Documentation and instructions
- `app/`: Compiled application package and configurations
- `runtime/`: Bundled lightweight Java runtime environment
