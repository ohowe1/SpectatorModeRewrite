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

package me.ohowe12.spectatormode.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    public void beforeEach() {
        server.setPlayers(0);
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

    @Test
    @DisplayName("Test /s force")
    public void testForceSucess() {
        PlayerMock target = createSurvivalModePlayer();
        PlayerMock op = server.addPlayer();
        op.setOp(true);

        Location original = target.getLocation();

        server.execute("s", op, target.getName()).assertResponse("§bSuccessfully forced %s into SPECTATOR", target.getName());
        op.assertNoMoreSaid();
        target.assertGameMode(GameMode.SPECTATOR);
        target.teleport(new Location(target.getWorld(), 100, 100, 100));

        server.execute("s", op, target.getName()).assertResponse("§bSuccessfully forced %s into SURVIVAL", target.getName());
        op.assertNoMoreSaid();
        target.assertGameMode(GameMode.SURVIVAL);
        target.assertLocation(original, 1);
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
