package me.ohowe12.spectatormode.tabCompleter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabCompleteUtil {
    @Nullable
    public static List<String> getStrings(@NotNull String @NotNull [] args, @NotNull List<String> arguments) {
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
