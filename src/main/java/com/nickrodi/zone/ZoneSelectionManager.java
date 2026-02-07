package com.nickrodi.zone;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ZoneSelectionManager {
    private final Map<UUID, ZoneSelectionSession> sessions = new ConcurrentHashMap<>();

    public ZoneSelectionSession start(UUID playerId, String zoneId, String worldName) {
        ZoneSelectionSession session = new ZoneSelectionSession(zoneId, worldName);
        sessions.put(playerId, session);
        return session;
    }

    public ZoneSelectionSession get(UUID playerId) {
        return sessions.get(playerId);
    }

    public boolean hasSession(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public ZoneSelectionSession end(UUID playerId) {
        return sessions.remove(playerId);
    }

    public void clear() {
        sessions.clear();
    }

    public void clearZone(String zoneId) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getZoneId().equalsIgnoreCase(zoneId));
    }
}
