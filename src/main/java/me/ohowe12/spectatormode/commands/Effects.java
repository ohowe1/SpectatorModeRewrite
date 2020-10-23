package me.ohowe12.spectatormode.commands;

import me.ohowe12.spectatormode.ConfigManager;
import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Effects implements CommandExecutor {

    private final PotionEffect NIGHTVISON = new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10);
    private final PotionEffect CONDUIT = new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10);
    private final SpectatorMode plugin;

    public Effects(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        ConfigManager manager = plugin.getConfigManager();
        if (label.equalsIgnoreCase("seffect")) {
            if (!(sender instanceof Player)) {
                Messenger.send(sender,"console-message");
                return true;
            }
            if (!manager.getBoolean("seffect")) {
                Messenger.send(sender,"permission-message");
            }
            Player player = (Player) sender;
            if (!plugin.getSpectatorCommand().inState(player.getUniqueId().toString())) {
                Messenger.send(sender,"no-spectator-message");
                return true;
            }
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)
                    || player.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
                return true;
            }
            if (manager.getBoolean("night-vision")) {
                player.addPotionEffect(NIGHTVISON);
            }
            if (manager.getBoolean("conduit")) {
                player.addPotionEffect(CONDUIT);
            }
            return true;
        }
        return false;
    }

}
