package com.nickrodi.listener;

import com.nickrodi.zone.ZoneSelectionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class SelectionListener implements Listener {
    private final ZoneSelectionManager selectionManager;

    public SelectionListener(ZoneSelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        selectionManager.end(event.getPlayer().getUniqueId());
    }
}
