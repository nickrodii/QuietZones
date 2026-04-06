# Quiet Zones

Quiet Zones is a simple Minecraft server plugin that mutes entity sounds inside zones you define.

## Requirements

- Minecraft server: Spigot, Paper, or Purpur
- Supported API target: 1.17+
- Java: use the Java version required by your server version

## Install

1. Build the plugin:

```powershell
mvn package
```

2. Copy the jar from `target/` into your server's `plugins/` folder.
3. Start or restart the server.

## Permission

- `quietzones.manage` - allows use of `/qz`

Players without this permission are still affected by quiet zones. They just cannot manage them.

## Commands

- `/qz`
- `/qz zone create <id>`
- `/qz zone remove <id>`
- `/qz zone <id> set`
- `/qz zone <id> enable`
- `/qz zone <id> disable`
- `/qz pos1 [x] [z]`
- `/qz pos2 [x] [z]`
- `/qz exit`

## Quick Setup

1. Run `/qz zone create farm`
2. Run `/qz zone farm set`
3. Stand at one corner and run `/qz pos1`
4. Stand at the opposite corner and run `/qz pos2`

Mobs inside that zone will be muted while the zone is enabled.
