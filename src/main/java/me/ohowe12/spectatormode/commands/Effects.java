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

package me.ohowe12.spectatormode.commands;

import me.ohowe12.spectatormode.ConfigManager;
import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Effects implements CommandExecutor {

    private final PotionEffect NIGHTVISON = new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10);
    private final PotionEffect CONDUIT = new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10);
    private final SpectatorMode plugin;
    private final ConfigManager manager;

    public Effects(SpectatorMode plugin) {
        this.plugin = plugin;
        this.manager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
            if (!(sender instanceof Player)) {
                Messenger.send(sender, "console-message");
                return true;
            }
            Player player = (Player) sender;
            toggleEffects(player);
            return true;
    }

    private void toggleEffects(Player player) {
        if (!manager.getBoolean("seffect")) {
            Messenger.send(player, "permission-message");
        }
        if (!plugin.getSpectatorCommand().inState(player.getUniqueId().toString())) {
            Messenger.send(player, "no-spectator-message");
            return;
        }
        if (hasEffects(player)) {
            removePotions(player);
            return;
        }
        if (manager.getBoolean("night-vision")) {
            player.addPotionEffect(NIGHTVISON);
        }
        if (manager.getBoolean("conduit")) {
            player.addPotionEffect(CONDUIT);
        }
        return;
    }

    private void removePotions(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
    }

    private boolean hasEffects(Player player) {
        return player.hasPotionEffect(PotionEffectType.NIGHT_VISION)
                || player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
    }

}
