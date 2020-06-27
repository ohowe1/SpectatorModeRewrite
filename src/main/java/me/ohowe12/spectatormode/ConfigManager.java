package me.ohowe12.spectatormode;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private final FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }
    public @NotNull String getColorizedString(@NotNull String path, String defaultString) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(path, defaultString)));
    }

    public boolean getBoolean(@NotNull String path, boolean defaultBoolean) {
        return config.getBoolean(path, defaultBoolean);
    }

    public List<?> getList(@NotNull String path, List<?> defaultList) {
        return Objects.requireNonNull(config.getList(path, defaultList));
    }

    public int getInt(@NotNull String path, int defaultInt) {
        return config.getInt(path, defaultInt);
    }
}
