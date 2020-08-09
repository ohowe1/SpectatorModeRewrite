/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.listener;

import java.util.Objects;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;


public class OnMoveListener implements Listener {

    private final SpectatorMode plugin;

    public OnMoveListener(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(@NotNull PlayerMoveEvent e) {
        int yLevel = plugin.getConfigManager().getInt("y-level");
        boolean enforceY = plugin.getConfigManager().getBoolean("enforce-y");
        boolean enforceDistance = plugin.getConfigManager().getBoolean("enforce-distance");
        boolean enforceNonTransparent = plugin.getConfigManager()
            .getBoolean("disallow-non-transparent-blocks");
        boolean enforceAllBlocks = plugin.getConfigManager().getBoolean("disallow-all-blocks");
        boolean enforceWorldBorder = plugin.getConfigManager().getBoolean("enforce-world-border");

        Player player = e.getPlayer();
        Location location = e.getTo();
        Location eyeLevel = new Location(player.getWorld(), Objects.requireNonNull(location).getX(),
            location.getY() + 1, location.getZ());

        if (!(plugin.getSpectatorCommand().inState(player.getUniqueId().toString()))) {
            return;
        }
        if (player.hasPermission("spectator-bypass")) {
            return;
        }
        if (!(player.getGameMode().equals(GameMode.SPECTATOR))) {
            return;
        }

        if (enforceY) {
            if (location.getY() <= yLevel) {
                e.setTo(e.getFrom());
                e.setCancelled(true);
                return;
            }
        }

        Block currentBlock = eyeLevel.getBlock();
        if (enforceAllBlocks) {
            if (!(currentBlock.getType().isAir())) {
                if (!(currentBlock.getType() == Material.RAIL)) {
                    if (!(currentBlock.getType() == Material.ACTIVATOR_RAIL)) {
                        if (!(currentBlock.getType() == Material.DETECTOR_RAIL)) {
                            if (!(currentBlock.getType() == Material.POWERED_RAIL)) {
                                e.setTo(e.getFrom());
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }

                }


            }
        }
        if (enforceNonTransparent) {
            if (checkBlock(currentBlock)) {
                e.setTo(e.getFrom());
                e.setCancelled(true);
                return;
            }
        }
        if (enforceDistance) {
            if (checkDistance(player.getUniqueId().toString(), location)) {
                e.setTo(e.getFrom());
                e.setCancelled(true);
                return;
            }
        }
        if (enforceWorldBorder) {
            if (!(Objects.requireNonNull(location.getWorld()).getWorldBorder()
                .isInside(location))) {
                e.setTo(e.getFrom());
                e.setCancelled(true);
            }
        }
    }

    private boolean checkDistance(String player, @NotNull Location location) {
        int distance = plugin.getConfigManager().getInt("distance");
        Location originalLocation = plugin.getSpectatorCommand().getState(player)
            .getPlayerLocation();
        return (originalLocation.distance(location)) > distance;
    }

    private boolean checkBlock(@NotNull Block currentBlock) {
        return (currentBlock.getType().isOccluding());
    }

    @EventHandler
    public void onTeleport(@NotNull PlayerTeleportEvent e) {
        boolean preventTeleport = plugin.getConfigManager().getBoolean("prevent-teleport");
        if (e.getPlayer().hasPermission("spectator-bypass")) {
            return;
        }
        if (!(plugin.getSpectatorCommand().inState(e.getPlayer().getUniqueId().toString()))) {
            return;
        }
        if (!(e.getPlayer().getGameMode().equals(GameMode.SPECTATOR))) {
            return;
        }
        if (!preventTeleport) {
            return;
        }
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            e.getPlayer()
                .sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
            e.setCancelled(true);
        }
    }
}
