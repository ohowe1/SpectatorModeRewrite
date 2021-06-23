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

import co.aikar.commands.PaperCommandManager;

import me.ohowe12.spectatormode.commands.SpectatorCommand;
import me.ohowe12.spectatormode.context.SpectatorContextCalculator;
import me.ohowe12.spectatormode.listener.OnCommandPreprocessListener;
import me.ohowe12.spectatormode.listener.OnGamemodeChangeListener;
import me.ohowe12.spectatormode.listener.OnLogOnListener;
import me.ohowe12.spectatormode.listener.OnMoveListener;
import me.ohowe12.spectatormode.util.ConfigManager;
import me.ohowe12.spectatormode.util.Logger;
import me.ohowe12.spectatormode.util.Messenger;
import wtf.choco.updatechecker.UpdateChecker;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class SpectatorMode extends JavaPlugin {

    private final boolean unitTest;
    private SpectatorManager spectatorManager;
    private ConfigManager config;
    private Logger pluginLogger;

    public SpectatorMode() {
        super();
        unitTest = false;
    }

    protected SpectatorMode(
            JavaPluginLoader loader,
            PluginDescriptionFile description,
            File dataFolder,
            File file) {
        super(loader, description, dataFolder, file);
        unitTest = true;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public boolean isUnitTest() {
        return unitTest;
    }

    @Override
    public void onEnable() {
        config = new ConfigManager(this, this.getConfig());
        pluginLogger = new Logger(this);
        spectatorManager = new SpectatorManager(this);
        registerCommands();
        if (config.getBoolean("update-checker")) {
            checkUpdate();
        }
        if (!unitTest) {
            addMetrics();
            initializeLuckPermsContext();
        }

        Messenger.init(this);
        registerListeners();
    }

    private void initializeLuckPermsContext() {
        try {
            Class.forName("net.luckperms.api.LuckPerms");
        } catch (ClassNotFoundException ignored) {
            pluginLogger.debugLog("LuckPerms class not found");
            return;
        }
        if (!getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            pluginLogger.debugLog("LuckPerms not enabled");
            return;
        }
        SpectatorContextCalculator.initializeSpectatorContext(this);
    }

    private void addMetrics() {
        Metrics metrics = new Metrics(this, 7132);
        for (Map.Entry<String, String> entry : config.getAllBooleansAndNumbers().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            metrics.addCustomChart(new Metrics.SimplePie(key + "_CHARTID", () -> value));
        }
    }

    private void checkUpdate() {
        UpdateChecker.init(this, 77267).requestUpdateCheck().whenComplete(((updateResult, throwable) -> {
            if (updateResult.getReason() == UpdateChecker.UpdateReason.NEW_UPDATE) {
                pluginLogger.logIfNotInTests(Logger.RED + "A new version of SMP Spectator Mode is available!");
            } else if (updateResult.getReason() == UpdateChecker.UpdateReason.UP_TO_DATE || updateResult.getReason() == UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                pluginLogger.logIfNotInTests(Logger.CYAN + "You are up to date on SMP Spectator Mode!");
            } else {
                pluginLogger.logIfNotInTests(Logger.YELLOW + "An error occurred when checking for SMP Spectator Mode updates. Reason: " + updateResult.getReason());
            }
        }));
    }

    public void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.registerCommand(new SpectatorCommand(this));
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new OnMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOnListener(this), this);
        getServer().getPluginManager().registerEvents(new OnCommandPreprocessListener(this), this);
        getServer().getPluginManager().registerEvents(new OnGamemodeChangeListener(this), this);
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return config;
    }

    public void setConfigManagerConfigFile(FileConfiguration fileConfiguration) {
        config = new ConfigManager(this, fileConfiguration);
    }

    public void reloadConfigManager() {
        this.reloadConfig();
        config = new ConfigManager(this, this.getConfig());
    }

    public Logger getPluginLogger() {
        return pluginLogger;
    }
}
