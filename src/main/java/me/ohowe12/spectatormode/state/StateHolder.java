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

package me.ohowe12.spectatormode.state;

import me.ohowe12.spectatormode.SpectatorMode;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StateHolder {
    private final Map<String, State> stateMap = new HashMap<>();
    private final SpectatorMode plugin;

    private final File dataFileLocation;
    private final FileConfiguration dataFile;

    public StateHolder(SpectatorMode plugin) {
        this(plugin, new File(plugin.getDataFolder(), "data.yml"));
    }

    public StateHolder(SpectatorMode plugin, File dataFileLocation) {
        this.plugin = plugin;
        this.dataFileLocation = dataFileLocation;
        this.dataFile = YamlConfiguration.loadConfiguration(dataFileLocation);
        load();
    }

    public void addPlayer(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null!");
        stateMap.put(player.getUniqueId().toString(), new State(player, plugin));
    }

    public boolean hasPlayer(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null");
        return hasPlayer(player.getUniqueId());
    }

    public boolean hasPlayer(@NotNull UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        return stateMap.containsKey(uuid.toString());
    }

    public State getPlayer(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null");
        return getPlayer(player.getUniqueId());
    }

    public State getPlayer(@NotNull UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        return stateMap.get(uuid.toString());
    }

    public void removePlayer(@NotNull UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        if (!hasPlayer(uuid)) {
            return;
        }
        stateMap.get(uuid.toString()).unPrepareMobs();
        stateMap.remove(uuid.toString());
    }

    public void removePlayer(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null");
        removePlayer(player.getUniqueId());
    }

    public Set<String> allPlayersInState() {
        return stateMap.keySet();
    }

    public void save() {
        if (dataFile.getConfigurationSection("data") != null) {
            Objects.requireNonNull(dataFile.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                if (!hasPlayer(UUID.fromString(key))) {
                    dataFile.set("data." + key, null);
                }
            });
        }
        for (@NotNull final Map.Entry<String, State> entry : stateMap.entrySet()) {
            dataFile.set("data." + entry.getKey(), entry.getValue().serialize());
        }
        try {
            dataFile.save(dataFileLocation);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        load(dataFile);
    }

    public void load(FileConfiguration file) {
        if (file.getConfigurationSection("data") == null) {
            return;
        }
        try {
            Objects.requireNonNull(file.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                final Map<String, Object> value = new HashMap<>();

                @SuppressWarnings("unchecked") final ArrayList<PotionEffect> potions =
                        (ArrayList<PotionEffect>) file
                        .getList("data." + key + ".Potions");
                value.put("Potions", potions);

                final int waterBubbles = file.getInt("data." + key + ".Water bubbles");
                value.put("Water bubbles", waterBubbles);

                final Map<String, Boolean> mobs = new HashMap<>();
                ConfigurationSection mobsConfigSection = file.getConfigurationSection("data." + key + ".Mobs");
                if (mobsConfigSection != null) {
                    for (String mobKey : mobsConfigSection.getKeys(false)) {
                        mobs.put(mobKey, file.getBoolean("data." + key + ".Mobs" + mobKey));
                    }
                }
                value.put("Mobs", mobs);

                final int fireTicks = file.getInt("data." + key + ".Fire ticks");
                value.put("Fire ticks", fireTicks);

                final Location location = file.getLocation("data." + key + ".Location");
                value.put("Location", location);

                stateMap.put(key, new State(value, plugin));
            });
        } catch (final NullPointerException ignored) {
        }
    }
}
