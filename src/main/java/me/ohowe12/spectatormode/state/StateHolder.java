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
import me.ohowe12.spectatormode.util.PlaceholderEntity;
import org.bukkit.Location;
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
    private final FileConfiguration dataFile;

    public StateHolder(SpectatorMode plugin) {
        this.plugin = plugin;
        this.dataFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));
    }

    public void addPlayer(Player player) {
        stateMap.put(player.getUniqueId().toString(), new State(player, plugin));
    }

    public boolean hasPlayer(Player player) {
        return hasPlayer(player.getUniqueId().toString());
    }

    public boolean hasPlayer(String uuid) {
        return stateMap.containsKey(uuid);
    }

    public State getPlayer(Player player) {
        return getPlayer(player.getUniqueId().toString());
    }

    public State getPlayer(String uuid) {
        return stateMap.get(uuid);
    }

    public void removePlayer(String uuid) {
        stateMap.get(uuid).unPrepareMobs();
        PlaceholderEntity.remove(stateMap.get(uuid));
        stateMap.remove(uuid);
    }

    public void removePlayer(Player player) {
        removePlayer(player.getUniqueId().toString());
    }

    public Set<String> allPlayersInState() {
        return stateMap.keySet();
    }
    public Collection<State> allStates() {
        return stateMap.values();
    }

    public void save() {
        if (dataFile.getConfigurationSection("data") != null) {
            Objects.requireNonNull(dataFile.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                if (!hasPlayer(key)) {
                    dataFile.set("data." + key, null);
                }
            });
        }
        for (@NotNull
        final Map.Entry<String, State> entry : stateMap.entrySet()) {
            dataFile.set("data." + entry.getKey(), entry.getValue().serialize());
        }
        try {
            dataFile.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (dataFile.getConfigurationSection("data") == null) {
            return;
        }
        try {
            Objects.requireNonNull(dataFile.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                final Map<String, Object> value = new HashMap<>();

                @SuppressWarnings("unchecked")
                final ArrayList<PotionEffect> potions = (ArrayList<PotionEffect>) dataFile
                        .getList("data." + key + ".Potions");
                value.put("Potions", potions);

                final int waterBubbles = dataFile.getInt("data." + key + ".Water bubbles");
                value.put("Water bubbles", waterBubbles);

                final Map<String, Boolean> mobs = new HashMap<>();
                Objects.requireNonNull(dataFile.getConfigurationSection("data." + key + ".Mobs")).getKeys(false)
                        .forEach(mobKey -> mobs.put(mobKey, dataFile.getBoolean("data." + key + ".Mobs" + mobKey)));
                value.put("Mobs", mobs);

                final int fireTicks = dataFile.getInt("data." + key + ".Fire ticks");
                value.put("Fire ticks", fireTicks);

                final Location location = dataFile.getLocation("data." + key + ".Location");
                value.put("Location", location);

                String placeHolder = dataFile.getString("data." + key + ".PlaceholderUUID");
                value.put("PlaceholderUUID", placeHolder);

                boolean needsMob = dataFile.getBoolean("data." + key + "NeedsMob");
                value.put("NeedsMob", needsMob);

                stateMap.put(key, new State(value, plugin));
            });
        } catch (final NullPointerException ignored) {
        }
    }
}
