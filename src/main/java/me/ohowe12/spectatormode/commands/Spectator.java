/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.commands;

import me.ohowe12.spectatormode.DataSaver;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.State;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class Spectator implements CommandExecutor {
    // For testing
    public boolean issEnabled() {
        return sEnabled;
    }

    private boolean sEnabled;
    private boolean nightVisionEnabled;
    private boolean conduitEnabled;
    private final SpectatorMode plugin;
    private List<String> worlds;

    private final @NotNull Map<String, State> state;
    @NotNull
    private final PotionEffect NIGHTVISON = new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10);
    private final PotionEffect CONDUIT = new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10);

    public Spectator(SpectatorMode plugin) {
        this.plugin = plugin;
        state = new HashMap<>();
        plugin.saveDefaultConfig();
        sEnabled = plugin.getConfigManager().getBoolean("enabled");
    }


    public boolean inState(String uuid) {
        return this.state.containsKey(uuid);
    }

    public State getState(String uuid) {
        return this.state.get(uuid);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
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
                @NotNull Player player = (Player) sender;
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
                    if (!sender.hasPermission("spectator-reload")) {
                        sender.sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
                        return true;
                    }
                    plugin.reloadConfig();
                    sender.sendMessage(plugin.getConfigManager().getColorizedString("reload-message"));
                    return true;
            }

            if (sender.hasPermission("spectator-force")) {
                @Nullable Player target = Bukkit.getPlayerExact(argument);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getColorizedString("invalid-player-message"));
                    return true;
                }
                if (Bukkit.getOnlinePlayers().contains(target)) {
                    if (checkIfEligibleForSpectatorMode(target)) {
                        sender.sendMessage(plugin.getConfigManager().getColorizedString("force-success").replaceAll("/target/", target.getName()));
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getColorizedString("force-fail").replaceAll("/target/", target.getName()));
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
        if (!sender.hasPermission("spectator-enable")) {
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

    private boolean checkIfEligibleForSpectatorMode(@NotNull Player player) {
        if (!player.hasPermission("spectator-use")) {
            player.sendMessage(plugin.getConfigManager().getColorizedString("permission-message"));
            return false;
        }
        @NotNull GameMode gm = player.getGameMode();
        if (!gm.equals(GameMode.SPECTATOR)) {
            assert worlds != null;
            if ((!worlds.contains(player.getWorld().getName())) && plugin.getConfigManager().getBoolean("enforce-worlds")) {
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
        target.sendMessage(ChatColor.DARK_RED + "An error has occurred.");

        target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        target.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        target.setGameMode(GameMode.SURVIVAL);
    }

    private void goIntoSurvivalMode(@NotNull Player target) {
        target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        target.removePotionEffect(PotionEffectType.CONDUIT_POWER);

        target.setGameMode(GameMode.SURVIVAL);

        setState(target);

        setMobs(target);

        state.remove(target.getUniqueId().toString());
        target.sendMessage(plugin.getConfigManager().getColorizedString("survival-mode-message"));
        if (!plugin.getUnitTest()) {
            DataSaver.save(state);
        }
    }

    private void goIntoSpectatorMode(@NotNull Player target) {
        state.put(target.getUniqueId().toString(), new State(target));

        for (@NotNull PotionEffect e : target.getActivePotionEffects()) {
            target.removePotionEffect(e.getType());
        }

        target.setGameMode(GameMode.SPECTATOR);
        target.sendMessage(plugin.getConfigManager().getColorizedString("spectator-mode-message"));
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


    private void setMobs(@NotNull Player player) {
        @NotNull Location loc = state.get(player.getUniqueId().toString()).getPlayerLocation();
        @Nullable World world = loc.getWorld();
        assert world != null;
        @NotNull Chunk defaultChunk = world.getChunkAt(loc);

        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                world.getChunkAt(defaultChunk.getX() + x, defaultChunk.getZ() + z);
            }
        }

        for (@NotNull HashMap.Entry<String, Boolean> entry : state.get(player.getUniqueId().toString()).getMobIds().entrySet()) {
            @NotNull UUID key = UUID.fromString(entry.getKey());
            if ((!(Bukkit.getEntity(key) instanceof LivingEntity))) {
                return;
            }
            @NotNull LivingEntity e = (LivingEntity) Objects.requireNonNull(Bukkit.getEntity(key));

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

}
