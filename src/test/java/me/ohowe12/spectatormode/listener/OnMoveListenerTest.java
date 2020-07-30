package me.ohowe12.spectatormode.listener;

import me.ohowe12.spectatormode.ConfigManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.State;
import me.ohowe12.spectatormode.commands.Spectator;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OnMoveListenerTest {
    private final SpectatorMode plugin = mock(SpectatorMode.class);
    private final ConfigManager configManager = mock(ConfigManager.class);
    private final Spectator spectator = mock(Spectator.class);
    private final Player player = mock(Player.class);
    private final Block air = mock(Block.class);
    private final Block stone = mock(Block.class);
    private final World world = mock(World.class);
    private final State state = mock(State.class);

    @Before
    public void setUp() {
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getSpectatorCommand()).thenReturn(spectator);
        when(plugin.getUnitTest()).thenReturn(true);

        resetAll();
        when(configManager.getInt("y-level")).thenReturn(0);
        when(configManager.getInt("distance")).thenReturn(10);

        when(spectator.inState("28295962-bed5-494a-8457-05d44feb2652")).thenReturn(true);
        when(spectator.getState("28295962-bed5-494a-8457-05d44feb2652")).thenReturn(state);

        when(player.getUniqueId()).thenReturn(UUID.fromString("28295962-bed5-494a-8457-05d44feb2652"));
        when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        when(player.getWorld()).thenReturn(world);

        when(air.getType()).thenReturn(Material.AIR);
        when(stone.getType()).thenReturn(Material.STONE);
    }

    @Test
    public void testPositiveYLevel() {
        resetAll();

        when(configManager.getBoolean("enforce-y")).thenReturn(true);

        Location from = new Location(world, 0, 1, 0);
        Location to = new Location(world, 0, -1, 0);

        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertTrue(e.isCancelled());
        assertEquals(e.getTo(), from);
    }

    @Test
    public void testNegativeYLevel() {
        resetAll();

        when(configManager.getBoolean("enforce-y")).thenReturn(true);

        Location from = new Location(world, 0, 2, 0);
        Location to = new Location(world, 0, 1, 0);
        Location eyeLevel = new Location(world, Objects.requireNonNull(to).getX(), to.getY() + 1, to.getZ());

        when(world.getBlockAt(eyeLevel)).thenReturn(stone);
        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertFalse(e.isCancelled());
        assertEquals(e.getTo(), to);
    }

    @Test
    public void testPositiveEnforceBlocks() {
        resetAll();

        when(configManager.getBoolean("disallow-all-blocks")).thenReturn(true);

        Location from = new Location(world, 0, 2, 0);
        Location to = new Location(world, 0, 1, 0);
        Location eyeLevel = new Location(world, Objects.requireNonNull(to).getX(), to.getY() + 1, to.getZ());

        when(world.getBlockAt(eyeLevel)).thenReturn(stone);
        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertTrue(e.isCancelled());
        assertEquals(e.getTo(), from);
    }

    @Test
    public void testNegativeEnforceBlocks() {
        resetAll();

        when(configManager.getBoolean("disallow-all-blocks")).thenReturn(true);

        Location from = new Location(world, 0, 2, 0);
        Location to = new Location(world, 0, 1, 0);
        Location eyeLevel = new Location(world, Objects.requireNonNull(to).getX(), to.getY() + 1, to.getZ());

        when(world.getBlockAt(eyeLevel)).thenReturn(air);
        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertFalse(e.isCancelled());
        assertEquals(e.getTo(), to);
    }

    @Test
    public void testPositiveDistance() {
        resetAll();
        when(configManager.getBoolean("enforce-distance")).thenReturn(true);

        Location original = new Location(world, 0, 0, 0);
        when(state.getPlayerLocation()).thenReturn(original);

        Location from = new Location(world, 10, 0, 0);
        Location to = new Location(world, 11, 0, 0);

        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertTrue(e.isCancelled());
        assertEquals(e.getTo(), from);
    }

    @Test
    public void testNegativeDistance() {
        resetAll();
        when(configManager.getBoolean("enforce-distance")).thenReturn(true);

        Location original = new Location(world, 0, 0, 0);
        when(state.getPlayerLocation()).thenReturn(original);

        Location from = new Location(world, 9, 0, 0);
        Location to = new Location(world, 10, 0, 0);

        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertFalse(e.isCancelled());
        assertEquals(e.getTo(), to);
    }

    @Test
    public void testSurvivalMode() {
        Player tempPlayer = mock(Player.class);
        when(tempPlayer.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(tempPlayer.getWorld()).thenReturn(world);

        Location from = new Location(world, 0, 0, 0);
        Location to = new Location(world, 0, -10, 0);

        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);
        new OnMoveListener(plugin).onMove(e);

        assertFalse(e.isCancelled());
        assertEquals(e.getTo(), to);
    }

    private void setAll() {
        when(configManager.getBoolean("enforce-y")).thenReturn(false);
        when(configManager.getBoolean("enforce-distance")).thenReturn(false);
        when(configManager.getBoolean("disallow-non-transparent-blocks")).thenReturn(false);
        when(configManager.getBoolean("disallow-all-blocks")).thenReturn(false);
        when(configManager.getBoolean("enforce-world-border")).thenReturn(false);
    }

    private void resetAll() {
        setAll();
    }
}