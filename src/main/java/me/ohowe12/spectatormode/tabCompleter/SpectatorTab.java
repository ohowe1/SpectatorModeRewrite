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

        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            for (String a : arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    results.add(a);
                }
            }
            return results;
        }

        return null;
    }
}

