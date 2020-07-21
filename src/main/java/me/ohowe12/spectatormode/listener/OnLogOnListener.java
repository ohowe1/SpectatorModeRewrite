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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OnLogOnListener implements Listener {

    SpectatorMode plugin = SpectatorMode.getInstance();

    @EventHandler
    public void onLogOn(@NotNull PlayerJoinEvent e) {
        boolean teleportBack = SpectatorMode.getInstance().getConfigManager().getBoolean("teleport-back");
        Player player = e.getPlayer();
        if (!(teleportBack)) {
            return;
        }
        try {
            if (plugin.spectatorCommand.inState(player.getUniqueId().toString())) {
                teleportPlayerBack(player);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void teleportPlayerBack(@NotNull Player player) {
        Location location = plugin.spectatorCommand.getState(player.getUniqueId().toString()).getPlayerLocation();
        player.teleport(location);
    }
}
