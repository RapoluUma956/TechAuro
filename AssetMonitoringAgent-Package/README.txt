# Asset Monitoring Agent (Silent Background Service)

This is the portable standalone version of the Asset Monitoring Agent.
No Java installation or setup is required.

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
http://192.168.140.127:8080/api/agent/report

## Folder Structure:
- `AssetMonitoringAgent.exe`: Silent background application launcher
- `server_url.txt`: Server endpoint configuration
- `agent.log`: Live telemetry logs (auto-generated when running)
- `app/`: Compiled application package and configurations
- `runtime/`: Bundled lightweight Java runtime environment
