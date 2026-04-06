# Quiet Zones

Quiet Zones is a very simple Minecraft plugin for all versions 1.17+ that allows players with proper permissions to specify different zones to mute entity noises, making mob farms silent and no longer disruptive.

## Compatibility

- For Bukkit, Spigot, Paper, and Purpur servers
- Supports all versions 1.17+

## Install

Find the latest download from the [plugin's Modrinth page](https://modrinth.com/plugin/quiet-zones/versions) and place the plugin in your servers /plugins folder.

## Permission

- `quietzones.manage` - allows use of `/qz` to create, resize, disable, and enable quiet zones.

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

Entities inside that zone will be muted while the zone is enabled.
