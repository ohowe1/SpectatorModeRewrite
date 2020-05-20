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
import org.bukkit.ChatColor;
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

import java.util.Map;
import java.util.Objects;


public class OnMoveListener implements Listener {
    private final SpectatorMode plugin = SpectatorMode.getInstance();
    private Map<String, State> state;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        int yLevel = plugin.getConfig().getInt("y-level", 0);
        boolean enforceY = plugin.getConfig().getBoolean("enforce-y", false);
        boolean enforceDistance = plugin.getConfig().getBoolean("enforce-distance", false);
        boolean enforceNonTransparent = plugin.getConfig().getBoolean("disallow-non-transparent-blocks", false);
        boolean enforceAllBlocks = plugin.getConfig().getBoolean("disallow-all-blocks", false);

        Player player = e.getPlayer();
        Location location = e.getTo();
        Location eyeLevel = new Location(player.getWorld(), location.getX(), location.getY() + 1, location.getZ());
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

    private boolean checkDistance(String player, Location location) {
        int distance = plugin.getConfig().getInt("distance", 64);
        Location originalLocation = state.get(player).getPlayerLocation();
        return (originalLocation.distance(location)) > distance;
    }

    private boolean checkBlock(Block currentBlock) {
        return (currentBlock.getType().isOccluding());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        boolean preventTeleport = plugin.getConfig().getBoolean("prevent-teleport", false);
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
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("permission-message", "&cYou do not have permission to do that!"))));
            e.setCancelled(true);
        }
    }
}
