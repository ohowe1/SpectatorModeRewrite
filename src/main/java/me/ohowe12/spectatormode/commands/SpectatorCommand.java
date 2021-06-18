package me.ohowe12.spectatormode.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;

import me.ohowe12.spectatormode.SpectatorManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.Messenger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("s|smps")
public class SpectatorCommand extends BaseCommand {
    private final SpectatorManager spectatorManager;
    private final SpectatorMode plugin;

    public SpectatorCommand(SpectatorMode plugin) {
        this(plugin, plugin.getSpectatorManager());
    }

    public SpectatorCommand(SpectatorMode plugin, SpectatorManager spectatorManager) {
        this.plugin = plugin;
        this.spectatorManager = spectatorManager;
    }

    @Default
    @Description("Toggle spectator mode")
    @CommandPermission("smpspectator.use")
    public void defaultCommand(Player player) {
        spectatorManager.togglePlayer(player);
    }

    @Subcommand("enable")
    @Description("Enables the /s command")
    @CommandPermission("smpspectator.enable")
    public void enableCommand(CommandSender sender) {
        spectatorManager.setSpectatorEnabled(true);

        Messenger.send(sender, "enable-message");
    }

    @Subcommand("disable")
    @Description("Disables the /s command")
    @CommandPermission("smpspectator.enable")
    public void disableCommand(CommandSender sender) {
        spectatorManager.setSpectatorEnabled(false);

        Messenger.send(sender, "disable-message");
    }

    @Subcommand("reload")
    @Description("Reloads the config and data file")
    @CommandPermission("smpspectator.reload")
    public void reloadCommand(CommandSender sender) {
        plugin.reloadConfigManager();
        spectatorManager.getStateHolder().load();
        Messenger.send(sender, "reload-message");
    }

    @Subcommand("effect")
    @Description("Toggles your spectator effects if enabled")
    @CommandPermission("smpspectator.toggle")
    public void toggleEffectsCommand(Player player) {
        spectatorManager.togglePlayerEffects(player);
    }

    @Subcommand("force")
    @Description("Forces a player into spectator mode")
    @CommandPermission("smpspectator.force")
    @CommandCompletion("@players")
    public void forcePlayerCommand(CommandSender sender, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target != null) {
            spectatorManager.togglePlayer(target, true);
            Messenger.send(sender, "force-success", target);
        } else {
            Messenger.send(sender, "invalid-player-message");
        }
    }
}
