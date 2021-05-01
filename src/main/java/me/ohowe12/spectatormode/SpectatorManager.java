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

package me.ohowe12.spectatormode;

import me.ohowe12.spectatormode.state.StateHolder;
import me.ohowe12.spectatormode.util.Messenger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpectatorManager {
    private final StateHolder stateHolder;
    private final SpectatorMode plugin;
    private boolean spectatorEnabled;

    public SpectatorManager(SpectatorMode plugin) {
        this.plugin = plugin;
        this.stateHolder = new StateHolder(plugin);
        this.spectatorEnabled = plugin.getConfigManager().getBoolean("enabled");
    }

    public boolean isSpectatorEnabled() {
        return spectatorEnabled;
    }

    public void setSpectatorEnabled(boolean spectatorEnabled) {
        this.spectatorEnabled = spectatorEnabled;
    }

    public StateHolder getStateHolder() {
        return stateHolder;
    }

    public void togglePlayer(Player player, boolean forced) {
        if (!spectatorEnabled && !forced) {
            Messenger.send(player, "disabled-message");
            return;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            toggleToSurvival(player);
        } else {
            toggleToSpectator(player, forced);
        }
    }

    public void togglePlayer(Player player) {
        togglePlayer(player, false);
    }

    private void toggleToSpectator(Player target, boolean forced) {
        if (canGoIntoSpectator(target, forced)) {
            if (stateHolder.hasPlayer(target)) {
                stateHolder.removePlayer(target);
            }

            stateHolder.addPlayer(target);
            removeAllPotionEffects(target);
            removeLeads(target);

            target.setGameMode(GameMode.SPECTATOR);
            addSpectatorEffectsIfEnabled(target);

            stateHolder.save();

            sendMessageIfNotSilenced(target, GameMode.SPECTATOR);
        }
    }

    private void toggleToSurvival(Player target) {
        if (stateHolder.hasPlayer(target)) {
            removeSpectatorEffects(target);

            target.setGameMode(GameMode.SURVIVAL);
            stateHolder.getPlayer(target).resetPlayer(target);

            stateHolder.removePlayer(target);

            stateHolder.save();

            sendMessageIfNotSilenced(target, GameMode.SURVIVAL);
        } else {
            Messenger.send(target, "not-in-state-message");
        }
    }

    private boolean canGoIntoSpectator(Player player, boolean forced) {
        if (forced || player.hasPermission("smpspectator.bypass")) {
            return true;
        }
        // Has fall damage
        if (player.getFallDistance() > 0) {
            Messenger.send(player, "falling-message");
            return false;
        }
        // Health
        if (player.getHealth() < plugin.getConfigManager().getDouble("minimum-health")) {
            Messenger.send(player, "health-message");
            return false;
        }
        // Closest mob
        double closestAllowed = plugin.getConfigManager().getDouble("closest-hostile");
        List<Entity> entites = player.getNearbyEntities(closestAllowed, closestAllowed, closestAllowed);
        for (Entity entity : entites) {
            if (entity instanceof Monster) {
                Messenger.send(player, "mob-to-close-message");
                return false;
            }
        }
        // Worlds
        if (!plugin.getConfigManager().getList("worlds-allowed").contains(player.getWorld().getName()) && plugin.getConfigManager().getBoolean("enforce-worlds")) {
            Messenger.send(player, "world-message");
            return false;
        }
        return true;
    }

    private void removeAllPotionEffects(Player target) {
        for (PotionEffect e : target.getActivePotionEffects()) {
            target.removePotionEffect(e.getType());
        }
    }

    private void addSpectatorEffectsIfEnabled(Player target) {
        if (plugin.getConfigManager().getBoolean("night-vision")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10));
        }
        if (plugin.getConfigManager().getBoolean("conduit")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10));
        }
    }

    private void removeSpectatorEffects(Player target) {
        target.removePotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10).getType());
        target.removePotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10).getType());
    }

    private void removeLeads(Player target) {
        List<LivingEntity> leads = target.getNearbyEntities(11, 11, 11).stream()
                .filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity)
                .filter(LivingEntity::isLeashed).filter(entity -> entity.getLeashHolder() instanceof Player)
                .filter(entity -> entity.getLeashHolder().equals(target)).collect(Collectors.toList());
        for (LivingEntity entity : leads) {
            entity.setLeashHolder(null);
            HashMap<Integer, ItemStack> failedItems = target.getInventory().addItem(new ItemStack(Material.LEAD));
            for (Map.Entry<Integer, ItemStack> item : failedItems.entrySet()) {
                target.getWorld().dropItemNaturally(target.getLocation(), item.getValue());
            }
        }
    }

    private void sendMessageIfNotSilenced(Player target, GameMode gameMode) {
        if (!plugin.getConfigManager().getBoolean("disable-switching-message")) {
            Messenger.send(target, gameMode == GameMode.SURVIVAL ? "survival-mode-message" : "spectator-mode-message");
        }
    }
}
