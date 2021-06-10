package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.state.StateHolder;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class OnGamemodeChange implements Listener {
    private final SpectatorMode plugin;
    private final StateHolder stateHolder;

    public OnGamemodeChange(SpectatorMode plugin) {
        this.plugin = plugin;
        this.stateHolder = plugin.getSpectatorManager().getStateHolder();
    }

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent gameModeChangeEvent) {
        if (plugin.getConfigManager().getBoolean("watch-gamemode") && gameModeChangeEvent.getNewGameMode() != GameMode.SPECTATOR && stateHolder.hasPlayer(gameModeChangeEvent.getPlayer())) {
            stateHolder.removePlayer(gameModeChangeEvent.getPlayer());
            plugin.getSpectatorManager().removeSpectatorEffects(gameModeChangeEvent.getPlayer());
        }
    }
}
