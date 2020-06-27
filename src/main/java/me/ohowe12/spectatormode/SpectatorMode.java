/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode;

import me.ohowe12.spectatormode.commands.Spectator;
import me.ohowe12.spectatormode.commands.Speed;
import me.ohowe12.spectatormode.listener.OnMoveListener;
import me.ohowe12.spectatormode.tabCompleter.SpectatorTab;
import me.ohowe12.spectatormode.tabCompleter.SpeedTab;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public final class SpectatorMode extends JavaPlugin {

    private boolean unitTest;

    public SpectatorMode() {
        super();
        unitTest = false;
    }

    protected SpectatorMode(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        unitTest = true;
    }

    private static SpectatorMode instance;

    private ConfigManager config;

    public static SpectatorMode getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new ConfigManager(this.getConfig());
        registerCommands();
        if (!unitTest) {
            int pluginId = 7132;
            Metrics metrics = new Metrics(this, pluginId);
            new UpdateChecker(this, 77267).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[SMP SPECTATOR MODE] SMP SPECTATOR MODE is all up to date!");
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[SMP SPECTATOR MODE] A new version of SMP SPECTATOR MODE is available!");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("s")).setExecutor(new Spectator());
        Objects.requireNonNull(this.getCommand("spectator")).setExecutor(new Spectator());
        Objects.requireNonNull(this.getCommand("s")).setTabCompleter(new SpectatorTab());
        Objects.requireNonNull(this.getCommand("spectator")).setTabCompleter(new SpectatorTab());

        Objects.requireNonNull(this.getCommand("speed")).setExecutor(new Speed());
        Objects.requireNonNull(this.getCommand("speed")).setTabCompleter(new SpeedTab());
        Objects.requireNonNull(this.getCommand("sp")).setExecutor(new Speed());
        Objects.requireNonNull(this.getCommand("sp")).setTabCompleter(new SpeedTab());

        getServer().getPluginManager().registerEvents(new OnMoveListener(), this);
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return config;
    }
}
