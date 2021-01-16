/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class OnMoveListener implements Listener {

    private final SpectatorMode plugin;

    public OnMoveListener(final SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(@NotNull final PlayerMoveEvent e) {
        if (!shouldSkipEvent(e) && shouldCancelMoveEvent(e)) {
            cancelPlayerMoveEvent(e);
        }
    }

    @EventHandler
    public void onTeleport(@NotNull final PlayerTeleportEvent e) {
        if (shoudCancelTeleport(e) && !shouldSkipEvent(e)) {
            Messenger.send(e.getPlayer(), "permission-message");
            e.setCancelled(true);
        }
    }

    private boolean shouldCancelMoveEvent(PlayerMoveEvent e) {
        final boolean enforceY = plugin.getConfigManager().getBoolean("enforce-y");
        final boolean enforceDistance = plugin.getConfigManager().getBoolean("enforce-distance");
        final boolean enforceWorldBorder = plugin.getConfigManager().getBoolean("enforce-world-border");

        return (enforceY && checkAndEnforceY(e)) || isCollidingAndCollidingNotAllowed(e)
                || (enforceDistance && distanceTooFar(e)) || (enforceWorldBorder && outsideWorldBorder(e));
    }

    private boolean outsideWorldBorder(PlayerMoveEvent e) {
        Location location = e.getTo();
        return !location.getWorld().getWorldBorder().isInside(location);
    }

    private boolean checkAndEnforceY(PlayerMoveEvent e) {
        int yLevel = plugin.getConfigManager().getInt("y-level");
        return e.getTo().getY() <= yLevel;
    }

    private void cancelPlayerMoveEvent(PlayerMoveEvent e) {
        e.setTo(e.getFrom());
        e.setCancelled(true);
    }

    @SuppressWarnings("all")
    public boolean isCollidingAndCollidingNotAllowed(@NotNull PlayerMoveEvent e) {
        final boolean enforceNonTransparent = plugin.getConfigManager().getBoolean("disallow-non-transparent-blocks");
        final boolean enforceAllBlocks = plugin.getConfigManager().getBoolean("disallow-all-blocks");
        final float bubbleSize = plugin.getConfigManager().getInt("bubble-size") / 100.0f;
        if (e.getTo() == null || !(enforceNonTransparent || enforceAllBlocks))
            return false;
        for (int x = -1; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = -1; z < 2; z++) {
                    Block block = e.getTo().getBlock().getRelative(x, y, z);
                    BoundingBox bb = block.getBoundingBox().clone().expand(bubbleSize);
                    Material mat = block.getType();
                    Vector tovect = e.getTo().toVector().clone().add(new Vector(0, 1.6, 0));
                    if (mat.isSolid() && tovect.isInAABB(bb.getMin(), bb.getMax())) {
                        return enforceAllBlocks || mat.isOccluding();
                    }
                }
            }
        }
        return false;
    }

    private boolean distanceTooFar(PlayerMoveEvent e) {
        final int distance = plugin.getConfigManager().getInt("distance");
        final Location originalLocation = plugin.getSpectatorCommand().getState(e.getPlayer().getUniqueId().toString())
                .getPlayerLocation();
        return (originalLocation.distance(e.getTo())) > distance;
    }

    private boolean shoudCancelTeleport(PlayerTeleportEvent e) {
        return (e.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE))
                && plugin.getConfigManager().getBoolean("prevent-teleport");
    }

    private boolean shouldSkipEvent(PlayerEvent e) {
        return e.getPlayer().hasPermission("smpspectator.bypass")
                || !plugin.getSpectatorCommand().inState(e.getPlayer().getUniqueId().toString())
                || !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR);
    }
}
