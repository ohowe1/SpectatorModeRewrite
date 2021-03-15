package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.PlaceholderEntity;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class OnLogOffListener implements Listener {

    private final SpectatorMode plugin;

    public OnLogOffListener(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogOut(@NotNull final PlayerQuitEvent e){
        final Player player = e.getPlayer();
        try {
            if (plugin.getSpectatorCommand().inState(player.getUniqueId().toString())) {
                PlaceholderEntity.remove(player);
            }
        } catch (final NullPointerException ignored) {
            // Filler
        }
    }
}
