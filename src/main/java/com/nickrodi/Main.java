package com.nickrodi;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.nickrodi.command.QuietZoneCommand;
import com.nickrodi.listener.SelectionListener;
import com.nickrodi.mute.ZoneMuteService;
import com.nickrodi.zone.ZoneManager;
import com.nickrodi.zone.ZoneSelectionManager;

public final class Main extends JavaPlugin {
    private ZoneManager zoneManager;
    private ZoneSelectionManager selectionManager;
    private ZoneMuteService zoneMuteService;

    @Override
    public void onEnable() {
        zoneManager = new ZoneManager(this);
        zoneManager.load();

        selectionManager = new ZoneSelectionManager();
        zoneMuteService = new ZoneMuteService(this, zoneManager);

        QuietZoneCommand quietZoneCommand = new QuietZoneCommand(zoneManager, selectionManager, zoneMuteService);
        PluginCommand command = getCommand("qz");
        if (command == null) {
            getLogger().severe("Command /qz was not found in plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        command.setExecutor(quietZoneCommand);
        command.setTabCompleter(quietZoneCommand);

        getServer().getPluginManager().registerEvents(new SelectionListener(selectionManager), this);

        zoneMuteService.start();
        getLogger().info("SMPQuietZones enabled.");
    }

    @Override
    public void onDisable() {
        if (zoneMuteService != null) {
            zoneMuteService.shutdown();
        }

        if (zoneManager != null) {
            zoneManager.save();
        }

        if (selectionManager != null) {
            selectionManager.clear();
        }
    }
}
