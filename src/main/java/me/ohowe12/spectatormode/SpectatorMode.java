/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import me.ohowe12.spectatormode.commands.Effects;
import me.ohowe12.spectatormode.commands.Spectator;
import me.ohowe12.spectatormode.commands.Speed;
import me.ohowe12.spectatormode.listener.OnCommandPreprocessListener;
import me.ohowe12.spectatormode.listener.OnLogOnListener;
import me.ohowe12.spectatormode.listener.OnLogOffListener;
import me.ohowe12.spectatormode.listener.OnMoveListener;
import me.ohowe12.spectatormode.tabCompleter.SpectatorTab;
import me.ohowe12.spectatormode.tabCompleter.SpeedTab;
import me.ohowe12.spectatormode.util.DataSaver;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

public class SpectatorMode extends JavaPlugin {

    private static SpectatorMode instance;
    private Spectator spectatorCommand;
    private ConfigManager config;
    private final boolean unitTest;

    public boolean isUnitTest() {
        return unitTest;
    }

    public SpectatorMode() {
        super();
        unitTest = false;
    }

    protected SpectatorMode(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        unitTest = true;
    }

    @Deprecated
    public static SpectatorMode getInstance() {
        return instance;
    }

    public Spectator getSpectatorCommand() {
        return spectatorCommand;
    }

    @Override
    public void onEnable() {
        instance = this;
        PlaceholderEntity.init(this);
        config = new ConfigManager(this, this.getConfig());
        Messenger.init(this);
        DataSaver.init(this.getDataFolder(), this);
        registerCommands();
        if (!unitTest) {
            addMetrics();
            
            if (config.getBoolean("update-checker")) {
                checkUpdate();
            }
        }
    }

    

    private void addMetrics() {
        Metrics metrics = new Metrics(this, 7132);
        for(Map.Entry<String, String> entry : config.getAllBooleansAndNumbers().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            getLogger().info(key + ": " + value);
            metrics.addCustomChart(new Metrics.SimplePie(key + "_CHARTID", new Callable<String>(){
                @Override
                public String call() throws Exception {
                    return value;
                }
            }));
        }
    }

    private void checkUpdate() {
        UpdateChecker.getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.AQUA
                                + "[SMP SPECTATOR MODE] SMP SPECTATOR MODE is all up to date at version "
                                + this.getDescription().getVersion() + '!');
            } else {
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.DARK_RED
                                + "[SMP SPECTATOR MODE] A new version of SMP SPECTATOR MODE is available (version "
                                + version + ")! You are on version " + this.getDescription().getVersion() + ".");
            }
        }, this);
    }

    @Override
    public void onDisable() {
        PlaceholderEntity.shutdown();
    }

    public void registerCommands() {
        spectatorCommand = new Spectator(this);
        Objects.requireNonNull(this.getCommand("s")).setExecutor(spectatorCommand);
        Objects.requireNonNull(this.getCommand("s")).setTabCompleter(new SpectatorTab());

        Objects.requireNonNull(this.getCommand("speed")).setExecutor(new Speed(this));
        Objects.requireNonNull(this.getCommand("speed")).setTabCompleter(new SpeedTab());

        Objects.requireNonNull(this.getCommand("seffect")).setExecutor(new Effects(this));

        getServer().getPluginManager().registerEvents(new OnMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOnListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOffListener(this), this);
        getServer().getPluginManager().registerEvents(new OnCommandPreprocessListener(this), this);
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return config;
    }

    public ConfigManager reloadConfigManager() {
        this.reloadConfig();
        config = new ConfigManager(this, this.getConfig());
        return config;
    }
}
