/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.commands;

import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Speed implements @Nullable CommandExecutor {
    final int maxSpeed;
    final SpectatorMode plugin;

    public Speed() {
        plugin = SpectatorMode.getInstance();
        plugin.saveDefaultConfig();
        maxSpeed = plugin.getConfig().getInt("max-speed", 5);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, @NotNull String[] args) {
        if ((label.equalsIgnoreCase("speed")) || (label.equalsIgnoreCase("sp"))) {
            float speed;
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to do that!");
                return true;

            }
            if (!sender.hasPermission("speed-use")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
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
                player.sendMessage(ChatColor.AQUA + "Speed has been set to " + speed);
                return true;

            }

            if (args[0].equalsIgnoreCase("print")) {
                player.sendMessage(ChatColor.AQUA + "Your speed is " + ChatColor.BOLD + player.getFlySpeed() * 10);
                return true;
            }

            try {
                speed = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That is " + ChatColor.BOLD + "not" + ChatColor.RESET + ChatColor.RED + " a valid speed!");
                return true;
            }

            if (speed > (float) maxSpeed || speed < 0f) {
                sender.sendMessage(ChatColor.RED + "That is " + ChatColor.BOLD + "not" + ChatColor.RESET + ChatColor.RED + " a valid speed!");
            } else {
                player.setFlySpeed(speed / 10);
                player.sendMessage(ChatColor.AQUA + "Speed has been set to " + speed);
            }
            return true;

        }

        return false;
    }
}

