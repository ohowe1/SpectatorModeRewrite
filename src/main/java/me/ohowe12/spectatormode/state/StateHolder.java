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
import me.ohowe12.spectatormode.util.Logger;

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
        try {
            this.dataFile = YamlConfiguration.loadConfiguration(dataFileLocation);
        } catch (IllegalArgumentException exception) {
            plugin.getPluginLogger()
                    .log(
                            Logger.RED
                                    + "Your data.yml file is invalid!\n"
                                    + "This could be due to a world that has not been loaded in"
                                    + " yet");
            throw exception;
        }
        load();
    }

    public void addPlayer(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null!");
        stateMap.put(player.getUniqueId().toString(), State.fromPlayer(player, plugin));
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

    public void removePlayer(@NotNull Player player) {
        Validate.notNull(player, "Player cannot be null");
        UUID uuid = player.getUniqueId();
        if (!hasPlayer(uuid)) {
            return;
        }
        getPlayer(player).unPrepareMobs(player);
        stateMap.remove(uuid.toString());
    }

    public Set<String> allPlayersInState() {
        return stateMap.keySet();
    }

    public void save() {
        if (dataFile.getConfigurationSection("data") != null) {
            Objects.requireNonNull(dataFile.getConfigurationSection("data"))
                    .getKeys(false)
                    .forEach(
                            key -> {
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
            plugin.getPluginLogger()
                    .log(
                            Logger.RED
                                    + "Cannot save the data.yml file! This is not normal and should"
                                    + " be reported. Error message is as follows: ");
            plugin.getPluginLogger().log(e.getMessage());
        }
    }

    public void load() {
        load(dataFile);
    }

    public void load(FileConfiguration file) {
        ConfigurationSection dataSection = file.getConfigurationSection("data");
        if (dataSection == null) {
            return;
        }
        loadFromConfigurationSection(dataSection);
    }

    private void loadFromConfigurationSection(@NotNull ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection playerSection = section.getConfigurationSection(key);
            assert playerSection != null;
            State.StateBuilder stateBuilder = new State.StateBuilder(plugin);

            @SuppressWarnings("unchecked")
            final List<PotionEffect> potions =
                    (List<PotionEffect>) playerSection.getList("Potions");
            stateBuilder.setPotionEffects(potions);

            final int waterBubbles = playerSection.getInt("Water bubbles", 300);
            stateBuilder.setWaterBubbles(waterBubbles);

            ConfigurationSection mobsSection = playerSection.getConfigurationSection("Mobs");
            stateBuilder.setMobIds(loadMobs(mobsSection));

            final int fireTicks = playerSection.getInt("Fire ticks", -20);
            stateBuilder.setFireTicks(fireTicks);

            final Location location = playerSection.getLocation("Location");
            stateBuilder.setPlayerLocation(location);

            stateMap.put(key, stateBuilder.build());
        }
    }

    private Map<String, Boolean> loadMobs(ConfigurationSection mobsSection) {
        final Map<String, Boolean> mobs = new HashMap<>();
        if (mobsSection != null) {
            for (String mobKey : mobsSection.getKeys(false)) {
                if (mobsSection.isList(mobKey)) {
                    mobs.put(mobKey, mobsSection.getBooleanList(mobKey).get(0));
                } else {
                    mobs.put(mobKey, mobsSection.getBoolean(mobKey));
                }
            }
        }
        return mobs;
    }
}
