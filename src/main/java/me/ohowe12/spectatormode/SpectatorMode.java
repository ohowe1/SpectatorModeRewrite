/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode;

import me.ohowe12.spectatormode.commands.Effects;
import me.ohowe12.spectatormode.commands.Spectator;
import me.ohowe12.spectatormode.commands.Speed;
import me.ohowe12.spectatormode.listener.OnCommandPreprocessListener;
import me.ohowe12.spectatormode.listener.OnLogOnListener;
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

public class SpectatorMode extends JavaPlugin {

    public Spectator getSpectatorCommand() {
        return spectatorCommand;
    }

    private Spectator spectatorCommand;

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
        if (!this.getUnitTest()) {
            int pluginId = 7132;
            new Metrics(this, pluginId);
            new UpdateChecker(this, 77267).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[SMP SPECTATOR MODE] SMP SPECTATOR MODE is all up to date at version " + this.getDescription().getVersion() + '!');
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[SMP SPECTATOR MODE] A new version of SMP SPECTATOR MODE is available (version " + version + ")! You are on version " + this.getDescription().getVersion() + ".");
                }
            });
        }
    }

    public boolean getUnitTest() {
        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void registerCommands() {
        spectatorCommand = new Spectator(this);
        Objects.requireNonNull(this.getCommand("s")).setExecutor(spectatorCommand);
        Objects.requireNonNull(this.getCommand("s")).setTabCompleter(new SpectatorTab());

        Objects.requireNonNull(this.getCommand("speed")).setExecutor(new Speed());
        Objects.requireNonNull(this.getCommand("speed")).setTabCompleter(new SpeedTab());

        Objects.requireNonNull(this.getCommand("seffect")).setExecutor(new Effects());

        getServer().getPluginManager().registerEvents(new OnMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOnListener(), this);
        getServer().getPluginManager().registerEvents(new OnCommandPreprocessListener(), this);
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return config;
    }
}
