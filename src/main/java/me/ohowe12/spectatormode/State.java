/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class State {
    private Location playerLocation;
    private int fireTicks;
    private ArrayList<PotionEffect> potionEffects;
    private int waterBubbles;
    private Map<String, Boolean> mobIds;

    public State(Player player) {
        playerLocation = player.getLocation();
        fireTicks = player.getFireTicks();
        potionEffects = (ArrayList<PotionEffect>) player.getActivePotionEffects();
        waterBubbles = player.getRemainingAir();
        prepareMobs(player);
    }

    public State(Map<String, Object> serialized) {
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

    private void deserialize(Map<String, Object> serialized) {
        playerLocation = (Location) serialized.get("Location");
        fireTicks = (int) serialized.get("Fire ticks");
        potionEffects = (ArrayList<PotionEffect>) serialized.get("Potions");
        waterBubbles = (int) serialized.get("Water bubbles");
        mobIds = (Map<String, Boolean>) serialized.get("Mobs");
    }

    private void prepareMobs(@NotNull Player player) {
        @NotNull Map<String, Boolean> ids = new HashMap<>();

        for (Entity e : player.getNearbyEntities(64, 64, 64)) {
            if (e instanceof LivingEntity) {
                @NotNull LivingEntity living = (LivingEntity) e;

                if (living.getRemoveWhenFarAway()) {
                    living.setRemoveWhenFarAway(false);
                    if (living instanceof Mob) {
                        @NotNull Mob m = (Mob) living;
                        try {
                            ids.put(m.getUniqueId().toString(), Objects.equals(m.getTarget(), player));
                        } catch (NullPointerException ignored) {

                        }
                    } else {
                        ids.put(living.getUniqueId().toString(), false);
                    }

                }
            }

        }
        mobIds = ids;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("Location", playerLocation);
        serialized.put("Fire ticks", fireTicks);
        serialized.put("Potions", potionEffects);
        serialized.put("Water bubbles", waterBubbles);
        serialized.put("Mobs", mobIds);
        return serialized;
    }
}
