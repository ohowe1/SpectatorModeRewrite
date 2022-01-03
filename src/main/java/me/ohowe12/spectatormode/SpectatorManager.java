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

import de.myzelyam.api.vanish.VanishAPI;
import me.ohowe12.spectatormode.state.StateHolder;
import me.ohowe12.spectatormode.util.Messenger;

import me.ohowe12.spectatormode.util.SpectatorEligibilityChecker;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorManager {
    private static final PotionEffect NIGHT_VISION =
            new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 1);
    private static final PotionEffect CONDUIT =
            new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 1);

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

    public void togglePlayer(Player player, boolean forced, boolean silenceMessages) {
        if (!spectatorEnabled && !forced) {
            Messenger.send(player, "disabled-message");
            return;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            toggleToSurvival(player, silenceMessages);
        } else {
            if (plugin.getConfigManager().getInt("stand-still-ticks") > 0 && !forced) {
                if (stateHolder.isPlayerAwaiting(player)) {
                    stateHolder.removePlayerAwaitingFromCanceled(player);
                } else {
                    Messenger.send(player, "stand-still-message");
                    stateHolder.addPlayerAwaiting(player, () -> {
                        toggleToSpectator(player, false, silenceMessages);
                        stateHolder.removePlayerAwaitingFromRan(player);
                    });
                }
            } else {
                toggleToSpectator(player, forced, silenceMessages);
            }
        }
    }

    public void togglePlayer(Player player, boolean forced) {
        togglePlayer(player, forced, false);
    }

    public void togglePlayer(Player player) {
        togglePlayer(player, false);
    }

    public void toggleToSpectator(Player target, boolean forced, boolean messagesForcedSilenced) {
        plugin.getPluginLogger().debugLog("Toggling " + target.getName() + " to spectator mode");
        if (canGoIntoSpectator(target, forced)) {
            if (stateHolder.hasPlayer(target)) {
                stateHolder.removePlayer(target);
            }

            stateHolder.addPlayer(target);

            addToKickers(target);
            removeAllPotionEffects(target);
            removeLeads(target);

            target.setGameMode(GameMode.SPECTATOR);
            addSpectatorEffectsIfEnabled(target);

            stateHolder.save();

            sendMessageIfNotSilenced(target, GameMode.SPECTATOR, messagesForcedSilenced);
        }
    }

    public void toggleToSurvival(Player target, boolean messagesForcedSilenced) {
        plugin.getPluginLogger().debugLog("Toggling" + target.getName() + " to survival mode");
        if (stateHolder.hasPlayer(target)) {
            removeSpectatorEffects(target);

            stateHolder.getPlayer(target).resetPlayer(target);
            stateHolder.removePlayer(target);

            removeFromKicker(target);
            target.setGameMode(GameMode.SURVIVAL);

            stateHolder.save();

            sendMessageIfNotSilenced(target, GameMode.SURVIVAL, messagesForcedSilenced);
        } else {
            Messenger.send(target, "not-in-state-message");
        }
    }

    private void addToKickers(Player target) {
        if (plugin.getConfigManager().getInt("spectator-ticks") > 0) {
            stateHolder.addPlayerKicker(target);
        }
    }

    private void removeFromKicker(Player target) {
        if (plugin.getConfigManager().getInt("spectator-ticks") > 0) {
            stateHolder.cancelKicker(target);
        }
    }

    private boolean canGoIntoSpectator(Player player, boolean forced) {
        SpectatorEligibilityChecker.EligibilityStatus status = SpectatorEligibilityChecker.getStatus(player, forced, plugin.getConfigManager());

        if (status == SpectatorEligibilityChecker.EligibilityStatus.CAN_GO) {
            return true;
        }

        Messenger.send(player, status.getMessage());
        return false;
    }

    private void removeAllPotionEffects(Player target) {
        for (PotionEffect e : target.getActivePotionEffects()) {
            target.removePotionEffect(e.getType());
        }
    }

    private void addSpectatorEffectsIfEnabled(Player target) {
        if (plugin.getConfigManager().getBoolean("night-vision")) {
            target.addPotionEffect(NIGHT_VISION);
        }
        if (plugin.getConfigManager().getBoolean("conduit")) {
            target.addPotionEffect(CONDUIT);
        }
        if (plugin.getConfigManager().getBoolean("supervanish-hook")) {
            VanishAPI.hidePlayer(target);
        }
    }

    public void removeSpectatorEffects(Player target) {
        if (plugin.getConfigManager().getBoolean("supervanish-hook")) {
            VanishAPI.showPlayer(target);
        }
        target.removePotionEffect(NIGHT_VISION.getType());
        target.removePotionEffect(CONDUIT.getType());
    }

    private void removeLeads(Player target) {
        if (plugin.isUnitTest()) {
            return;
        }
        List<LivingEntity> leads =
                target.getNearbyEntities(11, 11, 11).stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .map(entity -> (LivingEntity) entity)
                        .filter(LivingEntity::isLeashed)
                        .filter(entity -> entity.getLeashHolder() instanceof Player)
                        .filter(entity -> entity.getLeashHolder().equals(target))
                        .collect(Collectors.toList());
        for (LivingEntity entity : leads) {
            entity.setLeashHolder(null);
            HashMap<Integer, ItemStack> failedItems =
                    target.getInventory().addItem(new ItemStack(Material.LEAD));
            for (Map.Entry<Integer, ItemStack> item : failedItems.entrySet()) {
                target.getWorld().dropItemNaturally(target.getLocation(), item.getValue());
            }
        }
    }

    private void sendMessageIfNotSilenced(Player target, GameMode gameMode, boolean forceSilence) {
        if (!plugin.getConfigManager().getBoolean("disable-switching-message") && !forceSilence) {
            Messenger.send(
                    target,
                    gameMode == GameMode.SURVIVAL
                            ? "survival-mode-message"
                            : "spectator-mode-message");
        }
    }

    public void togglePlayerEffects(Player player) {
        if (!stateHolder.hasPlayer(player)) {
            Messenger.send(player, "no-spectator-message");
            return;
        }
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)
                || player.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        } else {
            addSpectatorEffectsIfEnabled(player);
        }
    }
}
