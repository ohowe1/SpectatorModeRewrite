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

import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.PlaceholderEntity;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.DataSaver;
import me.ohowe12.spectatormode.util.State;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Spectator implements CommandExecutor {

    private final SpectatorMode plugin;
    private final Map<String, State> state;
    private final PotionEffect NIGHTVISON = new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10);
    private final PotionEffect CONDUIT = new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10);
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
            Messenger.send(sender, "permission-message");
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
        if (argument.equalsIgnoreCase("disable")) {
            return true;
        } else if (argument.equalsIgnoreCase("enable")) {
            return true;
        } else if (argument.equalsIgnoreCase("reload")) {
            return true;
        }
        return false;
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
        }
    }

    private void attemptToReloadConfig(CommandSender sender) {
        if (!sender.hasPermission("smpspectator.reload")) {
            Messenger.send(sender, "permission-message");
        } else {
            plugin.reloadConfigManager();
            Messenger.send(sender, "reload-message");
        }
    }

    private void changeEnabled(boolean status, @NotNull CommandSender sender) {
        if (!sender.hasPermission("smpspectator.enable")) {
            Messenger.send(sender, "permission-message");
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
            Messenger.send(player, "permission-message");
            return false;
        }
        @NotNull
        GameMode gm = player.getGameMode();
        if (gm == GameMode.SPECTATOR) {
            if (!state.containsKey(player.getUniqueId().toString())) {
                playerNotInState(player);
            } else {
                goIntoSurvivalMode(player);
                return true;
            }
        } else {
            if (!worlds.contains(player.getWorld().getName())
                    && plugin.getConfigManager().getBoolean("enforce-worlds")) {
                Messenger.send(player, "world-message");
                return false;
            }
            if (player.getFallDistance() > 0) {
                Messenger.send(player, "falling-message");
                return false;
            }
            if (state.containsKey(player.getUniqueId().toString())) {
                setMobs(player);
                state.remove(player.getUniqueId().toString());
            }
            goIntoSpectatorMode(player);
            return true;
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
        sendSpectatorMessage(target);
        if (nightVisionEnabled) {
            target.addPotionEffect(NIGHTVISON);
        }
        if (conduitEnabled) {
            target.addPotionEffect(CONDUIT);
        }
        DataSaver.save(state);
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
