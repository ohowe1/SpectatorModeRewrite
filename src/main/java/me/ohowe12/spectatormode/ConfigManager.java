package me.ohowe12.spectatormode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {

    private final FileConfiguration config;
    private final Configuration defaults;

    public ConfigManager(final SpectatorMode plugin, final FileConfiguration config) {
        this.config = config;
        this.defaults = config.getDefaults();

        for (final String path : Objects.requireNonNull(defaults).getKeys(true)) {
            final boolean isIn = config.getKeys(true).contains(path);
            if (!isIn) {
                config.set(path, defaults.get(path));
            }
        }
        plugin.saveConfig();
    }

    public Map<String, String> getAllBooleansAndNumbers() {
        Map<String, String> result = new HashMap<>();
        for (String path : config.getKeys(true)) {
            if (config.isBoolean(path) || config.isInt(path)) {
                result.put(path, String.valueOf(config.get(path)));
            }
        }
        return result;
    }

    public @NotNull String getColorizedString(@NotNull final String path) {
        return ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(config.getString(path, defaults.getString(path))));
    }

    public boolean getBoolean(@NotNull final String path) {
        return config.getBoolean(path, defaults.getBoolean(path));
    }

    public double getDouble(@NotNull final String path) {
        return config.getDouble(path, defaults.getDouble(path));
    }

    public List<?> getList(@NotNull final String path) {
        return Objects.requireNonNull(config.getList(path, defaults.getList(path)));
    }

    public int getInt(@NotNull final String path) {
        return config.getInt(path, defaults.getInt(path));
    }
}
