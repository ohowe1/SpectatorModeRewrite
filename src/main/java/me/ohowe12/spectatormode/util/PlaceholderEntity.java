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

package me.ohowe12.spectatormode.util;

import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.state.State;
import me.ohowe12.spectatormode.state.StateHolder;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class PlaceholderEntity {

    private PlaceholderEntity() {

    }

    private static SpectatorMode plugin = null;
    private static StateHolder stateHolder = null;

    public static void init(SpectatorMode plugin, StateHolder stateHolder) {
        PlaceholderEntity.plugin = plugin;
        PlaceholderEntity.stateHolder = stateHolder;
    }

    public static void create(@NotNull State state) {
        if (!plugin.getConfigManager().getBoolean("placeholder-mob"))
            return;

        Zombie placeholder = (Zombie) state.getPlayerLocation().getWorld().spawnEntity(state.getPlayerLocation(),
                EntityType.ZOMBIE);
        placeholder.setAI(false);
        placeholder.setInvulnerable(true);
        placeholder.setCanPickupItems(false);
        placeholder.setCollidable(false);
        placeholder.setSilent(true);
        placeholder.setCustomName(state.getPlayer().getDisplayName());
        placeholder.setCustomNameVisible(true);
        placeholder.setBaby();

        if (placeholder.isInsideVehicle()) {
            Entity vehicle = placeholder.getVehicle();
            placeholder.leaveVehicle();
            if (vehicle != null) {
                vehicle.remove();
            }
        }
        EntityEquipment placeholderEquip = Objects.requireNonNull(placeholder.getEquipment());
        EntityEquipment playerEquip = Objects.requireNonNull(state.getPlayer().getEquipment());
        placeholderEquip.setArmorContents(playerEquip.getArmorContents());
        placeholderEquip.setItemInMainHand(new ItemStack(Material.AIR));
        placeholderEquip.setItemInMainHand(playerEquip.getItemInMainHand());
        placeholderEquip.setItemInOffHand(playerEquip.getItemInOffHand());
        placeholderEquip.setHelmet(getPlayerHead(state.getPlayer()));

        state.setPlaceholder(placeholder);
        plugin.getPluginLogger().debugLog("Added " + placeholder.getUniqueId());
        stateHolder.save();
    }

    private static State getStateOfPlayer(String uuid) {
        return stateHolder.getPlayer(uuid);
    }

    private static ItemStack getPlayerHead(@NotNull Player target) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = ((SkullMeta) Objects.requireNonNull(skull.getItemMeta()));
        meta.setOwningPlayer(target);
        skull.setItemMeta(meta);
        return skull;
    }

    public static void remove(@NotNull State state) {
        LivingEntity placeholder = state.getPlaceholder();
        if (placeholder != null) {
            placeholder.remove();
            plugin.getPluginLogger().debugLog("Removed " + placeholder.getUniqueId());
        }
    }

    public static void remove(String uuid) {
        remove(getStateOfPlayer(uuid));
    }

    public static void shutdown() {
        for (String uuid : stateHolder.allPlayersInState()) {
            remove(uuid);
        }
    }
}
