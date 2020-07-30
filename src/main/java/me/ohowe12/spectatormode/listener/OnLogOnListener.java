/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class OnLogOnListener implements Listener {

    final SpectatorMode plugin = SpectatorMode.getInstance();

    @EventHandler
    public void onLogOn(@NotNull PlayerJoinEvent e) {
        boolean teleportBack = SpectatorMode.getInstance().getConfigManager().getBoolean("teleport-back");
        Player player = e.getPlayer();
        if (!(teleportBack)) {
            return;
        }
        try {
            if (plugin.getSpectatorCommand().inState(player.getUniqueId().toString())) {
                teleportPlayerBack(player);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void teleportPlayerBack(@NotNull Player player) {
        Location location = plugin.getSpectatorCommand().getState(player.getUniqueId().toString()).getPlayerLocation();
        player.teleport(location);
    }
}
