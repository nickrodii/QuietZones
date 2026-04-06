package quietzones.mute;

import quietzones.zone.ZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ZoneMuteService {
    private final JavaPlugin plugin;
    private final ZoneManager zoneManager;
    private final Map<UUID, Boolean> originalSilentState = new HashMap<>();
    private BukkitTask task;

    public ZoneMuteService(JavaPlugin plugin, ZoneManager zoneManager) {
        this.plugin = plugin;
        this.zoneManager = zoneManager;
    }

    public void start() {
        if (task != null) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 10L);
    }

    public void refreshNow() {
        tick();
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        for (Map.Entry<UUID, Boolean> entry : new HashMap<>(originalSilentState).entrySet()) {
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity != null) {
                entity.setSilent(entry.getValue());
            }
        }
        originalSilentState.clear();
    }

    private void tick() {
        Set<UUID> currentlyLoaded = new HashSet<>();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                UUID entityId = entity.getUniqueId();
                currentlyLoaded.add(entityId);

                boolean shouldMute = zoneManager.isMutedLocation(entity.getLocation());
                if (shouldMute) {
                    originalSilentState.putIfAbsent(entityId, entity.isSilent());
                    if (!entity.isSilent()) {
                        entity.setSilent(true);
                    }
                } else {
                    restoreEntity(entityId, entity);
                }
            }
        }

        originalSilentState.keySet().removeIf(entityId -> !currentlyLoaded.contains(entityId));
    }

    private void restoreEntity(UUID entityId, Entity entity) {
        Boolean originalState = originalSilentState.remove(entityId);
        if (originalState != null) {
            entity.setSilent(originalState);
        }
    }
}
