package me.ohowe12.spectatormode;


import me.ohowe12.spectatormode.commands.Spectator;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpectatorTest {
    private final SpectatorMode plugin = mock(SpectatorMode.class);
    private final ConfigManager configManager = mock(ConfigManager.class);

    @Before
    public void beforeClass() throws Exception {
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getUnitTest()).thenReturn(true);

        when(configManager.getBoolean("enabled")).thenReturn(true);
        when(configManager.getColorizedString("permission-message")).thenReturn("§cYou do not have permission to do that!");
    }

    @Test
    public void testNoPermissionEnable() {
        final Player player = mock(Player.class);
        when(player.hasPermission("spectator-enable")).thenReturn(false);

        Command cmd = mock(Command.class);

        Spectator s = new Spectator(plugin);

        s.onCommand(player, cmd, "s", new String[]{"enable"});


        verify(player).sendMessage("§cYou do not have permission to do that!");
        assertTrue(s.issEnabled());
    }

    @Test
    public void testNoPermissionDisable() {
        final Player player = mock(Player.class);
        when(player.hasPermission("spectator-enable")).thenReturn(false);

        Command cmd = mock(Command.class);

        Spectator s = new Spectator(plugin);

        s.onCommand(player, cmd, "s", new String[]{"disable"});


        verify(player).sendMessage("§cYou do not have permission to do that!");
        assertTrue(s.issEnabled());
    }

    @Test
    public void testNoPermissionUse() {
        final Player player = mock(Player.class);
        when(player.hasPermission("spectator-use")).thenReturn(false);

        Command cmd = mock(Command.class);

        Spectator s = new Spectator(plugin);

        s.onCommand(player, cmd, "s", new String[0]);

        verify(player).sendMessage("§cYou do not have permission to do that!");
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
        verify(player, never()).setGameMode(GameMode.SPECTATOR);
    }
}
