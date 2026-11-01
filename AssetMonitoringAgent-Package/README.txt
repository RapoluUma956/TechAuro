# Asset Monitoring Agent (Standalone)

This is the portable standalone version of the Asset Monitoring Agent.
No Java installation or setup is required.

## How to Run:
1. Double-click AssetMonitoringAgent.exe or run it from Command Prompt / PowerShell:
   .\AssetMonitoringAgent.exe

The agent will automatically collect system telemetry (using the computer's system name)
and stream reports to the Asset Management server.

## Changing Server IP / URL:
Edit server_url.txt in this folder and replace with your backend server URL, e.g.:
http://192.168.254.135:8080/api/agent/report
or
http://172.16.39.244:8080/api/agent/report

## Folder Structure:
- AssetMonitoringAgent.exe: Application launcher
- server_url.txt: Optional target server URL configuration
- pp/: Compiled application package and configuration
- untime/: Bundled lightweight Java runtime environment
