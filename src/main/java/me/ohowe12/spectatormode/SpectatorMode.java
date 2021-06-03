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
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.ohowe12.spectatormode.context.SpectatorContextCalculator;
import me.ohowe12.spectatormode.listener.OnCommandPreprocessListener;
import me.ohowe12.spectatormode.listener.OnLogOnListener;
import me.ohowe12.spectatormode.listener.OnMoveListener;
import me.ohowe12.spectatormode.util.ConfigManager;
import me.ohowe12.spectatormode.util.Logger;
import me.ohowe12.spectatormode.util.Messenger;
import me.ohowe12.spectatormode.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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

    protected SpectatorMode(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
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
    public void onLoad() {
        if (!unitTest) {
            CommandAPI.onLoad(false);
        }
    }

    @Override
    public void onEnable() {
        config = new ConfigManager(this, this.getConfig());
        pluginLogger = new Logger(this);
        spectatorManager = new SpectatorManager(this);
        if (!unitTest) {
            CommandAPI.onEnable(this);
            registerCommands();
            addMetrics();
            if (config.getBoolean("update-checker")) {
                checkUpdate();
            }
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
        UpdateChecker.getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                pluginLogger.log(Logger.ANSI_CYAN + "SMP SPECTATOR MODE is all up to date at version " + this.getDescription().getVersion() + "!");
            } else {
                pluginLogger.log(Logger.ANSI_RED + "A new version of SMP SPECTATOR MODE is available (version "
                        + version + ")! You are on version " + this.getDescription().getVersion() + ".");
            }
        }, this);
    }

    public void registerCommands() {
        CommandAPICommand enableCommand =
                new CommandAPICommand("enable").withPermission("smpspectator.enable").executes((sender, args) -> {
                    spectatorManager.setSpectatorEnabled(true);
                    Messenger.send(sender, "enable-message");
                });

        CommandAPICommand disableCommand =
                new CommandAPICommand("disable").withPermission("smpspectator.enable").executes((sender, args) -> {
                    spectatorManager.setSpectatorEnabled(false);
                    Messenger.send(sender, "disable-message");
                });

        CommandAPICommand reloadCommand =
                new CommandAPICommand("reload").withPermission("smpspectator.reload").executes((sender, args) -> {
                    reloadConfigManager();
                    spectatorManager.getStateHolder().load();
                    Messenger.send(sender, "reload-message");
                });

        CommandAPICommand effectCommand =
                new CommandAPICommand("effect").withPermission("smpspectator.toggle").executesPlayer((player, args) -> {
                    spectatorManager.togglePlayerEffects(player);
                });

        CommandAPICommand speedCommand =
                new CommandAPICommand("speed").withPermission("smpspectator.speed").withArguments(new IntegerArgument("speed", 1, config.getInt("max-speed"))).executesPlayer((player, args) -> {
                    player.setFlySpeed(Math.min(1f, (int) args[0] * 0.1f));
                    Messenger.send(player, "speed-message", String.valueOf(args[0]));
                });

        CommandAPICommand forceCommand =
                new CommandAPICommand("force").withPermission("smpspectator.force").withArguments(new PlayerArgument(
                        "target")).executes((sender, args) -> {
                    spectatorManager.togglePlayer((Player) args[0], true);
                });

        CommandAPICommand mainCommand = new CommandAPICommand("s").withAliases("smps").withPermission("smpspectator" +
                ".use").executesPlayer((player,
                                                                                                                                          args) -> {
            spectatorManager.togglePlayer(player);
        }).withSubcommand(enableCommand).withSubcommand(disableCommand).withSubcommand(reloadCommand).withSubcommand(effectCommand).withSubcommand(forceCommand);

        if (config.getBoolean("speed")) {
            mainCommand.withSubcommand(speedCommand);
        }
        if (config.getBoolean("seffect")) {
            mainCommand.withSubcommand(effectCommand);
        }

        mainCommand.register();
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new OnMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new OnLogOnListener(this), this);
        getServer().getPluginManager().registerEvents(new OnCommandPreprocessListener(this), this);
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
