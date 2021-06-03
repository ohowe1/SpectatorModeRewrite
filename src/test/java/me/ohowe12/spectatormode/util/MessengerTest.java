package me.ohowe12.spectatormode.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.testutils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MessengerTest {

    private ServerMock server;
    private SpectatorMode plugin;
    private PlayerMock playerMock;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(SpectatorMode.class);

        playerMock = server.addPlayer();

        TestUtils.setConfigFileOfPlugin(plugin, "messangertests.yml");

        Messenger.init(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void send_SenderKey_Valid() {
        Messenger.send(playerMock, "regular-message");

        playerMock.assertSaid("Hello world!");
        playerMock.assertNoMoreSaid();
    }

    @Test
    void send_WithTarget_Valid() {
        PlayerMock otherPlayer = server.addPlayer("Player2");
        Messenger.send(playerMock, otherPlayer, "message-with-target");

        playerMock.assertSaid("Message with Player2");
        playerMock.assertNoMoreSaid();

        otherPlayer.assertNoMoreSaid();
    }

    @Test
    void send_WithExtra_Valid() {
        Messenger.send(playerMock, "message-with-extra", " like this");

        playerMock.assertSaid("Something should be appended to this like this");
        playerMock.assertNoMoreSaid();
    }

    @Test
    void send_WithAll_Valid() {
        PlayerMock otherPlayer = server.addPlayer("Player2");
        Messenger.send(playerMock, otherPlayer, "message-with-all-features", "mocked player");

        playerMock.assertSaid("Player2 is a mocked player");
        playerMock.assertNoMoreSaid();

        otherPlayer.assertNoMoreSaid();
    }

    @Test
    void send_ActionBar_Valid() {
        Messenger.send(playerMock, "action-bar-test");

        // This test doesn't really test much but /actionbar/ being removed cause mockbukkit just saids a "vanilla"
        // message on chat components

        playerMock.assertSaid("This should be on your action bar");
        playerMock.assertNoMoreSaid();
    }

    @Test
    void send_NotValidMessage_FailsNullPointer() {
        assertThrows(NullPointerException.class, () -> {
            Messenger.send(playerMock, "not-a-config-value");
        });

        playerMock.assertNoMoreSaid();
    }
}