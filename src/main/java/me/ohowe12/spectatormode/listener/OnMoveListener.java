/*
 * MIT License
 *
 * Copyright (c) 2021 carelesshippo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN
 */

package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.Messenger;
import org.bukkit.*;
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

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class OnMoveListener implements Listener {

    private final SpectatorMode plugin;

    public OnMoveListener(final SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(@NotNull final PlayerMoveEvent moveEvent) {
        if (shouldDoNotSkipEvent(moveEvent) && shouldCancelMoveEvent(moveEvent)) {
            cancelPlayerMoveEvent(moveEvent);
        }
    }

    @EventHandler
    public void onTeleport(@NotNull final PlayerTeleportEvent teleportEvent) {
        if (shouldCancelTeleport(teleportEvent) && shouldDoNotSkipEvent(teleportEvent)) {
            Messenger.send(teleportEvent.getPlayer(), "permission-message");
            teleportEvent.setCancelled(true);
        }
    }

    private boolean shouldCancelMoveEvent(PlayerMoveEvent moveEvent) {
        final boolean enforceY = plugin.getConfigManager().getBoolean("enforce-y");
        final boolean enforceDistance = plugin.getConfigManager().getBoolean("enforce-distance");
        final boolean enforceWorldBorder = plugin.getConfigManager().getBoolean("enforce-world-border");

        return (enforceY && checkAndEnforceY(moveEvent)) || isCollidingAndCollidingNotAllowed(moveEvent)
                || (enforceDistance && distanceTooFar(moveEvent)) || (enforceWorldBorder && outsideWorldBorder(moveEvent));
    }

    private boolean outsideWorldBorder(PlayerMoveEvent moveEvent) {
        if (plugin.isUnitTest()) {
            return false;
        }
        Location toLocation = moveEvent.getTo();
        if (toLocation == null) {
            return false;
        }
        World world = Objects.requireNonNull(toLocation.getWorld());
        return !world.getWorldBorder().isInside(toLocation);
    }

    private boolean checkAndEnforceY(PlayerMoveEvent moveEvent) {
        int yLevel = plugin.getConfigManager().getInt("y-level");
        Location toLocation = moveEvent.getTo();
        if (toLocation == null) {
            return false;
        }
        return toLocation.getY() <= yLevel;
    }

    private void cancelPlayerMoveEvent(PlayerMoveEvent moveEvent) {
        moveEvent.setTo(moveEvent.getFrom());
        moveEvent.setCancelled(true);
    }

    public boolean isCollidingAndCollidingNotAllowed(@NotNull PlayerMoveEvent moveEvent) {
        final boolean enforceNonTransparent = plugin.getConfigManager().getBoolean("disallow-non-transparent-blocks");
        final boolean enforceAllBlocks = plugin.getConfigManager().getBoolean("disallow-all-blocks");
        final List<String> disallowedBlocks = (List<String>) plugin.getConfigManager().getList("disallowed-blocks");


        final float bubbleSize = plugin.getConfigManager().getInt("bubble-size") / 100.0f;
        if (moveEvent.getTo() == null || !(enforceNonTransparent || enforceAllBlocks || disallowedBlocks.size() > 0))
            return false;
        for (int x = -1; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = -1; z < 2; z++) {
                    Block block = moveEvent.getTo().getBlock().getRelative(x, y, z);
                    BoundingBox bb = block.getBoundingBox().clone().expand(bubbleSize);
                    Material mat = block.getType();
                    Vector toVect = moveEvent.getTo().toVector().clone().add(new Vector(0, 1.6, 0));
                    if (mat.isSolid() && toVect.isInAABB(bb.getMin(), bb.getMax())) {
                        return enforceAllBlocks || (mat.isOccluding() && enforceNonTransparent) || disallowedBlocks.stream().anyMatch(mat.name()::equalsIgnoreCase);
                    }
                }
            }
        }
        return false;
    }

    private boolean distanceTooFar(PlayerMoveEvent moveEvent) {
        final int distance = plugin.getConfigManager().getInt("distance");
        final Location originalLocation = plugin.getSpectatorManager().getStateHolder().getPlayer(moveEvent.getPlayer())
                .getPlayerLocation();
        Location toLocation = moveEvent.getTo();
        if (toLocation == null) {
            return false;
        }
        return (originalLocation.distance(toLocation)) > distance;
    }

    private boolean shouldCancelTeleport(PlayerTeleportEvent teleportEvent) {
        return (teleportEvent.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE))
                && plugin.getConfigManager().getBoolean("prevent-teleport");
    }

    private boolean shouldDoNotSkipEvent(PlayerEvent event) {
        return !event.getPlayer().hasPermission("smpspectator.bypass")
                && plugin.getSpectatorManager().getStateHolder().hasPlayer(event.getPlayer())
                && event.getPlayer().getGameMode() == GameMode.SPECTATOR;
    }
}
