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

import me.ohowe12.spectatormode.PlaceholderEntity;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.util.State;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


public class ChunkListener implements Listener {

    private final SpectatorMode plugin;

    public ChunkListener(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chunkLoadEvent(@NotNull final ChunkLoadEvent e) {
        for (State state : plugin.getSpectatorCommand().getAllStates().values()) {
            if (state.getPlayerLocation().getChunk().equals(e.getChunk())) {
                if (state.isNeedsMob()) {
                    PlaceholderEntity.create(state.getPlayer());
                    state.setNeedsMob(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chunkUnLoadEvent(@NotNull final ChunkUnloadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            Optional<State> state = stateWithEntity(entity);
            if (state.isPresent()) {
                PlaceholderEntity.remove(state.get().getPlayerUUID().toString());
                state.ifPresent(value -> value.setNeedsMob(true));
            }
        }
    }

    private Optional<State> stateWithEntity(Entity e) {
        return plugin.getSpectatorCommand().getAllStates().values().stream().filter(state -> {
            if (state.getPlaceholder() != null) {
                return state.getPlaceholder().getUniqueId().equals(e.getUniqueId());
            }
            return false;
        }).findFirst();
    }
}
