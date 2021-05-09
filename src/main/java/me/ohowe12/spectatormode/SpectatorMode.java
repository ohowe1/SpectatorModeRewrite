/*
 * MIT License
 *
 * Copyright (c) 2021 carelesshippo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN
 */

package me.ohowe12.spectatormode;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import me.ohowe12.spectatormode.commands.Effects;
import me.ohowe12.spectatormode.commands.Spectator;
import me.ohowe12.spectatormode.commands.Speed;
import me.ohowe12.spectatormode.context.SpectatorContextCalculator;
import me.ohowe12.spectatormode.listener.*;
import me.ohowe12.spectatormode.tabcompleter.SpectatorTab;
import me.ohowe12.spectatormode.tabcompleter.SpeedTab;
import me.ohowe12.spectatormode.util.DataSaver;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

public class SpectatorMode extends JavaPlugin {

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

    public Spectator getSpectatorCommand() {
        return spectatorCommand;
    }

    @Override
    public void onEnable() {
        config = new ConfigManager(this, this.getConfig());
        Messenger.init(this);
        DataSaver.init(this.getDataFolder(), this);
        registerCommands();
        if (!unitTest) {
            addMetrics();

            if (config.getBoolean("update-checker")) {
                checkUpdate();
            }
            initalizeLuckPermsContext();
        }
    }

    private void initalizeLuckPermsContext() {
        try {
            Class.forName("net.luckperms.api.LuckPerms");
        } catch (ClassNotFoundException ignored) {
            getLogger().info("LuckPerms class not found");
            return;
        }
        if (!getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            getLogger().info("LuckPerms not enabled");
            return;
        }
        SpectatorContextCalculator.initalizeSpectatorContext(this);
    }

    private void addMetrics() {
        Metrics metrics = new Metrics(this, 7132);
        for (Map.Entry<String, String> entry : config.getAllBooleansAndNumbers().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            metrics.addCustomChart(new Metrics.SimplePie(key + "_CHARTID", new Callable<String>() {
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
                Bukkit.getConsoleSender().sendMessage(
                        ChatColor.AQUA + "[SMP SPECTATOR MODE] SMP SPECTATOR MODE is all up to date at version "
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
