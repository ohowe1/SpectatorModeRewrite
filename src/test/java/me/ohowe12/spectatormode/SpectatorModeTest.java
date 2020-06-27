package me.ohowe12.spectatormode;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SpectatorModeTest {
    private static ServerMock server;
    private static SpectatorMode plugin;

    @Before
    public void load() {
        server = MockBukkit.mock();
        MockBukkit.load(SpectatorMode.class);
        plugin = SpectatorMode.getInstance();
    }

    @After
    public void unload() {
        MockBukkit.unmock();
    }

    @Test
    public void testDisableSpectator() {
        Player player = server.addPlayer();
        player.addAttachment(plugin, "spectator-enable", true);

        server.execute("s", player, "enable");

        server.execute("s", player).assertResponse("§dSpectator mode has been §lenabled");
    }

    @Test
    public void testEnableSpectator() {
        Player player = server.addPlayer();
        player.addAttachment(plugin, "spectator-enable", true);

        server.execute("s", player, "disable");

        server.execute("s", player).assertResponse("§dSpectator mode has been §ldisabled");
    }

    @Test
    public void testIncorrectEnablePermissions() {
        Player player = server.addPlayer();

        server.execute("s", player, "enable").assertResponse("§cYou do not have permission to do that!");
    }
    @Test
    public void testNoPermissions() {
        Player player = server.addPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        player.addAttachment(plugin, "spectator-use", false);

        server.execute("s", player).assertResponse("§cYou do not have permission to do that!");
    }

}
