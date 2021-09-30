package me.ohowe12.spectatormode.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.testutils.TestUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OnMoveListenerTest {

    ServerMock serverMock;
    SpectatorMode plugin;

    // Default player location is 0, 5, 0
    PlayerMock playerMock;

    @BeforeEach
    void setUp() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(SpectatorMode.class);

        serverMock.setPlayers(0);
        playerMock = serverMock.addPlayer();
        playerMock.setGameMode(GameMode.SURVIVAL);

        plugin.reloadConfig();

        plugin.getSpectatorManager().togglePlayer(playerMock);
        playerMock.nextMessage();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -5, -6})
    void checkY_EnabledAndBelowAllowed_Canceled(int shift) {
        TestUtils.setConfigFileOfPlugin(plugin, "badyenabled.yml");

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(0, shift, 0));

        assertMoveEventCanceled(event);
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, 1})
    void checkY_EnabledAndAboveAllowed_Allowed(int shift) {
        TestUtils.setConfigFileOfPlugin(plugin, "badyenabled.yml");

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(0, -shift, 0));

        assertMoveEventNotCanceled(event);
    }

    @Test
    void checkY_DisabledAndBelowAllowed_Allowed() {
        TestUtils.setConfigFileOfPlugin(plugin, "badydisabled.yml");

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(0, -3, 0));

        assertMoveEventNotCanceled(event);
    }

    @ParameterizedTest
    @ValueSource(doubles = {5.1, 6})
    void checkDistance_EnabledAndToFar_Canceled(double shift) {
        TestUtils.setConfigFileOfPlugin(plugin, "baddistanceenabled.yml");

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(shift, 0, 0));

        assertMoveEventCanceled(event);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5})
    void checkDistance_EnabledAndClose_Allowed(int shift) {
        TestUtils.setConfigFileOfPlugin(plugin, "baddistanceenabled.yml");

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(shift, 0, 0));

        assertMoveEventNotCanceled(event);
    }

    @Test
    void checkDistance_DisabledAndToFar_Allowed() {
        TestUtils.setConfigFileOfPlugin(plugin, "baddistancedisabled.yml");

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(6, 0, 0));

        assertMoveEventNotCanceled(event);
    }

    @Test
    void checkCancelable_HasBypass_Allowed() {
        TestUtils.setConfigFileOfPlugin(plugin, "baddistanceenabled.yml");

        playerMock.addAttachment(plugin, "smpspectator.bypass", true);

        PlayerMoveEvent event =
                playerMock.simulatePlayerMove(playerMock.getLocation().add(6, 0, 0));

        assertMoveEventNotCanceled(event);
    }

    //    World border not yet implemented in MockBukkit. Awaiting pull request merge
    //    @Test
    //    void checkWorldBorder_EnabledAndOutside_Cancelled() {
    //        // Enabled by default
    //
    //        playerMock.getWorld().getWorldBorder().setSize(10);
    //
    //        PlayerMoveEvent event = playerMock.simulatePlayerMove(playerMock.getLocation().add(11,
    // 0, 0));
    //
    //        assertMoveEventCanceled(event);
    //    }
    //
    //    @Test
    //    void checkWorldBorder_EnabledAndInside_Allowed() {
    //        // Enabled by default
    //
    //        playerMock.getWorld().getWorldBorder().setSize(10);
    //
    //        PlayerMoveEvent event = playerMock.simulatePlayerMove(playerMock.getLocation().add(9,
    // 0, 0));
    //
    //        assertMoveEventCanceled(event);
    //    }
    //
    //    @Test
    //    void checkWorldBorder_DisabledAndOutside_Allowed() {
    //        TestUtils.setConfigFileOfPlugin(plugin, "worldborderdisabled.yml");
    //
    //        playerMock.getWorld().getWorldBorder().setSize(10);
    //
    //        PlayerMoveEvent event = playerMock.simulatePlayerMove(playerMock.getLocation().add(9,
    // 0, 0));
    //
    //        assertMoveEventCanceled(event);
    //    }

    private void assertMoveEventCanceled(PlayerMoveEvent event) {
        assertTrue(event.isCancelled());
        assertEquals(event.getFrom(), event.getTo());
    }

    private void assertMoveEventNotCanceled(PlayerMoveEvent event) {
        assertFalse(event.isCancelled());
    }

    @Test
    void checkTeleport_TeleportDisallowed_NoTeleport() {
        TestUtils.setConfigFileOfPlugin(plugin, "badteleportenabled.yml");

        boolean teleportResult = playerMock.teleport(
                new Location(playerMock.getWorld(), 10, 10, 10),
                PlayerTeleportEvent.TeleportCause.SPECTATE);

        TestUtils.assertEqualsColored("&cYou are not allowed to teleport in spectator mode", playerMock.nextMessage());
        assertFalse(teleportResult);
        playerMock.assertNotTeleported();
        assertEquals(new Location(playerMock.getWorld(), 0, 5, 0),
                playerMock.getLocation());
    }

    @Test
    void checkTeleport_TeleportDisallowedButDifferentCause_Teleport() {
        TestUtils.setConfigFileOfPlugin(plugin, "badteleportenabled.yml");

        Location teleportLocation = new Location(playerMock.getWorld(), 10, 10, 10);

        boolean teleportResult = playerMock.teleport(
                teleportLocation,
                PlayerTeleportEvent.TeleportCause.PLUGIN);

        playerMock.assertNoMoreSaid();
        assertTrue(teleportResult);
        playerMock.assertTeleported(teleportLocation, 0);
        assertEquals(new Location(playerMock.getWorld(), 10, 10, 10),
                playerMock.getLocation());
    }

    @Test
    void checkTeleport_TeleportDisallowedButHasBypass_Teleport() {
        TestUtils.setConfigFileOfPlugin(plugin, "badteleportenabled.yml");

        playerMock.addAttachment(plugin, "smpspectator.bypass", true);

        Location teleportLocation = new Location(playerMock.getWorld(), 10, 10, 10);

        boolean teleportResult = playerMock.teleport(
                teleportLocation,
                PlayerTeleportEvent.TeleportCause.SPECTATE);

        playerMock.assertNoMoreSaid();
        assertTrue(teleportResult);
        playerMock.assertTeleported(teleportLocation, 0);
        assertEquals(new Location(playerMock.getWorld(), 10, 10, 10),
                playerMock.getLocation());
    }
}
