/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.tabCompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpectatorTab implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> arguments = new ArrayList<>();
        if (sender.hasPermission("spectator-force")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                arguments.add(p.getName());
            }
        }
        if (sender.hasPermission("spectator-enable")) {
            arguments.add("enable");
            arguments.add("disable");
            arguments.add("reload");
        }

        return TabCompleteUtil.getStrings(args, arguments);
    }
}

