package me.ohowe12.spectatormode.tabcompleter;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabCompleteUtil {

    private TabCompleteUtil() {

    }

    @Nullable
    public static List<String> getStrings(@NotNull final String @NotNull [] args,
            @NotNull final List<String> arguments) {
        final List<String> results = new ArrayList<>();

        if (args.length == 1) {
            for (final String a : arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    results.add(a);
                }
            }
            return results;
        }

        return null;
    }
}
