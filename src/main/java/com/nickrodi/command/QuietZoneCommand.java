package com.nickrodi.command;

import com.nickrodi.mute.ZoneMuteService;
import com.nickrodi.zone.QuietZone;
import com.nickrodi.zone.ZoneManager;
import com.nickrodi.zone.ZonePoint;
import com.nickrodi.zone.ZoneSelectionManager;
import com.nickrodi.zone.ZoneSelectionSession;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class QuietZoneCommand implements CommandExecutor, TabCompleter {
    private static final Pattern ZONE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final String ADMIN_PERMISSION = "smpquietzones.admin";

    private final ZoneManager zoneManager;
    private final ZoneSelectionManager selectionManager;
    private final ZoneMuteService zoneMuteService;

    public QuietZoneCommand(ZoneManager zoneManager, ZoneSelectionManager selectionManager, ZoneMuteService zoneMuteService) {
        this.zoneManager = zoneManager;
        this.selectionManager = selectionManager;
        this.zoneMuteService = zoneMuteService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String root = args[0].toLowerCase(Locale.ROOT);
        switch (root) {
            case "zone" -> handleZone(sender, args);
            case "pos1" -> handlePosition(sender, args, true);
            case "pos2" -> handlePosition(sender, args, false);
            case "exit" -> handleExit(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filterByPrefix(Arrays.asList("zone", "pos1", "pos2", "exit"), args[0]);
        }

        if (args[0].equalsIgnoreCase("zone")) {
            if (args.length == 2) {
                List<String> options = new ArrayList<>();
                options.add("create");
                options.add("remove");
                options.addAll(zoneManager.getZoneIds());
                return filterByPrefix(options, args[1]);
            }

            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("remove")) {
                    return filterByPrefix(zoneManager.getZoneIds(), args[2]);
                }
                if (!args[1].equalsIgnoreCase("create")) {
                    return filterByPrefix(Arrays.asList("set", "enable", "disable"), args[2]);
                }
            }
        }

        return Collections.emptyList();
    }

    private void handleZone(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendZoneHelp(sender);
            return;
        }

        String second = args[1].toLowerCase(Locale.ROOT);
        switch (second) {
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            default -> handleZoneAction(sender, args);
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("Usage: /qz zone create <id>");
            return;
        }

        String zoneId = args[2];
        if (!isValidZoneId(zoneId)) {
            sender.sendMessage("Invalid id. Use only letters, numbers, _ and -.");
            return;
        }

        if (!zoneManager.createZone(zoneId)) {
            sender.sendMessage("Zone '" + QuietZone.normalizeId(zoneId) + "' already exists.");
            return;
        }

        sender.sendMessage("Zone '" + QuietZone.normalizeId(zoneId) + "' created.");
        sender.sendMessage("Set coordinates with: /qz zone " + QuietZone.normalizeId(zoneId) + " set");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("Usage: /qz zone remove <id>");
            return;
        }

        String zoneId = QuietZone.normalizeId(args[2]);
        if (!zoneManager.removeZone(zoneId)) {
            sender.sendMessage("Zone '" + zoneId + "' does not exist.");
            return;
        }

        selectionManager.clearZone(zoneId);
        zoneMuteService.refreshNow();
        sender.sendMessage("Zone '" + zoneId + "' removed.");
    }

    private void handleZoneAction(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sendZoneHelp(sender);
            return;
        }

        String zoneId = QuietZone.normalizeId(args[1]);
        String action = args[2].toLowerCase(Locale.ROOT);
        QuietZone zone = zoneManager.getZone(zoneId);
        if (zone == null) {
            sender.sendMessage("Zone '" + zoneId + "' does not exist.");
            return;
        }

        switch (action) {
            case "set" -> handleSet(sender, zone);
            case "enable" -> {
                if (!zoneManager.setEnabled(zoneId, true)) {
                    sender.sendMessage("Could not enable zone '" + zoneId + "'.");
                    return;
                }
                zoneMuteService.refreshNow();
                sender.sendMessage("Zone '" + zoneId + "' is now enabled.");
            }
            case "disable" -> {
                if (!zoneManager.setEnabled(zoneId, false)) {
                    sender.sendMessage("Could not disable zone '" + zoneId + "'.");
                    return;
                }
                zoneMuteService.refreshNow();
                sender.sendMessage("Zone '" + zoneId + "' is now disabled.");
            }
            default -> sendZoneHelp(sender);
        }
    }

    private void handleSet(CommandSender sender, QuietZone zone) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /qz zone <id> set.");
            return;
        }

        String zoneId = zone.getId();
        String worldName = player.getWorld().getName();
        selectionManager.start(player.getUniqueId(), zoneId, worldName);

        player.sendMessage("Selection mode started for zone '" + zoneId + "' in world '" + worldName + "'.");
        player.sendMessage("Use /qz pos1 to set first corner from where you stand.");
        player.sendMessage("Or use /qz pos1 <x> <z> to set coordinates directly.");
        player.sendMessage("Then set second corner with /qz pos2 (or /qz pos2 <x> <z>).");
        player.sendMessage("Use /qz exit to leave selection mode.");
    }

    private void handlePosition(CommandSender sender, String[] args, boolean firstPosition) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can set zone positions.");
            return;
        }

        ZoneSelectionSession session = selectionManager.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage("You are not in selection mode. Use /qz zone <id> set first.");
            return;
        }

        if (args.length == 1 && !player.getWorld().getName().equals(session.getWorldName())) {
            player.sendMessage("You started selection in world '" + session.getWorldName() + "'.");
            player.sendMessage("Return to that world or use /qz pos1 <x> <z> and /qz pos2 <x> <z>.");
            return;
        }

        ZonePoint point = parsePoint(player.getLocation(), args);
        if (point == null) {
            String commandName = firstPosition ? "pos1" : "pos2";
            player.sendMessage("Usage: /qz " + commandName + " [x] [z]");
            return;
        }

        if (firstPosition) {
            session.setPos1(point);
            player.sendMessage("Position 1 set to X=" + trim(point.x()) + ", Z=" + trim(point.z()) + ".");
        } else {
            session.setPos2(point);
            player.sendMessage("Position 2 set to X=" + trim(point.x()) + ", Z=" + trim(point.z()) + ".");
        }

        if (!session.isComplete()) {
            if (firstPosition) {
                player.sendMessage("Now set the final corner with /qz pos2 or /qz pos2 <x> <z>.");
            } else {
                player.sendMessage("Now set the first corner with /qz pos1 or /qz pos1 <x> <z>.");
            }
            player.sendMessage("Use /qz exit to cancel.");
            return;
        }

        zoneManager.setZoneBounds(session.getZoneId(), session.getWorldName(), session.getPos1(), session.getPos2());
        selectionManager.end(player.getUniqueId());
        zoneMuteService.refreshNow();

        QuietZone zone = zoneManager.getZone(session.getZoneId());
        if (zone == null) {
            player.sendMessage("Zone no longer exists.");
            return;
        }

        player.sendMessage("Zone '" + zone.getId() + "' updated.");
        player.sendMessage("Bounds: world=" + zone.getWorldName()
                + " x=" + trim(zone.getMinX()) + " to " + trim(zone.getMaxX())
                + " z=" + trim(zone.getMinZ()) + " to " + trim(zone.getMaxZ()) + ".");
        player.sendMessage("Mute is " + (zone.isEnabled() ? "enabled" : "disabled") + " for this zone.");
    }

    private void handleExit(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /qz exit.");
            return;
        }

        ZoneSelectionSession ended = selectionManager.end(player.getUniqueId());
        if (ended == null) {
            player.sendMessage("You are not currently in selection mode.");
            return;
        }

        player.sendMessage("Selection mode exited for zone '" + ended.getZoneId() + "'.");
    }

    private ZonePoint parsePoint(Location location, String[] args) {
        if (args.length == 1) {
            return new ZonePoint(location.getX(), location.getZ());
        }
        if (args.length != 3) {
            return null;
        }

        try {
            double x = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            return new ZonePoint(x, z);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isValidZoneId(String zoneId) {
        return ZONE_ID_PATTERN.matcher(zoneId).matches();
    }

    private String trim(double value) {
        if (Math.floor(value) == value) {
            return Integer.toString((int) value);
        }
        return Double.toString(value);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("Quiet Zones commands:");
        sendZoneHelp(sender);
        sender.sendMessage("/qz pos1 [x] [z]");
        sender.sendMessage("/qz pos2 [x] [z]");
        sender.sendMessage("/qz exit");
    }

    private void sendZoneHelp(CommandSender sender) {
        sender.sendMessage("/qz zone create <id>");
        sender.sendMessage("/qz zone remove <id>");
        sender.sendMessage("/qz zone <id> set");
        sender.sendMessage("/qz zone <id> enable");
        sender.sendMessage("/qz zone <id> disable");
    }

    private List<String> filterByPrefix(List<String> values, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return values;
        }

        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .distinct()
                .sorted()
                .toList();
    }
}
