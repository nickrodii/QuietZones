package com.nickrodi.zone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZoneManager {
    private final JavaPlugin plugin;
    private final File zonesFile;
    private final Map<String, QuietZone> zones = new ConcurrentHashMap<>();

    public ZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.zonesFile = new File(plugin.getDataFolder(), "zones.yml");
    }

    public void load() {
        zones.clear();

        if (!zonesFile.exists()) {
            createZonesFile();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(zonesFile);
        ConfigurationSection zonesSection = config.getConfigurationSection("zones");
        if (zonesSection == null) {
            return;
        }

        for (String key : zonesSection.getKeys(false)) {
            String normalizedId = QuietZone.normalizeId(key);
            String path = "zones." + key + ".";

            QuietZone zone = new QuietZone(normalizedId);
            zone.setEnabled(config.getBoolean(path + "enabled", true));

            String worldName = config.getString(path + "world");
            if (worldName != null
                    && config.contains(path + "minX")
                    && config.contains(path + "maxX")
                    && config.contains(path + "minZ")
                    && config.contains(path + "maxZ")) {
                double minX = config.getDouble(path + "minX");
                double maxX = config.getDouble(path + "maxX");
                double minZ = config.getDouble(path + "minZ");
                double maxZ = config.getDouble(path + "maxZ");
                zone.setBounds(worldName, minX, minZ, maxX, maxZ);
            }

            zones.put(normalizedId, zone);
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();
        List<String> zoneIds = new ArrayList<>(zones.keySet());
        Collections.sort(zoneIds);

        for (String zoneId : zoneIds) {
            QuietZone zone = zones.get(zoneId);
            if (zone == null) {
                continue;
            }

            String path = "zones." + zoneId + ".";
            config.set(path + "enabled", zone.isEnabled());
            if (zone.isConfigured()) {
                config.set(path + "world", zone.getWorldName());
                config.set(path + "minX", zone.getMinX());
                config.set(path + "maxX", zone.getMaxX());
                config.set(path + "minZ", zone.getMinZ());
                config.set(path + "maxZ", zone.getMaxZ());
            }
        }

        try {
            config.save(zonesFile);
        } catch (IOException exception) {
            plugin.getLogger().severe(() -> "Failed to save zones.yml: " + exception.getMessage());
        }
    }

    public Collection<QuietZone> getZones() {
        return Collections.unmodifiableCollection(zones.values());
    }

    public List<String> getZoneIds() {
        List<String> ids = new ArrayList<>(zones.keySet());
        Collections.sort(ids);
        return ids;
    }

    public QuietZone getZone(String id) {
        if (id == null) {
            return null;
        }
        return zones.get(QuietZone.normalizeId(id));
    }

    public boolean createZone(String id) {
        String normalizedId = QuietZone.normalizeId(id);
        if (zones.containsKey(normalizedId)) {
            return false;
        }

        zones.put(normalizedId, new QuietZone(normalizedId));
        save();
        return true;
    }

    public boolean removeZone(String id) {
        String normalizedId = QuietZone.normalizeId(id);
        QuietZone removed = zones.remove(normalizedId);
        if (removed == null) {
            return false;
        }

        save();
        return true;
    }

    public boolean setEnabled(String id, boolean enabled) {
        QuietZone zone = getZone(id);
        if (zone == null) {
            return false;
        }

        zone.setEnabled(enabled);
        save();
        return true;
    }

    public boolean setZoneBounds(String id, String worldName, ZonePoint pos1, ZonePoint pos2) {
        QuietZone zone = getZone(id);
        if (zone == null) {
            return false;
        }

        zone.setBounds(worldName, pos1.x(), pos1.z(), pos2.x(), pos2.z());
        save();
        return true;
    }

    public boolean isMutedLocation(Location location) {
        for (QuietZone zone : zones.values()) {
            if (zone.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private void createZonesFile() {
        File parent = zonesFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, "Could not create plugin data folder: {0}", parent.getAbsolutePath());
        }

        try {
            if (!zonesFile.createNewFile()) {
                plugin.getLogger().log(Level.WARNING, "Could not create zones.yml at {0}", zonesFile.getAbsolutePath());
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create zones.yml: {0}", exception.getMessage());
        }
    }
}
