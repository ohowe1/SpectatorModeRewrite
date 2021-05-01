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

import dev.jorel.commandapi.annotations.*;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Command("s")
@Alias("smps")
public class SpectatorCommand {

    private static SpectatorMode plugin;

    public static void initPlugin(SpectatorMode plugin) {
        SpectatorCommand.plugin = plugin;
    }

    @Default
    @Permission("smpspectator.use")
    public static void s(Player player) {
        plugin.getSpectatorManager().togglePlayer(player);
    }

    @Default
    @Permission("smpspectator.force")
    public static void sForce(CommandSender sender, @APlayerArgument Player target) {
        plugin.getSpectatorManager().togglePlayer(target, true);
    }

    @Subcommand("enable")
    @Permission("smpspectator.enable")
    public static void enableSpectator(CommandSender sender) {
        plugin.getSpectatorManager().setSpectatorEnabled(true);
        Messenger.send(sender, "enable-message");
    }

    @Subcommand("disable")
    @Permission("smpspectator.disable")
    public static void disableSpectator(CommandSender sender) {
        plugin.getSpectatorManager().setSpectatorEnabled(false);
        Messenger.send(sender, "disable-message");
    }

    @Subcommand("reload")
    @Permission("smpspecator.reload")
    public static void reloadConfig(CommandSender sender) {
        plugin.reloadConfigManager();
    }

    @Subcommand("effect")
    @Permission("smpspectator.toggle")
    public static void toggleEffects(Player player) {
        if (!plugin.getConfigManager().getBoolean("seffect")) {
            Messenger.send(player, "permission-message");
        }
        if (!plugin.getSpectatorManager().getStateHolder().hasPlayer(player)) {
            Messenger.send(player, "no-spectator-message");
            return;
        }
        if (hasEffects(player)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        } else {
            if (plugin.getConfigManager().getBoolean("night-vision")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10));
            }
            if (plugin.getConfigManager().getBoolean("conduit")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10));
            }
        }
    }

    private static boolean hasEffects(Player player) {
        return player.hasPotionEffect(PotionEffectType.NIGHT_VISION)
                || player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
    }

    @Subcommand("speed")
    @Permission("smpspectator.speed")
    public static void spectatorSpeed(Player player, @AIntegerArgument(min = 1, max = 5) int speed) {
        player.setFlySpeed((float) speed / 10);
        Messenger.send(player, "speed-message", String.valueOf(speed));
    }
}
