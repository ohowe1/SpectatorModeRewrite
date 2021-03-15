/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

public class UpdateChecker {

    private UpdateChecker() {
        
    }


    public static void getVersion(final @NotNull Consumer<String> consumer, Plugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream inputStream = new URL(
                "https://api.spigotmc.org/legacy/update.php?resource=77267")
                .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
