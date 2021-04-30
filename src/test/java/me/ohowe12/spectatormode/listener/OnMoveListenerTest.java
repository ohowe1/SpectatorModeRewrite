/*
 * MIT License
 *
 * Copyright (c) 2021 carelesshippo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN
 */

package me.ohowe12.spectatormode.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.UUID;
import me.ohowe12.spectatormode.ConfigManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.State;
import me.ohowe12.spectatormode.commands.Spectator;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

public class OnMoveListenerTest {

    private static final SpectatorMode plugin = mock(SpectatorMode.class);
    private static final ConfigManager configManager = mock(ConfigManager.class);
    private static final Spectator spectator = mock(Spectator.class);
    private static final Player player = mock(Player.class);
    private static final Block air = mock(Block.class);
    private static final Block stone = mock(Block.class);
    private static final World world = mock(World.class);
    private static final State state = mock(State.class);

    @BeforeAll
    public static void setUp() {
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getSpectatorCommand()).thenReturn(spectator);

        resetAll();
        when(configManager.getInt("y-level")).thenReturn(0);
        when(configManager.getInt("distance")).thenReturn(10);

        when(spectator.inState("28295962-bed5-494a-8457-05d44feb2652")).thenReturn(true);
        when(spectator.getState("28295962-bed5-494a-8457-05d44feb2652")).thenReturn(state);

        when(player.getUniqueId())
            .thenReturn(UUID.fromString("28295962-bed5-494a-8457-05d44feb2652"));
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
        Location eyeLevel = new Location(world, Objects.requireNonNull(to).getX(), to.getY() + 1,
            to.getZ());

        when(world.getBlockAt(eyeLevel)).thenReturn(stone);
        PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

        new OnMoveListener(plugin).onMove(e);

        assertFalse(e.isCancelled());
        assertEquals(e.getTo(), to);
    }

    // @Test
    // public void testPositiveEnforceBlocks() {
    //     resetAll();

    //     when(configManager.getBoolean("disallow-all-blocks")).thenReturn(true);

    //     Location from = new Location(world, 0, 2, 0);
    //     Location to = new Location(world, 0, 1, 0);
    //     Location eyeLevel = new Location(world, Objects.requireNonNull(to).getX(), to.getY() + 1,
    //         to.getZ());

    //     when(world.getBlockAt(eyeLevel)).thenReturn(stone);
    //     PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

    //     new OnMoveListener(plugin).onMove(e);

    //     assertTrue(e.isCancelled());
    //     assertEquals(e.getTo(), from);
    // }

    // @Test
    // public void testNegativeEnforceBlocks() {
    //     resetAll();

    //     when(configManager.getBoolean("disallow-all-blocks")).thenReturn(true);

    //     Location from = new Location(world, 0, 2, 0);
    //     Location to = new Location(world, 0, 1, 0);
    //     Location eyeLevel = new Location(world, Objects.requireNonNull(to).getX(), to.getY() + 1,
    //         to.getZ());

    //     when(world.getBlockAt(eyeLevel)).thenReturn(air);
    //     PlayerMoveEvent e = new PlayerMoveEvent(player, from, to);

    //     new OnMoveListener(plugin).onMove(e);

    //     assertFalse(e.isCancelled());
    //     assertEquals(e.getTo(), to);
    // }

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


    private static void resetAll() {
        when(configManager.getBoolean("enforce-y")).thenReturn(false);
        when(configManager.getBoolean("enforce-distance")).thenReturn(false);
        when(configManager.getBoolean("disallow-non-transparent-blocks")).thenReturn(false);
        when(configManager.getBoolean("disallow-all-blocks")).thenReturn(false);
        when(configManager.getBoolean("enforce-world-border")).thenReturn(false);
    }
}
