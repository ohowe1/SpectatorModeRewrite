/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.tabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpeedTab implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
        @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }


}
