package com.nickrodi.zone;

import org.bukkit.Location;

import java.util.Locale;
import java.util.Objects;

public final class QuietZone {
    private final String id;
    private String worldName;
    private double minX;
    private double maxX;
    private double minZ;
    private double maxZ;
    private boolean configured;
    private boolean enabled;

    public QuietZone(String id) {
        this.id = normalizeId(id);
        this.enabled = true;
        this.configured = false;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isConfigured() {
        return configured;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public void setBounds(String worldName, double x1, double z1, double x2, double z2) {
        this.worldName = Objects.requireNonNull(worldName, "worldName");
        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxZ = Math.max(z1, z2);
        this.configured = true;
    }

    public boolean contains(Location location) {
        if (!enabled || !configured || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }

        double x = location.getX();
        double z = location.getZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public static String normalizeId(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
