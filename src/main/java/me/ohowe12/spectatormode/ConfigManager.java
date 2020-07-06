package me.ohowe12.spectatormode;

import me.ohowe12.spectatormode.commands.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private final FileConfiguration config;
    private final Configuration defaults;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
        this.defaults = config.getDefaults();

        for (String path : defaults.getKeys(true)) {
            boolean isIn = config.getKeys(true).contains(path);
            Bukkit.getLogger().info(String.valueOf(isIn));
            if (!isIn) {
                config.set(path, defaults.get(path));
            }
        }
        SpectatorMode.getInstance().saveConfig();
    }
    public @NotNull String getColorizedString(@NotNull String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(path, defaults.getString(path))));
    }

    public boolean getBoolean(@NotNull String path) {
        return config.getBoolean(path, defaults.getBoolean(path));
    }

    public List<?> getList(@NotNull String path) {
        return Objects.requireNonNull(config.getList(path, defaults.getList(path)));
    }

    public int getInt(@NotNull String path) {
        return config.getInt(path, defaults.getInt(path));
    }
}
