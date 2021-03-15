/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.PlaceholderEntity;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.DataSaver;
import me.ohowe12.spectatormode.util.State;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Spectator implements CommandExecutor {

    private static final String PERMISSIONMESSAGE = "permission-message";

    private final SpectatorMode plugin;
    private final Map<String, State> state;
    private final PotionEffect nightVisionEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10);
    private final PotionEffect conduitEffect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10);
    private boolean sEnabled;
    private boolean nightVisionEnabled;
    private boolean conduitEnabled;
    private List<String> worlds;

    public Spectator(SpectatorMode plugin) {
        this.plugin = plugin;
        state = new HashMap<>();
        DataSaver.load(state);
        plugin.saveDefaultConfig();
        sEnabled = plugin.getConfigManager().getBoolean("enabled");
    }

    // For testing
    public boolean issEnabled() {
        return sEnabled;
    }

    private void setMobs(@NotNull Player player) {
        state.get(player.getUniqueId().toString()).unPrepareMobs();
    }

    public boolean inState(String uuid) {
        return this.state.containsKey(uuid);
    }

    public State getState(String uuid) {
        return this.state.get(uuid);
    }

    public void save() {
        DataSaver.save(state);
    }

    public Map<String, State> getAllStates() {
        return this.state;
    }

    @SuppressWarnings("unchecked")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
            @NotNull String @NotNull [] args) {
        worlds = (List<String>) plugin.getConfigManager().getList("worlds-allowed");
        nightVisionEnabled = plugin.getConfigManager().getBoolean("night-vision");
        conduitEnabled = plugin.getConfigManager().getBoolean("conduit");

        if (label.equalsIgnoreCase("s") || label.equalsIgnoreCase("spectator")) {
            processPlayerCommand(sender, args);
            return true;
        }
        return false;
    }

    private void processPlayerCommand(CommandSender sender, String[] args) {
        DataSaver.load(state);
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                Messenger.send(sender, "console-message");
                return;
            }
            @NotNull
            Player player = (Player) sender;
            if (!sEnabled) {
                Messenger.send(sender, "disabled-message");
                return;
            }
            checkAndChangeGamemode(player);
            return;
        }
        String argument = args[0];
        if (isSpecialArgument(argument)) {
            checkAndExecuteDisable(sender, argument);
            return;
        }

        if (sender.hasPermission("smpspectator.force")) {
            forcePlayer(sender, argument);
        } else {
            Messenger.send(sender, PERMISSIONMESSAGE);
        }
    }

    private void forcePlayer(CommandSender sender, String argument) {
        Player target = plugin.getServer().getPlayerExact(argument);
        if (target == null) {
            Messenger.send(sender, "invalid-player-message");
            return;
        }
        if (plugin.getServer().getOnlinePlayers().contains(target)) {
            if (checkAndChangeGamemode(target, true)) {
                Messenger.send(sender, target, "force-success", target.getGameMode().name());
            } else {
                Messenger.send(sender, target, "force-fail",
                        target.getGameMode() == GameMode.SPECTATOR ? GameMode.SURVIVAL.name()
                                : GameMode.SPECTATOR.name());
            }
        }
    }

    private boolean isSpecialArgument(String argument) {
        return argument.equalsIgnoreCase("disable") || argument.equalsIgnoreCase("enable")
                || argument.equalsIgnoreCase("reload");
    }

    private void checkAndExecuteDisable(CommandSender sender, String argument) {
        switch (argument.toLowerCase()) {
        case "disable":
            changeEnabled(false, sender);
            break;
        case "enable":
            changeEnabled(true, sender);
            break;
        case "reload":
            attemptToReloadConfig(sender);
            break;
        default:

        }
    }

    private void attemptToReloadConfig(CommandSender sender) {
        if (!sender.hasPermission("smpspectator.reload")) {
            Messenger.send(sender, PERMISSIONMESSAGE);
        } else {
            plugin.reloadConfigManager();
            Messenger.send(sender, "reload-message");
        }
    }

    private void changeEnabled(boolean status, @NotNull CommandSender sender) {
        if (!sender.hasPermission("smpspectator.enable")) {
            Messenger.send(sender, PERMISSIONMESSAGE);
            return;
        }
        sEnabled = status;
        if (status) {
            Messenger.send(sender, "enable-message");
        } else {
            Messenger.send(sender, "disable-message");
        }
    }

    private boolean checkAndChangeGamemode(@NotNull Player player) {
        return checkAndChangeGamemode(player, false);
    }

    private boolean checkAndChangeGamemode(@NotNull Player player, boolean force) {
        if (!player.hasPermission("smpspectator.use") && !force) {
            Messenger.send(player, PERMISSIONMESSAGE);
            return false;
        }
        @NotNull
        GameMode currentGm = player.getGameMode();
        if (currentGm == GameMode.SPECTATOR) {
            prepareSurvival(player);
        } else {
            return prepareSpectator(player);
        }
        return true;
    }

    private boolean prepareSpectator(Player player) {
        if (!player.hasPermission("smpspectator.bypass")) {
            if (checkAndEnforceWorld(player) || checkAndEnforceHealth(player) || checkAndEnforceFalling(player)
                    || checkAndEnforceHostiles(player)) {
                return false;
            }
            if (state.containsKey(player.getUniqueId().toString())) {
                setMobs(player);
                state.remove(player.getUniqueId().toString());
            }
        }
        goIntoSpectatorMode(player);
        return true;
    }

    private void prepareSurvival(Player player) {
        if (!state.containsKey(player.getUniqueId().toString())) {
            playerNotInState(player);
        } else {
            goIntoSurvivalMode(player);
        }
    }

    private boolean checkAndEnforceFalling(Player player) {
        if (player.getFallDistance() > 0) {
            Messenger.send(player, "falling-message");
            return true;
        }
        return false;
    }

    private boolean checkAndEnforceWorld(Player player) {
        if (!worlds.contains(player.getWorld().getName()) && plugin.getConfigManager().getBoolean("enforce-worlds")) {
            Messenger.send(player, "world-message");
            return true;
        }
        return false;
    }

    private boolean checkAndEnforceHealth(Player player) {
        if (player.getHealth() < plugin.getConfigManager().getDouble("minimum-health")) {
            Messenger.send(player, "health-message");
            return true;
        }
        return false;
    }

    private boolean checkAndEnforceHostiles(Player player) {
        if (plugin.isUnitTest()) {
            return false;
        }
        double closestAllowed = plugin.getConfigManager().getDouble("closest-hostile");
        List<Entity> entites = player.getNearbyEntities(closestAllowed, closestAllowed, closestAllowed);
        for (Entity entity : entites) {
            if (entity instanceof Monster) {
                Messenger.send(player, "mob-to-close-message");
                return true;
            }
        }
        return false;
    }

    private void playerNotInState(@NotNull Player target) {
        Messenger.send(target, "not-in-state-message");

        target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        target.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        target.setGameMode(GameMode.SURVIVAL);
    }

    public void goIntoSurvivalMode(@NotNull Player target) {
        goIntoSurvivalMode(target, false);
    }

    public void goIntoSurvivalMode(@NotNull Player target, boolean silent) {
        PlaceholderEntity.remove(target);
        target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        target.removePotionEffect(PotionEffectType.CONDUIT_POWER);

        target.setGameMode(GameMode.SURVIVAL);

        state.get(target.getUniqueId().toString()).setPlayer(target);

        setMobs(target);

        state.remove(target.getUniqueId().toString());
        if (!silent) {
            sendSurvivalMessage(target);
        }
        DataSaver.save(state);
    }

    private void goIntoSpectatorMode(@NotNull Player target) {
        state.put(target.getUniqueId().toString(), State.fromPlayer(target, plugin));
        PlaceholderEntity.create(target);

        for (@NotNull
        PotionEffect e : target.getActivePotionEffects()) {
            target.removePotionEffect(e.getType());
        }

        target.setGameMode(GameMode.SPECTATOR);
        if (!plugin.isUnitTest()) {
            removeLead(target);
        }
        sendSpectatorMessage(target);
        if (nightVisionEnabled) {
            target.addPotionEffect(nightVisionEffect);
        }
        if (conduitEnabled) {
            target.addPotionEffect(conduitEffect);
        }
        DataSaver.save(state);
    }

    private void removeLead(Player target) {
        List<LivingEntity> leads = target.getNearbyEntities(11, 11, 11).stream()
                .filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity)
                .filter(LivingEntity::isLeashed).filter(entity -> entity.getLeashHolder() instanceof Player)
                .filter(entity -> ((Player) entity.getLeashHolder()).equals(target)).collect(Collectors.toList());
        for (LivingEntity entity : leads) {
            entity.setLeashHolder(null);
            HashMap<Integer, ItemStack> failedItems = target.getInventory().addItem(new ItemStack(Material.LEAD));
            for (Map.Entry<Integer, ItemStack> item : failedItems.entrySet()) {
                target.getWorld().dropItemNaturally(target.getLocation(), item.getValue());
            }
        }
    }

    private void sendSurvivalMessage(Player target) {
        if (!plugin.getConfigManager().getBoolean("disable-switching-message")) {
            Messenger.send(target, "survival-mode-message");
        }
    }

    private void sendSpectatorMessage(Player target) {
        if (!plugin.getConfigManager().getBoolean("disable-switching-message")) {
            Messenger.send(target, "spectator-mode-message");
        }
    }

}
