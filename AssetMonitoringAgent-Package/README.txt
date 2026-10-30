# Asset Monitoring Agent (Standalone)

This is the portable standalone version of the Asset Monitoring Agent.
No Java installation or setup is required.

## How to Run:
1. Double-click AssetMonitoringAgent.exe or run it from Command Prompt / PowerShell:
   .\AssetMonitoringAgent.exe

The agent will automatically collect system telemetry (using the computer's system name)
and stream reports to the Asset Management server.

## Folder Structure:
- AssetMonitoringAgent.exe: Application launcher
- pp/: Compiled application package and configuration
- untime/: Bundled lightweight Java runtime environment
