/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.commands;

import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Speed implements @Nullable CommandExecutor {

    private final SpectatorMode plugin;

    public Speed(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
        @NotNull String label, @NotNull String @NotNull [] args) {
        int maxSpeed = plugin.getConfigManager().getInt("max-speed");
        boolean speedAllowed = plugin.getConfigManager().getBoolean("speed");
        if ((label.equalsIgnoreCase("speed")) || (label.equalsIgnoreCase("sp"))) {
            float speed;
            if (!speedAllowed) {
                Messenger.send(sender,"permission-message");
            }
            if (!(sender instanceof Player)) {
                Messenger.send(sender,"console-message");
                return true;

            }
            if (!sender.hasPermission("smpspectator.speed")) {
                Messenger.send(sender,"permission-message");
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
                Messenger.send(sender,"speed-message", String.valueOf(speed));
                return true;

            }

            try {
                speed = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                Messenger.send(sender,"invalid-speed-message");
                return true;
            }

            if (speed > (float) maxSpeed || speed < 0f) {
                Messenger.send(sender,"invalid-speed-message");
            } else {
                player.setFlySpeed(speed / 10);
                Messenger.send(sender,"speed-message", String.valueOf(speed));
            }
            return true;

        }

        return false;
    }
}

