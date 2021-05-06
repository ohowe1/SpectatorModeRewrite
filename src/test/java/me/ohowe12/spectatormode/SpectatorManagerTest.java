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

package me.ohowe12.spectatormode;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.ohowe12.spectatormode.utils.TestUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static me.ohowe12.spectatormode.utils.TestUtils.assertEqualsColored;

class SpectatorManagerTest {

    ServerMock serverMock;
    SpectatorMode plugin;

    PlayerMock playerMock;
    SpectatorManager spectatorManager;

    @BeforeEach
    void setUp() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(SpectatorMode.class);

        serverMock.setPlayers(0);
        playerMock = serverMock.addPlayer();
        playerMock.setGameMode(GameMode.SURVIVAL);

        plugin.reloadConfig();

        spectatorManager = plugin.getSpectatorManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testTogglePlayerGamemodeChange() {
        playerMock.assertGameMode(GameMode.SURVIVAL);

        spectatorManager.togglePlayer(playerMock);
        playerMock.assertGameMode(GameMode.SPECTATOR);

        spectatorManager.togglePlayer(playerMock);
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void testTogglePlayerMessageSent() {
        spectatorManager.togglePlayer(playerMock);
        assertEqualsColored("&9Setting gamemode to &b&lSPECTATOR MODE", playerMock.nextMessage());

        spectatorManager.togglePlayer(playerMock);
        assertEqualsColored("&9Setting gamemode to &b&lSURVIVAL MODE", playerMock.nextMessage());
    }

    @Test
    void testTogglePlayerLocationChange() {
        Location originalLocation = playerMock.getLocation();

        spectatorManager.togglePlayer(playerMock);
        playerMock.teleport(playerMock.getLocation().add(10, 10, 10));

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertLocation(originalLocation, 0);
    }

    @Test
    void testTogglePlayerWithBadFallDamage() {
        playerMock.setFallDistance(10);

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored("&cHey you &lcan not &r&cdo that while falling!", playerMock.nextMessage());
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void testTogglePlayerWithBadHealth() {
        TestUtils.setConfigFileOfPlugin(plugin, "badhealth.yml");

        playerMock.setHealth(4);

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored("&cYou are below the minimum required health to preform this command!", playerMock.nextMessage());
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void testTogglePlayerWithBadWorld() {
        TestUtils.setConfigFileOfPlugin(plugin, "badworld.yml");

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored("&cHey you&l can not &r&cdo that in that world!", playerMock.nextMessage());
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

}