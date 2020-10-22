package me.ohowe12.spectatormode;

import org.bukkit.Location;
import org.bukkit.Material;
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

    public static void create(@NotNull Player target){
        if(!SpectatorMode.getInstance().getConfigManager().getBoolean("placeholder-mob")) return;

        Zombie placeholder = (Zombie) target.getWorld().spawnEntity(getStateOfPlayer(target).getPlayerLocation(), EntityType.ZOMBIE);
        placeholder.setAI(false);
        placeholder.setInvulnerable(true);
        placeholder.setCanPickupItems(false);
        placeholder.setCollidable(false);
        placeholder.setSilent(true);
        placeholder.setCustomName(target.getDisplayName());
        placeholder.setCustomNameVisible(true);
        placeholder.setBaby(true);

        EntityEquipment placeholderEquip = Objects.requireNonNull(placeholder.getEquipment());
        EntityEquipment playerEquip = Objects.requireNonNull(target.getEquipment());
        placeholderEquip.setArmorContents(playerEquip.getArmorContents());
        placeholderEquip.setItemInMainHand(new ItemStack(Material.AIR));
        placeholderEquip.setItemInMainHand(playerEquip.getItemInMainHand());
        placeholderEquip.setItemInOffHand(playerEquip.getItemInOffHand());
        placeholderEquip.setHelmet(getPlayerHead(target));
        getStateOfPlayer(target).setPlaceholder(placeholder);
    }

    private static State getStateOfPlayer(@NotNull Player target){
        SpectatorMode plugin = SpectatorMode.getInstance();
        String id = target.getUniqueId().toString();
        return plugin.getSpectatorCommand().getState(id);
    }

    private static ItemStack getPlayerHead(@NotNull Player target){
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = ((SkullMeta) Objects.requireNonNull(skull.getItemMeta()));
        meta.setOwningPlayer(target);
        skull.setItemMeta(meta);
        return skull;
    }

    public static void remove(@NotNull Player target){
        LivingEntity placeholder = getStateOfPlayer(target).getPlaceholder();
        if(placeholder != null) placeholder.remove();
    }
}
