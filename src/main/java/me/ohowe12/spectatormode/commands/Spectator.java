/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode.commands;

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
    static boolean sEnabled;
    static boolean nightVisionEnabled;
    static SpectatorMode plugin;
    @Nullable
    static List<String> worlds;
    private static Spectator instance;

    public final Map<String, State> state;
    @NotNull
    final PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10);
    final FileConfiguration data;

    public Spectator() {
        instance = this;
        plugin = SpectatorMode.getInstance();
        state = new HashMap<>();
        plugin.saveDefaultConfig();
        data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));

    }

    public static Spectator getInstance() {
        return instance;
    }

    public void save() {
        if (data.getConfigurationSection("data") != null) {
            Objects.requireNonNull(data.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                if (!state.containsKey(key)) {
                    data.set("data." + key, null);
                }
            });
        }
        for (@NotNull Entry<String, State> entry : state.entrySet()) {
            data.set("data." + entry.getKey(), entry.getValue().serialize());
        }
        try {
            data.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (data.getConfigurationSection("data") == null) {
            return;
        }
        try {
            Objects.requireNonNull(data.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                @Nullable Map<String, Object> value = new HashMap<>();

                ArrayList<PotionEffect> potions = (ArrayList<PotionEffect>) data.getList("data." + key + ".Potions");
                value.put("Potions", potions);

                int waterBubbles = data.getInt("data." + key + ".Water bubbles");
                value.put("Water bubbles", waterBubbles);

                Map<String, Boolean> mobs = new HashMap<>();
                Objects.requireNonNull(data.getConfigurationSection("data." + key + ".Mobs")).getKeys(false).forEach(mobKey -> mobs.put(mobKey, data.getBoolean("data." + key + ".Mobs" + mobKey)));
                value.put("Mobs", mobs);

                int fireTicks = data.getInt("data." + key + ".Fire ticks");
                value.put("Fire ticks", fireTicks);

                Location location = data.getLocation("data." + key + ".Location");
                value.put("Location", location);

                state.put(key, new State(value));
            });
        } catch (NullPointerException ignored) {
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        worlds = (List<String>) plugin.getConfig().getList("worlds-allowed", Arrays.asList("world", "world_nether", "world_the_end"));
        sEnabled = plugin.getConfig().getBoolean("enabled", true);
        nightVisionEnabled = plugin.getConfig().getBoolean("night-vision", true);
        if (label.equalsIgnoreCase("s") || label.equalsIgnoreCase("spectator")) {
            load();

            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("console-message", "&cYou are &lnot &ca player!"))));
                    return true;
                }
                @NotNull Player player = (Player) sender;
                if (!sEnabled) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("disabled-message", "&cSpectator Mode is &lnot &r&cenabled by the server!"))));
                    return true;
                }
                checkIfEligibleForSpectatorMode(player);
                return true;
            }
            String argument = args[0];
            if (sender.hasPermission("spectator-force")) {
                @Nullable Player target = Bukkit.getPlayerExact(argument);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "That is not a valid player!");
                    return true;
                }
                if (Bukkit.getOnlinePlayers().contains(target)) {
                    sender.sendMessage("Putting " + argument + " into spectator mode");
                    checkIfEligibleForSpectatorMode(target);
                }
            }
            if (!sender.hasPermission("spectator-enable")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("permission-message", "&cYou do not have permission to do that!"))));
                return true;
            }
            switch (argument.toLowerCase()) {
                case "disable":
                    sEnabled = false;
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("disable-message", "&dSpectator mode has been &ldisabled"))));
                    break;
                case "enable":
                    sEnabled = true;
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("enable-message", "&dSpectator mode has been &lenabled"))));
                    break;
                case "reload":
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("reload-message", "&bThe config file has been reloaded!"))));
            }
            return true;

        }
        return false;

    }

    private void checkIfEligibleForSpectatorMode(@NotNull Player player) {
        if (!player.hasPermission("spectator-use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("permission-message", "&cYou do not have permission to do that!"))));
            return;
        }
        @NotNull GameMode gm = player.getGameMode();
        if (!gm.equals(GameMode.SPECTATOR)) {
            assert worlds != null;
            if ((!worlds.contains(player.getWorld().getName())) && plugin.getConfig().getBoolean("enforce-worlds", false)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("world-message", "&cHey you&l can't &r&cdo that in that world!"))));
                return;
            }
            if (!player.isOnGround()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("falling-message", "&cHey you &lcan't &r&cdo that while falling!"))));
                return;
            }
            if (state.containsKey(player.getUniqueId().toString())) {
                setMobs(player);
                state.remove(player.getUniqueId().toString());
            }
            goIntoSpectatorMode(player);
            return;
        }
        if (!state.containsKey(player.getUniqueId().toString())) {
            player.sendMessage(state.toString());
            player.sendMessage(ChatColor.DARK_RED + "An error has occurred.");

            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }
        goIntoSurvivalMode(player);
    }

    private void goIntoSurvivalMode(Player target) {
        target.removePotionEffect(PotionEffectType.NIGHT_VISION);

        target.setGameMode(GameMode.SURVIVAL);

        setState(target);

        setMobs(target);

        state.remove(target.getUniqueId().toString());
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("survival-mode-message", "&9Setting gamemode to &b&lSURVIVAL MODE"))));
        save();
    }

    private void goIntoSpectatorMode(Player target) {
        state.put(target.getUniqueId().toString(), new State(target));

        for (@NotNull PotionEffect e : target.getActivePotionEffects()) {
            target.removePotionEffect(e.getType());
        }

        target.setGameMode(GameMode.SPECTATOR);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("spectator-mode-message", "&9Setting gamemode to &b&lSPECTATOR MODE"))));
        if (nightVisionEnabled) {
            target.addPotionEffect(nightVision);
        }
        save();
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
                @NotNull Mob m = (Mob) e;
                m.setTarget(player);
            }
        }
    }


    private void setState(@NotNull Player player) {
        final String UUID = player.getUniqueId().toString();
        if (state.get(UUID) == null) {
            player.sendMessage("null on state");
        }
        if (state.get(UUID).getPlayerLocation() == null) {
            player.sendMessage("null on location");
        }
        player.teleport(state.get(UUID).getPlayerLocation());
        player.setFireTicks(state.get(UUID).getFireTicks());
        player.addPotionEffects(state.get(UUID).getPotionEffects());
        player.setRemainingAir(state.get(UUID).getWaterBubbles());
    }

}