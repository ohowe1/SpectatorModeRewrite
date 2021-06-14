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
        if (plugin.getSpectatorManager().getStateHolder().hasPlayer(player) && teleportBack) {
            teleportPlayerBack(player);
        }
    }

    private void teleportPlayerBack(@NotNull final Player player) {
        final boolean silent = plugin.getConfigManager().getBoolean("silence-survival-mode-message-on-join");
        plugin.getPluginLogger().debugLog("Sending player back to survival mode");
        plugin.getSpectatorManager().togglePlayer(player, true, silent);
    }
}
