/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import org.jetbrains.annotations.Nullable;

import me.ohowe12.spectatormode.SpectatorMode;

@SuppressWarnings("unchecked")
public class State {

    private Player player;
    private final SpectatorMode plugin;
    private Location playerLocation;
    private int fireTicks;
    private ArrayList<PotionEffect> potionEffects;
    private int waterBubbles;
    private Map<String, Boolean> mobIds;
    @Nullable
    private LivingEntity placeholder;

    private State(@NotNull Player player, @NotNull SpectatorMode plugin) {
        this.player = player;
        this.plugin = plugin;
        playerLocation = player.getLocation();
        fireTicks = player.getFireTicks();
        potionEffects = (ArrayList<PotionEffect>) player.getActivePotionEffects();
        waterBubbles = player.getRemainingAir();
        prepareMobs();
    }

    private State(@NotNull Map<String, Object> serialized, @NotNull SpectatorMode plugin) {
        this.plugin = plugin;
        deserialize(serialized);
    }

    public @Nullable LivingEntity getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(@Nullable LivingEntity placeholder) {
        this.placeholder = placeholder;
    }

    public static State fromMap(@NotNull Map<String, Object> serialized, SpectatorMode plugin) {
        return new State(serialized, plugin);
    }

    public static State fromPlayer(@NotNull Player player, SpectatorMode plugin) {
        return new State(player, plugin);
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
                for (Entity e : world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z).getEntities()) {
                    if (e instanceof LivingEntity) {
                        @NotNull
                        LivingEntity living = (LivingEntity) e;
                        if (e instanceof Player) {
                            continue;
                        }
                        if (living.getRemoveWhenFarAway()) {
                            living.setRemoveWhenFarAway(false);
                            boolean targeted = false;
                            if (living instanceof Mob) {
                                @NotNull
                                Mob m = (Mob) living;
                                if (m.getTarget() instanceof Player) {
                                    targeted = player.equals((Player) m.getTarget());
                                }
                                mobIds.put(living.getUniqueId().toString(), targeted);
                            }
                        }
                    }
                }

            }
        }
    }

    public void unPrepareMobs() {
        @NotNull
        Location loc = getPlayerLocation();
        World world = loc.getWorld();
        assert world != null;
        @NotNull
        Chunk defaultChunk = world.getChunkAt(loc);

        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z).addPluginChunkTicket(plugin);
            }
        }

        for (@NotNull
        HashMap.Entry<String, Boolean> entry : getMobIds().entrySet()) {
            UUID key = UUID.fromString(entry.getKey());
            if (!(Bukkit.getEntity(key) instanceof LivingEntity)) {
                continue;
            }
            LivingEntity e = (LivingEntity) Bukkit.getEntity(key);

            e.setRemoveWhenFarAway(true);

            if (entry.getValue() && e instanceof Mob) {
                Mob m = (Mob) e;
                m.setTarget(player);
            }
        }
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z).removePluginChunkTicket(plugin);
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
