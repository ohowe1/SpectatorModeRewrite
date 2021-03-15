/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.PlaceholderEntity;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class OnLogOnListener implements Listener {

    private final SpectatorMode plugin;

    public OnLogOnListener(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogOn(@NotNull final PlayerJoinEvent e) {
        final boolean teleportBack = plugin.getConfigManager().getBoolean("teleport-back");
        final Player player = e.getPlayer();
        try {
            if (plugin.getSpectatorCommand().inState(player.getUniqueId().toString())) {
                if (teleportBack) {
                    teleportPlayerBack(player);
                } else {
                    PlaceholderEntity.create(player);
                }
            }
        } catch (final NullPointerException ignored) {
            // Filler
        }
    }

    private void teleportPlayerBack(@NotNull final Player player) {
        final boolean silent = plugin.getConfigManager().getBoolean("silence-survival-mode-message-on-join");
        plugin.getSpectatorCommand().goIntoSurvivalMode(player, silent);
    }
}
