/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.State;
import me.ohowe12.spectatormode.commands.Spectator;
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

import java.util.Map;
import java.util.Objects;


public class OnMoveListener implements Listener {
    private final SpectatorMode plugin = SpectatorMode.getInstance();
    private Map<String, State> state;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(@NotNull PlayerMoveEvent e) {
        int yLevel = plugin.getConfigManager().getInt("y-level");
        boolean enforceY = plugin.getConfigManager().getBoolean("enforce-y");
        boolean enforceDistance = plugin.getConfigManager().getBoolean("enforce-distance");
        boolean enforceNonTransparent = plugin.getConfigManager().getBoolean("disallow-non-transparent-blocks");
        boolean enforceAllBlocks = plugin.getConfigManager().getBoolean("disallow-all-blocks");

        Player player = e.getPlayer();
        Location location = e.getTo();
        Location eyeLevel = new Location(player.getWorld(), Objects.requireNonNull(location).getX(), location.getY() + 1, location.getZ());
        state = Spectator.getInstance().state;

        if (!(state.containsKey(player.getUniqueId().toString()))) {
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
                if (!currentBlock.getType().equals(Material.RAIL)){
                    if (!currentBlock.getType().equals(Material.ACTIVATOR_RAIL)) {
                        if (!currentBlock.getType().equals(Material.DETECTOR_RAIL)) {
                            if (!currentBlock.getType().equals(Material.POWERED_RAIL)) {
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
            }
        }
        if (enforceDistance) {
            if (checkDistance(player.getUniqueId().toString(), location)) {
                e.setTo(e.getFrom());
                e.setCancelled(true);
                return;
            }
        }
        if (!(Objects.requireNonNull(location.getWorld()).getWorldBorder().isInside(location))) {
            e.setTo(e.getFrom());
            e.setCancelled(true);
        }
    }

    private boolean checkDistance(String player, @NotNull Location location) {
        int distance = plugin.getConfigManager().getInt("distance");
        Location originalLocation = state.get(player).getPlayerLocation();
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
        if (!(state.containsKey(e.getPlayer().getUniqueId().toString()))) {
            return;
        }
        if (!(e.getPlayer().getGameMode().equals(GameMode.SPECTATOR))) {
            return;
        }
        if (!preventTeleport) {
            return;
        }
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            e.getPlayer().sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
            e.setCancelled(true);
        }
    }
}
