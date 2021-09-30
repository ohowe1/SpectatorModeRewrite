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

package me.ohowe12.spectatormode.util;

import me.ohowe12.spectatormode.SpectatorMode;

public class Logger {

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    private final SpectatorMode plugin;
    private final ConfigManager config;
    private final String PREFIX;
    private final String FOOTER;

    public Logger(SpectatorMode plugin) {
        this.plugin = plugin;
        config = plugin.getConfigManager();
        PREFIX = CYAN;
        FOOTER = RESET;
    }

    public void debugLog(String message) {
        if (config.getBoolean("debug")) {
            plugin.getLogger().info(GREEN + "[Debug] " + PREFIX + message + FOOTER);
        }
    }

    public void log(String message) {
        plugin.getLogger().info(PREFIX + message + FOOTER);
    }

    public void logIfNotInTests(String message) {
        if (!plugin.isUnitTest()) {
            log(message);
        }
    }
}
