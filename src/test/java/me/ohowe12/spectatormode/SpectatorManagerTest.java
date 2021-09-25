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

import static me.ohowe12.spectatormode.testutils.TestUtils.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import me.ohowe12.spectatormode.testutils.TestUtils;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    void togglePlayer_Valid_SwitchesGameMode() {
        playerMock.assertGameMode(GameMode.SURVIVAL);

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertGameMode(GameMode.SPECTATOR);

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertGameMode(GameMode.SURVIVAL);

    }

    @Test
    void togglePlayer_Valid_HasEffects() {
        spectatorManager.togglePlayer(playerMock);

        assertHasSpectatorEffects(playerMock);

        spectatorManager.togglePlayer(playerMock);

        assertDoesNotHaveAnyEffects(playerMock);
    }

    @Test
    void togglePlayer_EffectsDisabled_NoEffects() {
        TestUtils.setConfigFileOfPlugin(plugin, "disabledeffects.yml");

        spectatorManager.togglePlayer(playerMock);

        assertDoesNotHaveAnyEffects(playerMock);

        spectatorManager.togglePlayer(playerMock);

        assertDoesNotHaveAnyEffects(playerMock);
    }

    @Test
    void togglePlayerEffects_Valid_GivenAndRemovedEffects() {
        spectatorManager.togglePlayer(playerMock);

        spectatorManager.togglePlayerEffects(playerMock);
        assertDoesNotHaveAnyEffects(playerMock);

        spectatorManager.togglePlayerEffects(playerMock);
        assertHasSpectatorEffects(playerMock);
    }

    @Test
    void togglePlayer_Valid_MessagesSent() {
        spectatorManager.togglePlayer(playerMock);
        assertEqualsColored("&9Setting gamemode to &b&lSPECTATOR MODE", playerMock.nextMessage());

        spectatorManager.togglePlayer(playerMock);
        assertEqualsColored("&9Setting gamemode to &b&lSURVIVAL MODE", playerMock.nextMessage());
    }

    @Test
    void togglePlayer_MovesThenTogglesBack_TeleportedBack() {
        Location originalLocation = playerMock.getLocation();

        spectatorManager.togglePlayer(playerMock);
        playerMock.teleport(playerMock.getLocation().add(10, 10, 10));

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertLocation(originalLocation, 0);
    }

    @Test
    void togglePlayer_WithFallDistance_NoGameModeChange() {
        playerMock.setFallDistance(10);

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored(
                "&cHey you &lcan not &r&cdo that while falling!", playerMock.nextMessage());
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void togglePlayer_WithFallDistancePreventionDisabled_GameModeChange() {
        TestUtils.setConfigFileOfPlugin(plugin, "falldistancedisabled.yml");
        playerMock.setFallDistance(10);

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    void togglePlayer_BadHealth_NoGameModeChange() {
        TestUtils.setConfigFileOfPlugin(plugin, "badhealth.yml");

        playerMock.setHealth(4);

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored(
                "&cYou are below the minimum required health to preform this command!",
                playerMock.nextMessage());
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void togglePlayer_BadHealthEnabledButAbove_GameModeChange() {
        TestUtils.setConfigFileOfPlugin(plugin, "badhealth.yml");

        playerMock.setHealth(5);

        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void togglePlayer_BadWorld_NoGameModeChange() {
        TestUtils.setConfigFileOfPlugin(plugin, "badworld.yml");

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored(
                "&cHey you&l can not &r&cdo that in that world!", playerMock.nextMessage());
        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void togglePlayer_BadWorldEnabledAndInValidWorld_GameModeChange() {
        TestUtils.setConfigFileOfPlugin(plugin, "badworld.yml");
        WorldMock newWorld = serverMock.addSimpleWorld("umaybeinthisworld");
        playerMock.teleport(newWorld.getSpawnLocation());

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertGameMode(GameMode.SPECTATOR);
    }

    @ParameterizedTest
    @ValueSource(doubles = {2.1, 3})
    void togglePlayer_YEnabledAndGoodY_GameModeChange(double yLevel) {
        TestUtils.setConfigFileOfPlugin(plugin, "badyenabled.yml");

        playerMock.teleport(new Location(playerMock.getWorld(), 0, yLevel, 0));

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertGameMode(GameMode.SPECTATOR);
    }

    @ParameterizedTest
    @ValueSource(doubles = {2, 1})
    void togglePlayer_YEnabledAndBadY_NoGameModeChange(double yLevel) {
        TestUtils.setConfigFileOfPlugin(plugin, "badyenabled.yml");

        playerMock.teleport(new Location(playerMock.getWorld(), 0, yLevel, 0));

        spectatorManager.togglePlayer(playerMock);

        playerMock.assertGameMode(GameMode.SURVIVAL);
        assertEqualsColored("&cYou are below the enforced y-level limit", playerMock.nextMessage());
    }

    @Test
    void togglePlayer_TimeDelayOnNoMove_GameModeChangeAfterTime() {
        TestUtils.setConfigFileOfPlugin(plugin, "timedelay.yml");

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored("&bStand still to be put into spectator mode!", playerMock.nextMessage());

        serverMock.getScheduler().performTicks(9);
        playerMock.assertGameMode(GameMode.SURVIVAL);
        serverMock.getScheduler().performOneTick();

        playerMock.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    void togglePlayer_TimeDelayOnWithMove_NoGameModeChangeAfterTime() {
        TestUtils.setConfigFileOfPlugin(plugin, "timedelay.yml");

        spectatorManager.togglePlayer(playerMock);

        assertEqualsColored("&bStand still to be put into spectator mode!", playerMock.nextMessage());

        serverMock.getScheduler().performTicks(4);
        playerMock.simulatePlayerMove(new Location(playerMock.getWorld(), 0, 1, 0));
        assertEqualsColored("&cYou moved! Spectator mode has been cancelled", playerMock.nextMessage());

        playerMock.assertGameMode(GameMode.SURVIVAL);
        serverMock.getScheduler().performTicks(6);

        playerMock.assertGameMode(GameMode.SURVIVAL);
    }
}
