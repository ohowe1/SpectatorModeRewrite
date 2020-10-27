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
        mobIds = new HashMap<>();
        playerLocation = player.getLocation();
        // Temporary till it gets added to mockbukkit
        if (plugin.isUnitTest()) {
            fireTicks = -20;
        } else {
            fireTicks = player.getFireTicks();
        }
        potionEffects = new ArrayList<>(player.getActivePotionEffects());
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
        // Also temporary
        // This method is not covered by tests! Watch out
        if (plugin.isUnitTest()) {
            return;
        }
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
                targeted = player.equals((Player) m.getTarget());
            }
            mobIds.put(living.getUniqueId().toString(), targeted);
        }
    }

    public void unPrepareMobs() {
        // Also temporary
        // This method is not covered by tests! Watch out
        if (plugin.isUnitTest()) {
            return;
        }
        @NotNull
        Location loc = getPlayerLocation();
        World world = loc.getWorld();

        @NotNull
        Chunk defaultChunk = world.getChunkAt(loc);

        loadChunks(world, defaultChunk.getX(), defaultChunk.getZ());

        for (HashMap.Entry<String, Boolean> entry : getMobIds().entrySet()) {
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
