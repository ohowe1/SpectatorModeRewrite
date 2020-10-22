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
import java.util.Objects;
import java.util.UUID;

import me.ohowe12.spectatormode.DataSaver;
import me.ohowe12.spectatormode.PlaceholderEntity;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.State;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (!plugin.getUnitTest()) {
            DataSaver.load(state);
        }
        plugin.saveDefaultConfig();
        sEnabled = plugin.getConfigManager().getBoolean("enabled");
    }

    // For testing
    public boolean issEnabled() {
        return sEnabled;
    }

    private void setMobs(@NotNull Player player) {
        @NotNull
        Location loc = state.get(player.getUniqueId().toString()).getPlayerLocation();
        World world = loc.getWorld();
        assert world != null;
        @NotNull
        Chunk defaultChunk = world.getChunkAt(loc);

        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z);
            }
        }

        for (@NotNull
        HashMap.Entry<String, Boolean> entry : state.get(player.getUniqueId().toString()).getMobIds().entrySet()) {
            UUID key = UUID.fromString(entry.getKey());
            if ((!(Bukkit.getEntity(key) instanceof LivingEntity))) {
                return;
            }
            LivingEntity e = (LivingEntity) Objects.requireNonNull(Bukkit.getEntity(key));

            e.setRemoveWhenFarAway(true);

            if (entry.getValue() && e instanceof Mob) {
                Mob m = (Mob) e;
                m.setTarget(player);
            }
        }
    }

    private void setState(@NotNull Player player) {
        final String UUID = player.getUniqueId().toString();
        player.teleport(state.get(UUID).getPlayerLocation());
        player.setFireTicks(state.get(UUID).getFireTicks());
        player.addPotionEffects(state.get(UUID).getPotionEffects());
        player.setRemainingAir(state.get(UUID).getWaterBubbles());
    }

    public boolean inState(String uuid) {
        return this.state.containsKey(uuid);
    }

    public State getState(String uuid) {
        return this.state.get(uuid);
    }

    public Map<String, State> getAllStates(){
        return this.state;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
            @NotNull String @NotNull [] args) {
        worlds = (List<String>) plugin.getConfigManager().getList("worlds-allowed");
        nightVisionEnabled = plugin.getConfigManager().getBoolean("night-vision");
        conduitEnabled = plugin.getConfigManager().getBoolean("conduit");

        if (label.equalsIgnoreCase("s") || label.equalsIgnoreCase("spectator")) {
            if (!plugin.getUnitTest()) {
                DataSaver.load(state);
            }
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getColorizedString("console-message"));
                    return true;
                }
                @NotNull
                Player player = (Player) sender;
                if (!sEnabled) {
                    player.sendMessage(plugin.getConfigManager().getColorizedString("disabled-message"));
                    return true;
                }
                checkIfEligibleForSpectatorMode(player);
                return true;
            }
            String argument = args[0];
            switch (argument.toLowerCase()) {
                case "disable":
                    changeEnabled(false, sender);
                    return true;
                case "enable":
                    changeEnabled(true, sender);
                    return true;
                case "reload":
                    if (!sender.hasPermission("smpspectator.reload")) {
                        sender.sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
                        return true;
                    }
                    plugin.reloadConfigManager();
                    sender.sendMessage(plugin.getConfigManager().getColorizedString("reload-message"));
                    return true;
            }

            if (sender.hasPermission("smpspectator.force")) {
                @Nullable
                Player target = Bukkit.getPlayerExact(argument);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getColorizedString("invalid-player-message"));
                    return true;
                }
                if (Bukkit.getOnlinePlayers().contains(target)) {
                    if (checkIfEligibleForSpectatorMode(target, true)) {
                        sender.sendMessage(plugin.getConfigManager().getColorizedString("force-success")
                                .replaceAll("/target/", target.getName()));
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getColorizedString("force-fail")
                                .replaceAll("/target/", target.getName()));
                    }
                }
            } else {
                sender.sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
            }

            return true;

        }
        return false;

    }

    private void changeEnabled(boolean status, @NotNull CommandSender sender) {
        if (!sender.hasPermission("smpspectator.enable")) {
            sender.sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
            return;
        }
        sEnabled = status;
        if (status) {
            sender.sendMessage(plugin.getConfigManager().getColorizedString("enable-message"));
        } else {
            sender.sendMessage(plugin.getConfigManager().getColorizedString("disable-message"));
        }
    }

    private boolean checkIfEligibleForSpectatorMode(@NotNull Player player){
        return checkIfEligibleForSpectatorMode(player, false);
    }

    private boolean checkIfEligibleForSpectatorMode(@NotNull Player player, boolean force) {
        if (!player.hasPermission("smpspectator.use") && !force) {
            player.sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
            return false;
        }
        @NotNull
        GameMode gm = player.getGameMode();
        if (!gm.equals(GameMode.SPECTATOR)) {
            assert worlds != null;
            if ((!worlds.contains(player.getWorld().getName()))
                    && plugin.getConfigManager().getBoolean("enforce-worlds")) {
                player.sendMessage(plugin.getConfigManager().getColorizedString("world-message"));
                return false;
            }
            if (!player.isOnGround()) {
                player.sendMessage(plugin.getConfigManager().getColorizedString("falling-message"));
                return false;
            }
            if (state.containsKey(player.getUniqueId().toString())) {
                setMobs(player);
                state.remove(player.getUniqueId().toString());
            }
            goIntoSpectatorMode(player);
            return true;
        } else {
            if (!state.containsKey(player.getUniqueId().toString())) {
                playerNotInState(player);
            } else {
                goIntoSurvivalMode(player);
            }
        }
        return true;
    }

    private void playerNotInState(@NotNull Player target) {
        target.sendMessage(plugin.getConfigManager().getColorizedString("not-in-state-message"));

        target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        target.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        target.setGameMode(GameMode.SURVIVAL);
    }

    public void goIntoSurvivalMode(@NotNull Player target){
        goIntoSurvivalMode(target, false);
    }

    public void goIntoSurvivalMode(@NotNull Player target, boolean silent) {
        PlaceholderEntity.remove(target);
        target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        target.removePotionEffect(PotionEffectType.CONDUIT_POWER);

        target.setGameMode(GameMode.SURVIVAL);

        setState(target);

        setMobs(target);

        state.remove(target.getUniqueId().toString());
        if(!silent){
            sendSurvivalMessage(target);
        }
        if (!plugin.getUnitTest()) {
            DataSaver.save(state);
        }
    }

    private void goIntoSpectatorMode(@NotNull Player target) {
        state.put(target.getUniqueId().toString(), new State(target));
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
        if (!plugin.getUnitTest()) {
            DataSaver.save(state);
        }
    }

    private void sendSurvivalMessage(Player target) {
        if (!plugin.getConfigManager().getBoolean("disable-switching-message")) {
            target.sendMessage(plugin.getConfigManager().getColorizedString("survival-mode-message"));
        }
    }

    private void sendSpectatorMessage(Player target) {
        if (!plugin.getConfigManager().getBoolean("disable-switching-message")) {
            target.sendMessage(plugin.getConfigManager().getColorizedString("spectator-mode-message"));
        }
    }

}
