package me.ohowe12.spectatormode.tabCompleter;

import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpeedTab implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> arguments = new ArrayList<>();
        if (sender.hasPermission("speed-use")) {
            arguments.add("print");
            for (int i = 1; i <= (SpectatorMode.getInstance().getConfig().getInt("max-speed", 5)); i++) {
                arguments.add(Integer.toString(i));
            }
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
