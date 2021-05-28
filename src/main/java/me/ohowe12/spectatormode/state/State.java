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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class State {

    private Player player;
    private final SpectatorMode plugin;
    private Location playerLocation;
    private int fireTicks;
    private ArrayList<PotionEffect> potionEffects;
    private int waterBubbles;
    private Map<String, Boolean> mobIds;

    public State(@NotNull Player player, @NotNull SpectatorMode plugin) {
        this.player = player;
        this.plugin = plugin;
        mobIds = new HashMap<>();
        playerLocation = player.getLocation();
        fireTicks = player.getFireTicks();
        potionEffects = new ArrayList<>(player.getActivePotionEffects());
        waterBubbles = player.getRemainingAir();
        if (!plugin.isUnitTest())
            prepareMobs();
    }

    public State(@NotNull Map<String, Object> serialized, @NotNull SpectatorMode plugin) {
        this.plugin = plugin;
        deserialize(serialized);
    }

    public Location getPlayerLocation() {
        return playerLocation;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public ArrayList<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public int getWaterBubbles() {
        return waterBubbles;
    }

    public Map<String, Boolean> getMobIds() {
        return mobIds;
    }

    public Player getPlayer() {
        return player;
    }
    public UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    public void resetPlayer(Player player) {
        player.teleport(getPlayerLocation());
            player.setFireTicks(getFireTicks());
        player.addPotionEffects(getPotionEffects());
        player.setRemainingAir(getWaterBubbles());
    }

    private void deserialize(@NotNull Map<String, Object> serialized) {
        playerLocation = (Location) serialized.get("Location");
        fireTicks = (int) serialized.get("Fire ticks");
        potionEffects = (ArrayList<PotionEffect>) serialized.get("Potions");
        waterBubbles = (int) serialized.get("Water bubbles");
        mobIds = (Map<String, Boolean>) serialized.get("Mobs");

    }

    private void prepareMobs() {
        World world = player.getWorld();
        Chunk defaultChunk = world.getChunkAt(getPlayerLocation());
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                processMobChunk(world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z));
            }
        }
    }

    private void processMobChunk(Chunk chunk) {
        for (Entity e : chunk.getEntities()) {
            checkAndAddEntity(e);
        }
    }

    private void checkAndAddEntity(Entity e) {
        if (e instanceof LivingEntity) {
            @NotNull
            LivingEntity living = (LivingEntity) e;
            if (e instanceof Player) {
                return;
            }
            if (living.getRemoveWhenFarAway()) {
                addLivingEntity(living);
            }
        }
    }

    private void addLivingEntity(LivingEntity living) {
        living.setRemoveWhenFarAway(false);
        boolean targeted = false;
        if (living instanceof Mob) {
            @NotNull
            Mob m = (Mob) living;
            if (m.getTarget() instanceof Player) {
                targeted = player.equals(m.getTarget());
            }
            mobIds.put(living.getUniqueId().toString(), targeted);
        }
    }

    public void unPrepareMobs() {
        if (plugin.isUnitTest()) {
            return;
        }
        @NotNull
        Location loc = getPlayerLocation();
        World world = loc.getWorld();

        @NotNull
        Chunk defaultChunk = world.getChunkAt(loc);

        loadChunks(world, defaultChunk.getX(), defaultChunk.getZ());

        for (Map.Entry<String, Boolean> entry : getMobIds().entrySet()) {
            UUID key = UUID.fromString(entry.getKey());
            Entity entityEntity = Bukkit.getEntity(key);
            if (!(entityEntity instanceof LivingEntity)) {
                continue;
            }
            LivingEntity entityLiving = (LivingEntity) entityEntity;

            entityLiving.setRemoveWhenFarAway(true);

            if (entry.getValue() && entityLiving instanceof Mob) {
                Mob entityMob = (Mob) entityLiving;
                entityMob.setTarget(player);
            }
        }

        unloadChunks(world, defaultChunk.getX(), defaultChunk.getZ());

    }

    private void loadChunks(World world, int defaultX, int defaultZ) {
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                world.getChunkAt(defaultX + x, defaultZ + z).addPluginChunkTicket(plugin);
            }
        }
    }

    private void unloadChunks(World world, int defaultX, int defaultZ) {
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                world.getChunkAt(defaultX + x, defaultZ + z).removePluginChunkTicket(plugin);
            }
        }
    }

    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("Location", playerLocation);
        serialized.put("Fire ticks", fireTicks);
        serialized.put("Potions", potionEffects);
        serialized.put("Water bubbles", waterBubbles);
        serialized.put("Mobs", mobIds);
        return serialized;
    }
}
