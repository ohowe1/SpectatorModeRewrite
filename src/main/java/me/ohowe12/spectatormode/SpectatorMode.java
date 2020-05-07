package me.ohowe12.spectatormode;

import me.ohowe12.spectatormode.commands.Spectator;
import me.ohowe12.spectatormode.commands.Speed;
import me.ohowe12.spectatormode.listener.OnMoveListener;
import me.ohowe12.spectatormode.tabCompleter.SpectatorTab;
import me.ohowe12.spectatormode.tabCompleter.SpeedTab;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpectatorMode extends JavaPlugin {

    private static SpectatorMode instance;

    public static SpectatorMode getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        new UpdateChecker(this, 77267).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[SMP SPECTATOR MODE] SMP SPECTATOR MODE is all up to date!");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[SMP SPECTATOR MODE] A new version of SMP SPECTATOR MODE is available!");
            }
        });
        registerCmds();
        getServer().getPluginManager().registerEvents(new OnMoveListener(), this);
        this.getConfig().addDefault("enforce-worlds", false);
        int pluginId = 7132;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void registerCmds() {
        Objects.requireNonNull(this.getCommand("s")).setExecutor(new Spectator());
        Objects.requireNonNull(this.getCommand("spectator")).setExecutor(new Spectator());
        Objects.requireNonNull(this.getCommand("s")).setTabCompleter(new SpectatorTab());
        Objects.requireNonNull(this.getCommand("spectator")).setTabCompleter(new SpectatorTab());

        Objects.requireNonNull(this.getCommand("speed")).setExecutor(new Speed());
        Objects.requireNonNull(this.getCommand("speed")).setTabCompleter(new SpeedTab());
        Objects.requireNonNull(this.getCommand("sp")).setExecutor(new Speed());
        Objects.requireNonNull(this.getCommand("sp")).setTabCompleter(new SpeedTab());
    }
}
