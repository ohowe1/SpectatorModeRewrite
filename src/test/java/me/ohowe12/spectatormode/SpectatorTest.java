package me.ohowe12.spectatormode;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import me.ohowe12.spectatormode.commands.Spectator;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.Before;
import org.junit.Test;

public class SpectatorTest {

    private final SpectatorMode plugin = mock(SpectatorMode.class);
    private final ConfigManager configManager = mock(ConfigManager.class);
    private final Command cmd = mock(Command.class);

    @Before
    public void beforeClass() {
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getUnitTest()).thenReturn(true);

        when(configManager.getBoolean("enabled")).thenReturn(true);
        when(configManager.getColorizedString("permission-message"))
            .thenReturn("§cYou do not have permission to do that!");
        when(configManager.getColorizedString("spectator-mode-message"))
            .thenReturn("§9Setting gamemode to §b§lSPECTATOR MODE");
        when(configManager.getColorizedString("survival-mode-message"))
            .thenReturn("§9Setting gamemode to §b§lSURVIVAL MODE");
        when(configManager.getBoolean("enforce-worlds")).thenReturn(false);
        when(configManager.getBoolean("night-vision")).thenReturn(true);
        when(configManager.getBoolean("conduit")).thenReturn(true);
        when(configManager.getList("worlds-allowed")).thenReturn(new ArrayList<>());
        when(configManager.getBoolean("placeholder-mob")).thenReturn(false);
        Messenger.init(plugin);
        PlaceholderEntity.init(plugin);
    }

    @Test
    public void testNoPermissionEnable() {
        final Player player = mock(Player.class);
        when(player.hasPermission("smpspectator.enable")).thenReturn(false);

        final Spectator s = new Spectator(plugin);
        s.onCommand(player, cmd, "s", new String[]{"enable"});

        verify(player).sendMessage("§cYou do not have permission to do that!");
        assertTrue(s.issEnabled());
    }

    @Test
    public void testNoPermissionDisable() {
        final Player player = mock(Player.class);
        when(player.hasPermission("smpspectator.enable")).thenReturn(false);

        final Spectator s = new Spectator(plugin);
        s.onCommand(player, cmd, "s", new String[]{"disable"});

        verify(player).sendMessage("§cYou do not have permission to do that!");
        assertTrue(s.issEnabled());
    }

    @Test
    public void testNoPermissionUse() {
        final Player player = mock(Player.class);
        when(player.hasPermission("smpspectator.use")).thenReturn(false);
        when(player.getUniqueId())
            .thenReturn(UUID.fromString("e7a9396a-634c-4f25-a66f-1e5ce18dfae7"));

        final Spectator s = new Spectator(plugin);
        s.onCommand(player, cmd, "s", new String[0]);

        verify(player).sendMessage("§cYou do not have permission to do that!");
        verify(player, never()).setGameMode(GameMode.SURVIVAL);
        verify(player, never()).setGameMode(GameMode.SPECTATOR);
    }

    @Test
    public void testInSurvival() {
        final Player player = mock(Player.class);
        when(player.hasPermission("smpspectator.use")).thenReturn(true);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.isOnGround()).thenReturn(true);
        when(player.getUniqueId())
            .thenReturn(UUID.fromString("fe93039a-48f2-45ca-acb5-b7988765c090"));
        when(player.getActivePotionEffects()).thenReturn(new ArrayList<>());

        final World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        when(player.getLocation()).thenReturn(new Location(world, 0, 0, 0));

        Chunk chunk = mock(Chunk.class);
        when(chunk.getX()).thenReturn(0);
        when(chunk.getZ()).thenReturn(0);
        when(world.getChunkAt(any(Location.class))).thenReturn(chunk);
        Chunk chunk1 = mock(Chunk.class);
        when(chunk1.getEntities()).thenReturn(new Entity[0]);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk1);

        when(player.getWorld()).thenReturn(world);

        final Spectator s = new Spectator(plugin);
        s.onCommand(player, cmd, "s", new String[0]);

        verify(player).setGameMode(GameMode.SPECTATOR);
        verify(player).sendMessage("§9Setting gamemode to §b§lSPECTATOR MODE");
        verify(player)
            .addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 10));
        verify(player)
            .addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 10000000, 10));
    }
}
