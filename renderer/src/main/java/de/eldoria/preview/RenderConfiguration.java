package de.eldoria.preview;

import org.bukkit.Location;

public class RenderConfiguration {
    int previewRefreshInterval;
    int renderDistance;
    private int maxRenderMs;

    public int previewRefreshInterval() {
        return previewRefreshInterval;
    }

    public int renderDistance() {
        return renderDistance;
    }

    public boolean isOutOfRenderRange(Location origin, Location other) {
        if (origin.getWorld() != other.getWorld()) return false;
        return origin.distanceSquared(other) > renderDistanceSquared();
    }

    private int renderDistanceSquared() {
        return (int) Math.pow(renderDistance(), 2);
    }

    public int maxRenderMs() {
        return maxRenderMs;
    }
}
