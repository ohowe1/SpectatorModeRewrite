package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.state.StateHolder;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class OnGameModeChangeListener implements Listener {
    private final SpectatorMode plugin;
    private final StateHolder stateHolder;

    public OnGameModeChangeListener(SpectatorMode plugin) {
        this.plugin = plugin;
        this.stateHolder = plugin.getSpectatorManager().getStateHolder();
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent gameModeChangeEvent) {
        if (plugin.getConfigManager().getBoolean("watch-gamemode")
                && gameModeChangeEvent.getNewGameMode() != GameMode.SPECTATOR
                && stateHolder.hasPlayer(gameModeChangeEvent.getPlayer())) {
            stateHolder.removePlayer(gameModeChangeEvent.getPlayer());
            stateHolder.cancelKicker(gameModeChangeEvent.getPlayer());
            if (gameModeChangeEvent.getPlayer().isOnline()) {
                plugin.getSpectatorManager().removeSpectatorEffects(gameModeChangeEvent.getPlayer());
            } else {
                stateHolder.addToRemoveOnFullLogin(gameModeChangeEvent.getPlayer());
            }
        }
    }
}
