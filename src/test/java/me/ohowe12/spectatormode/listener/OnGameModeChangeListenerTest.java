package me.ohowe12.spectatormode.listener;

import static me.ohowe12.spectatormode.testutils.TestUtils.assertDoesNotHaveAnyEffects;
import static me.ohowe12.spectatormode.testutils.TestUtils.assertHasSpectatorEffects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import me.ohowe12.spectatormode.SpectatorManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.testutils.TestUtils;

import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnGameModeChangeListenerTest {
    ServerMock serverMock;
    SpectatorMode plugin;

    PlayerMock playerMock;
    SpectatorManager spectatorManager;

    @BeforeEach
    void setUp() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(SpectatorMode.class);

        serverMock.setPlayers(0);
        playerMock = serverMock.addPlayer();
        playerMock.setGameMode(GameMode.SURVIVAL);

        plugin.reloadConfig();

        spectatorManager = plugin.getSpectatorManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void gameModeChange_InState_RemovedFromStateAndEffectsRemoved() {
        spectatorManager.togglePlayer(playerMock);

        new OnGameModeChangeListener(plugin)
                .onGameModeChange(new PlayerGameModeChangeEvent(playerMock, GameMode.SURVIVAL));
        playerMock.setGameMode(GameMode.SURVIVAL);

        assertFalse(spectatorManager.getStateHolder().hasPlayer(playerMock));
        assertDoesNotHaveAnyEffects(playerMock);
    }

    @Test
    void gameModeChange_NotInState_EffectsKept() {
        playerMock.setGameMode(GameMode.SPECTATOR);
        playerMock.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 1));
        playerMock.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000, 1));

        new OnGameModeChangeListener(plugin)
                .onGameModeChange(new PlayerGameModeChangeEvent(playerMock, GameMode.SURVIVAL));
        playerMock.setGameMode(GameMode.SURVIVAL);

        assertHasSpectatorEffects(playerMock);
    }

    @Test
    void gameModeChange_InStateNotEnabled_EffectsKeptAndStillInState() {
        TestUtils.setConfigFileOfPlugin(plugin, "gamemodechangedisabled.yml");

        spectatorManager.togglePlayer(playerMock);

        new OnGameModeChangeListener(plugin)
                .onGameModeChange(new PlayerGameModeChangeEvent(playerMock, GameMode.SURVIVAL));
        playerMock.setGameMode(GameMode.SURVIVAL);

        assertTrue(spectatorManager.getStateHolder().hasPlayer(playerMock));
        assertHasSpectatorEffects(playerMock);
    }
}
