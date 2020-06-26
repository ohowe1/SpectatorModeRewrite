package me.ohowe12.spectatormode;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private final FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }
    public String getColorizedString(String path, String defaultString) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(path, defaultString)));
    }

    public boolean getBoolean(String path, boolean defaultBoolean) {
        return config.getBoolean(path, defaultBoolean);
    }

    public List<?> getList(String path, List<?> defaultList) {
        return Objects.requireNonNull(config.getList(path, defaultList));
    }

    public int getInt(String path, int defaultInt) {
        return config.getInt(path, defaultInt);
    }
}
