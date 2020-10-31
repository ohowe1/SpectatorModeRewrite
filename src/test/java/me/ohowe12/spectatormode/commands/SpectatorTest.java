package me.ohowe12.spectatormode.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.ohowe12.spectatormode.SpectatorMode;

public class SpectatorTest {
    private static ServerMock server;
    private static SpectatorMode plugin;

    @BeforeAll
    public static void setUp() {
        server = MockBukkit.mock();
        plugin = (SpectatorMode) MockBukkit.load(SpectatorMode.class);
    }

    @Test
    @DisplayName("Player without permission to disable can not")
    public void testNoPermissionDisable() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "smpspectator.enable", false);

        server.execute("s", player, "disable").assertResponse("§cYou do not have permission to do that!");
        assertTrue(plugin.getSpectatorCommand().issEnabled());
    }

    @Test
    @DisplayName("Player without permission to use /s can not")
    public void testNoPermissionUse() {
        PlayerMock player = createSurvivalModePlayer();
        player.addAttachment(plugin, "smpspectator.use", false);

        server.execute("s", player).assertResponse("§cYou do not have permission to do that!");
        player.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    @DisplayName("Player with permission can execute /s")
    public void testPermissionUse() {
        PlayerMock player = createSurvivalModePlayer();
        player.addAttachment(plugin, "smpspectator.use", true);
        Location original = player.getLocation();

        server.execute("s", player).assertResponse("§9Setting gamemode to §b§lSPECTATOR MODE");
        player.assertGameMode(GameMode.SPECTATOR);
        player.teleport(new Location(player.getWorld(), 100, 100, 100));

        server.execute("s", player).assertResponse("§9Setting gamemode to §b§lSURVIVAL MODE");
        player.assertGameMode(GameMode.SURVIVAL);
        player.assertLocation(original, 1);
    }

    private PlayerMock createSurvivalModePlayer() {
        PlayerMock player = server.addPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        return player;
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

}
