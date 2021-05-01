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

import dev.jorel.commandapi.CommandAPI;
import me.ohowe12.spectatormode.commands.SpectatorCommand;
import me.ohowe12.spectatormode.context.SpectatorContextCalculator;
import me.ohowe12.spectatormode.listener.*;
import me.ohowe12.spectatormode.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.Callable;

public class SpectatorMode extends JavaPlugin {

    private SpectatorManager spectatorManager;
    private ConfigManager config;
    private Logger pluginLogger;

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }


    @Override
    public void onLoad() {
        CommandAPI.onLoad(false);
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);
        config = new ConfigManager(this, this.getConfig());
        pluginLogger = new Logger(this);
        spectatorManager = new SpectatorManager(this);
        SpectatorCommand.initPlugin(this);
        registerCommands();
        Messenger.init(this);
        PlaceholderEntity.init(this, spectatorManager.getStateHolder());
        registerCommands();
        addMetrics();

        if (config.getBoolean("update-checker")) {
            checkUpdate();
        }
        initializeLuckPermsContext();
        registerListeners();
    }

    private void initializeLuckPermsContext() {
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
                public String call() {
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
        PlaceholderEntity.shutdown();
    }

    public void registerCommands() {
        CommandAPI.registerCommand(SpectatorCommand.class);
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new OnMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOnListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOffListener(this), this);
        getServer().getPluginManager().registerEvents(new OnCommandPreprocessListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return config;
    }

    public void reloadConfigManager() {
        this.reloadConfig();
        config = new ConfigManager(this, this.getConfig());
    }

    public Logger getPluginLogger() {
        return pluginLogger;
    }

}
