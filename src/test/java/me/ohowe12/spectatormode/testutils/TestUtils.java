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

package me.ohowe12.spectatormode.testutils;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

import me.ohowe12.spectatormode.SpectatorMode;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.io.File;

public class TestUtils {
    public static void assertEqualsColored(String expected, String actual) {
        assertEquals(ChatColor.translateAlternateColorCodes('&', expected), actual);
    }

    public static void assertNotEqualsColored(String expected, String actual) {
        assertNotEquals(ChatColor.translateAlternateColorCodes('&', expected), actual);
    }

    public static void setConfigFileOfPlugin(SpectatorMode plugin, String configName) {
        FileConfiguration configuration =
                YamlConfiguration.loadConfiguration(
                        new File("src/test/resources/configs/" + configName));
        configuration.setDefaults(plugin.getConfig());
        plugin.setConfigManagerConfigFile(configuration);
    }

    public static void assertDoesNotHaveAnyEffects(PlayerMock playerMock) {
        assertEquals(0, playerMock.getActivePotionEffects().size());
    }

    public static void assertHasSpectatorEffects(PlayerMock playerMock) {
        assertTrue(
                playerMock.getActivePotionEffects().stream()
                        .anyMatch(e -> e.getType() == PotionEffectType.NIGHT_VISION));
        assertTrue(
                playerMock.getActivePotionEffects().stream()
                        .anyMatch(e -> e.getType() == PotionEffectType.CONDUIT_POWER));
    }
}
