package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.State;
import me.ohowe12.spectatormode.commands.Spectator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class OnLogOnListener implements Listener {
    Map<String, State> state;

    @EventHandler
    public void onLogOn(PlayerJoinEvent e) {
        state = Spectator.getInstance().state;
        boolean teleportBack = SpectatorMode.getInstance().getConfig().getBoolean("teleport-back", false);
        Player player = e.getPlayer();
        if (!(teleportBack)) {
            return;
        }
        try {
            if (state.containsKey(player.getUniqueId().toString())) {
                teleportPlayerBack(player);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void teleportPlayerBack(Player player) {
        Location location = state.get(player.getUniqueId().toString()).getPlayerLocation();
        player.teleport(location);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
}
