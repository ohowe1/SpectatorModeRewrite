package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.Messenger;
import me.ohowe12.spectatormode.SpectatorMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class OnCommandPreprocessListener implements Listener {

    private final SpectatorMode plugin;

    public OnCommandPreprocessListener(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommandEvent(final PlayerCommandPreprocessEvent e) {
        final Player player = e.getPlayer();

        if (player.hasPermission("smpspectator.bypass")) {
            return;
        }
        if (!plugin.getSpectatorCommand().inState(player.getUniqueId().toString())) {
            return;
        }
        if (plugin.getConfigManager().getList("bad-commands")
            .contains(e.getMessage().substring(1))) {
            Messenger.sendChat(player,"bad-command-message");
            e.setCancelled(true);
        }
    }
}
