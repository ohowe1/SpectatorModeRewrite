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

import java.util.*;

public class State {

    private final SpectatorMode plugin;
    private final Location playerLocation;
    private final int fireTicks;
    private final List<PotionEffect> potionEffects;
    private final int waterBubbles;
    private final Map<String, Boolean> mobIds;

    public State(@NotNull StateBuilder builder, @NotNull SpectatorMode plugin) {
        this.plugin = plugin;
        this.mobIds = builder.mobIds;
        this.playerLocation = builder.playerLocation;
        this.fireTicks = builder.fireTicks;
        this.potionEffects = builder.potionEffects;
        this.waterBubbles = builder.waterBubbles;
    }

    public static State fromPlayer(@NotNull Player player, @NotNull SpectatorMode plugin) {
        Validate.notNull(player);
        State state =
                new StateBuilder(plugin)
                        .setPlayerLocation(player.getLocation())
                        .setFireTicks(player.getFireTicks())
                        .setPotionEffects(new ArrayList<>(player.getActivePotionEffects()))
                        .setWaterBubbles(player.getRemainingAir())
                        .build();
        state.prepareMobs(player);

        return state;
    }

    public Location getPlayerLocation() {
        return playerLocation;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public int getWaterBubbles() {
        return waterBubbles;
    }

    public Map<String, Boolean> getMobIds() {
        return mobIds;
    }

    public void resetPlayer(Player player) {
        player.teleport(getPlayerLocation());
        player.setFireTicks(getFireTicks());
        player.addPotionEffects(getPotionEffects());
        player.setRemainingAir(getWaterBubbles());
    }

    private void prepareMobs(Player player) {
        if (!plugin.getConfigManager().getBoolean("mobs") || plugin.isUnitTest()) {
            return;
        }
        World world = player.getWorld();
        Chunk defaultChunk = world.getChunkAt(getPlayerLocation());
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                processMobChunk(
                        world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z), player);
            }
        }
    }

    private void processMobChunk(Chunk chunk, Player player) {
        for (Entity e : chunk.getEntities()) {
            checkAndAddEntity(e, player);
        }
    }

    private void checkAndAddEntity(Entity entity, Player player) {
        if (entity instanceof LivingEntity) {
            @NotNull LivingEntity living = (LivingEntity) entity;
            if (entity instanceof Player) {
                return;
            }
            if (living.getRemoveWhenFarAway()) {
                addLivingEntity(living, player);
            }
        }
    }

    private void addLivingEntity(LivingEntity living, Player player) {
        living.setRemoveWhenFarAway(false);
        boolean targeted = false;
        if (living instanceof Mob) {
            @NotNull Mob m = (Mob) living;
            if (m.getTarget() instanceof Player) {
                targeted = m.getTarget().equals(player);
            }
            mobIds.put(living.getUniqueId().toString(), targeted);
        }
    }

    public void unPrepareMobs(Player player) {
        if (!plugin.getConfigManager().getBoolean("mobs") || plugin.isUnitTest()) {
            return;
        }
        @NotNull Location loc = getPlayerLocation();
        World world = loc.getWorld();

        @NotNull Chunk defaultChunk = world.getChunkAt(loc);

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

    public static class StateBuilder {
        private final SpectatorMode plugin;
        private Location playerLocation;
        private int fireTicks = -30;
        private List<PotionEffect> potionEffects = new ArrayList<>();
        private int waterBubbles = 300;
        private Map<String, Boolean> mobIds = new HashMap<>();

        public StateBuilder(SpectatorMode plugin) {
            this.plugin = plugin;
        }

        public State build() {
            return new State(this, plugin);
        }

        public StateBuilder setPlayerLocation(Location playerLocation) {
            this.playerLocation = playerLocation;
            return this;
        }

        public StateBuilder setFireTicks(int fireTicks) {
            this.fireTicks = fireTicks;
            return this;
        }

        public StateBuilder setPotionEffects(List<PotionEffect> potionEffects) {
            this.potionEffects = potionEffects;
            return this;
        }

        public StateBuilder setWaterBubbles(int waterBubbles) {
            this.waterBubbles = waterBubbles;
            return this;
        }

        public StateBuilder setMobIds(Map<String, Boolean> mobIds) {
            this.mobIds = mobIds;
            return this;
        }
    }
}
