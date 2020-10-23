/*
 * SpectatorModeRewrite
 *
 * Copyright (c) 2020. Oliver Howe
 *
 * MIT License
 */

package me.ohowe12.spectatormode;

import java.util.Objects;

import me.ohowe12.spectatormode.commands.Effects;
import me.ohowe12.spectatormode.commands.Spectator;
import me.ohowe12.spectatormode.commands.Speed;
import me.ohowe12.spectatormode.listener.OnCommandPreprocessListener;
import me.ohowe12.spectatormode.listener.OnLogOnListener;
import me.ohowe12.spectatormode.listener.OnLogOffListener;
import me.ohowe12.spectatormode.listener.OnMoveListener;
import me.ohowe12.spectatormode.tabCompleter.SpectatorTab;
import me.ohowe12.spectatormode.tabCompleter.SpeedTab;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SpectatorMode extends JavaPlugin {

    private static SpectatorMode instance;
    private Spectator spectatorCommand;
    private ConfigManager config;

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
        Messenger.init(config);
        DataSaver.init(this.getDataFolder());
        registerCommands();
        if (!this.getUnitTest()) {
            int pluginId = 7132;
            new Metrics(this, pluginId);
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
    }
    
    // This will be mocked to be true in tests
    public boolean getUnitTest() {
        return false;
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
