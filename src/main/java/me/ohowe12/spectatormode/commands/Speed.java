/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.commands;

import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Speed implements @Nullable CommandExecutor {
    final SpectatorMode plugin;

    public Speed() {
        plugin = SpectatorMode.getInstance();
        plugin.saveDefaultConfig();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, @NotNull String[] args) {
        int maxSpeed = plugin.getConfigManager().getInt("max-speed", 5);
        boolean speedAllowed = plugin.getConfigManager().getBoolean("speed", true);
        if ((label.equalsIgnoreCase("speed")) || (label.equalsIgnoreCase("sp"))) {
            float speed;
            if (!speedAllowed) {
                sender.sendMessage(plugin.getConfigManager().getColorizedString("permission-message", "&cYou do not have permission to do that!"));
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getColorizedString("console-message", "&cYou are &lnot &ca player!"));
                return true;

            }
            if (!sender.hasPermission("speed-use")) {
                sender.sendMessage(plugin.getConfigManager().getColorizedString("permission-message", "&cYou do not have permission to do that!"));
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                if (maxSpeed < 2) {
                    speed = 1;
                } else {
                    speed = 2;
                }
                player.setFlySpeed(speed / 10);
                player.sendMessage(plugin.getConfigManager().getColorizedString("speed-message", "&bSpeed has been set to ") + speed);
                return true;

            }

            try {
                speed = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getColorizedString("invalid-speed-message", "&cThat is not a valid speed"));
                return true;
            }

            if (speed > (float) maxSpeed || speed < 0f) {
                player.sendMessage(plugin.getConfigManager().getColorizedString("invalid-speed-message", "&cThat is not a valid speed"));
            } else {
                player.setFlySpeed(speed / 10);
                player.sendMessage(plugin.getConfigManager().getColorizedString("speed-message", "&bSpeed has been set to ") + speed);
            }
            return true;

        }

        return false;
    }
}

