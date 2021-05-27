/*
 * MIT License
 *
 * Copyright (c) 2021 carelesshippo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN
 */

package me.ohowe12.spectatormode.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.ohowe12.spectatormode.SpectatorMode;
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

    public String getString(@NotNull final String path) {
        return Objects.requireNonNull(config.getString(path, defaults.getString(path)));
    }

    public @NotNull String getColorizedString(@NotNull final String path) {
        return ChatColor.translateAlternateColorCodes('&',
                getString(path));
    }

    public boolean getBoolean(@NotNull final String path) {
        return config.getBoolean(path, defaults.getBoolean(path));
    }

    public double getDouble(@NotNull final String path) {
        return config.getDouble(path, defaults.getDouble(path));
    }

    @SuppressWarnings("all")
    public List<?> getList(@NotNull final String path) {
        return Objects.requireNonNull(config.getList(path, defaults.getList(path)));
    }

    public int getInt(@NotNull final String path) {
        return config.getInt(path, defaults.getInt(path));
    }
}
