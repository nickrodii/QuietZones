package com.nickrodi.zone;

public final class ZoneSelectionSession {
    private final String zoneId;
    private final String worldName;
    private ZonePoint pos1;
    private ZonePoint pos2;

    public ZoneSelectionSession(String zoneId, String worldName) {
        this.zoneId = zoneId;
        this.worldName = worldName;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getWorldName() {
        return worldName;
    }

    public ZonePoint getPos1() {
        return pos1;
    }

    public ZonePoint getPos2() {
        return pos2;
    }

    public void setPos1(ZonePoint pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(ZonePoint pos2) {
        this.pos2 = pos2;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }
}
