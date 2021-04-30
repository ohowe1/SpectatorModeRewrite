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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.ohowe12.spectatormode.SpectatorMode;


public class DataSaver {

    private DataSaver() {

    }

    private static File dataFolder;
    private static final String HEADER = "data.";

    private static FileConfiguration data;
    private static SpectatorMode plugin;

    public static void init(File dataFolder, SpectatorMode plugin) {
        DataSaver.plugin = plugin;
        DataSaver.dataFolder = dataFolder;
        data = YamlConfiguration
        .loadConfiguration(new File(dataFolder, "data.yml"));
    }

    public static void save(final Map<String, State> state) {
        if (data.getConfigurationSection("data") != null) {
            Objects.requireNonNull(data.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                if (!state.containsKey(key)) {
                    setData(key, null);
                }
            });
        }
        for (@NotNull
        final Map.Entry<String, State> entry : state.entrySet()) {
            setData(entry.getKey(), entry.getValue().serialize());
        }
        try {
            data.save(new File(dataFolder, "data.yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void load(final Map<String, State> state) {
        if (data.getConfigurationSection("data") == null) {
            return;
        }
        try {
            Objects.requireNonNull(data.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                @Nullable
                final Map<String, Object> value = new HashMap<>();

                final ArrayList<PotionEffect> potions = (ArrayList<PotionEffect>) data
                        .getList(HEADER + key + ".Potions");
                value.put("Potions", potions);

                final int waterBubbles = data.getInt(HEADER + key + ".Water bubbles");
                value.put("Water bubbles", waterBubbles);

                final Map<String, Boolean> mobs = new HashMap<>();
                Objects.requireNonNull(data.getConfigurationSection(HEADER + key + ".Mobs")).getKeys(false)
                        .forEach(mobKey -> mobs.put(mobKey, data.getBoolean(HEADER + key + ".Mobs" + mobKey)));
                value.put("Mobs", mobs);

                final int fireTicks = data.getInt(HEADER + key + ".Fire ticks");
                value.put("Fire ticks", fireTicks);

                final Location location = data.getLocation(HEADER + key + ".Location");
                value.put("Location", location);

                String placeHolder = data.getString(HEADER + key + ".PlaceholderUUID");
                value.put("PlaceholderUUID", placeHolder);

                boolean needsMob = data.getBoolean(HEADER + key + "NeedsMob");
                value.put("NeedsMob", needsMob);
                
                state.put(key, State.fromMap(value, plugin));
            });
        } catch (final NullPointerException ignored) {
        }
    }

    private static void setData(String key, Object value) {
        data.set(HEADER + key, value);
    }

}
