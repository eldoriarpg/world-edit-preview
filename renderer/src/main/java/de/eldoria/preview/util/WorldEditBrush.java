/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.preview.util;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extension.platform.Actor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to manage world edit brushes.
 */
public final class WorldEditBrush {
    private static final WorldEdit WORLD_EDIT = WorldEdit.getInstance();

    private WorldEditBrush() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Get the schematic brush of a player registered on the item in its main hand.
     *
     * @param player player for lookup
     * @return schematic brush instance if the item is a schematic brush
     */
    @Nullable
    public static Brush getBrush(Player player) {
        return getBrush(player, player.getInventory().getItemInMainHand().getType());
    }

    /**
     * Get the schematic brush of a player registered on the item in its main hand.
     *
     * @param player   player for lookup
     * @param material material to get the brush
     * @return schematic brush instance if the item is a schematic brush
     */
    @SuppressWarnings({"ProhibitedExceptionCaught"})
    @Nullable
    public static Brush getBrush(Player player, Material material) {
        var itemType = BukkitAdapter.asItemType(material);
        if (itemType == null || itemType.hasBlockType()) {
            return null;
        }
        try {
            if (getLocalSession(player).getTool(itemType) instanceof BrushTool brushTool) {
                return brushTool.getBrush();
            }
        } catch (NullPointerException e) {
            // for some reason world edit throws a NPE when this function is called on world edit tools
        }
        return null;
    }

    /**
     * Get the local session of a player
     *
     * @param player player for lookup
     * @return local session.
     */
    private static LocalSession getLocalSession(Player player) {
        Actor actor = BukkitAdapter.adapt(player);

        return WORLD_EDIT.getSessionManager().get(actor);
    }
}
