package me.ohowe12.spectatormode.listener;

import static me.ohowe12.spectatormode.testutils.TestUtils.assertEqualsColored;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import me.ohowe12.spectatormode.SpectatorManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.testutils.TestUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnCommandPreprocessListenerTest {
    ServerMock serverMock;
    SpectatorMode plugin;

    PlayerMock playerMock;
    SpectatorManager spectatorManager;

    PlayerCommandPreprocessEvent event;

    @BeforeEach
    void setUp() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(SpectatorMode.class);

        serverMock.setPlayers(0);
        playerMock = serverMock.addPlayer();
        playerMock.setGameMode(GameMode.SURVIVAL);

        plugin.reloadConfig();

        spectatorManager = plugin.getSpectatorManager();

        TestUtils.setConfigFileOfPlugin(plugin, "disabledcommands.yml");

        spectatorManager.togglePlayer(playerMock);
        playerMock.nextMessage();

        event = new PlayerCommandPreprocessEvent(playerMock, "/testcommand");
    }

    @AfterEach
    void tearDown() {
        Bukkit.getScheduler().cancelTasks(plugin);
        MockBukkit.unmock();
    }

    @Test
    void testDisabledCommand() {
        assert event != null;
        new OnCommandPreprocessListener(plugin).onCommandEvent(event);

        assertEqualsColored(
                "&cYou can not execute that command while in spectator mode",
                playerMock.nextMessage());
        assertTrue(event.isCancelled());
    }

    @Test
    void testDisabledCommandWithOverridePermission() {
        assert playerMock != null;
        playerMock.addAttachment(plugin, "smpspectator.bypass", true);

        new OnCommandPreprocessListener(plugin).onCommandEvent(event);

        playerMock.assertNoMoreSaid();

        assertFalse(event.isCancelled());
    }

    @Test
    void testCommandWithSemiColon() {
        assert playerMock != null;
        PlayerCommandPreprocessEvent otherEvent =
                new PlayerCommandPreprocessEvent(playerMock, "/exampleplugin" + ":testcommand");
        new OnCommandPreprocessListener(plugin).onCommandEvent(otherEvent);

        assertEqualsColored(
                "&cYou can not execute that command while in spectator mode",
                playerMock.nextMessage());
        assertTrue(otherEvent.isCancelled());
    }

    @Test
    void testWhenNotInState() {
        assert playerMock != null;
        // Put into survival
        spectatorManager.togglePlayer(playerMock);
        playerMock.nextMessage();

        new OnCommandPreprocessListener(plugin).onCommandEvent(event);

        playerMock.assertNoMoreSaid();
        assertFalse(event.isCancelled());
    }

    @Test
    void testJustSlash() {
        // here to just make sure we dont get index out of bounds or something like that
        new OnCommandPreprocessListener(plugin)
                .onCommandEvent(new PlayerCommandPreprocessEvent(playerMock, "/"));

        assertTrue(true);
    }
}
