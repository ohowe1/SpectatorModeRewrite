package me.ohowe12.spectatormode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataSaver {

    private static final SpectatorMode plugin = SpectatorMode.getInstance();
    private static final FileConfiguration data = YamlConfiguration
        .loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));

    public static void save(final Map<String, State> state) {
        if (data.getConfigurationSection("data") != null) {
            Objects.requireNonNull(data.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                if (!state.containsKey(key)) {
                    data.set("data." + key, null);
                }
            });
        }
        for (@NotNull
        final Map.Entry<String, State> entry : state.entrySet()) {
            data.set("data." + entry.getKey(), entry.getValue().serialize());
        }
        try {
            data.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(final Map<String, State> state) {
        if (data.getConfigurationSection("data") == null) {
            return;
        }
        try {
            Objects.requireNonNull(data.getConfigurationSection("data")).getKeys(false).forEach(key -> {
                @Nullable
                final Map<String, Object> value = new HashMap<>();

                final ArrayList<PotionEffect> potions = (ArrayList<PotionEffect>) data
                        .getList("data." + key + ".Potions");
                value.put("Potions", potions);

                final int waterBubbles = data.getInt("data." + key + ".Water bubbles");
                value.put("Water bubbles", waterBubbles);

                final Map<String, Boolean> mobs = new HashMap<>();
                Objects.requireNonNull(data.getConfigurationSection("data." + key + ".Mobs")).getKeys(false)
                        .forEach(mobKey -> mobs.put(mobKey, data.getBoolean("data." + key + ".Mobs" + mobKey)));
                value.put("Mobs", mobs);

                final int fireTicks = data.getInt("data." + key + ".Fire ticks");
                value.put("Fire ticks", fireTicks);

                final Location location = data.getLocation("data." + key + ".Location");
                value.put("Location", location);

                state.put(key, new State(value));
            });
        } catch (final NullPointerException ignored) {
        }
    }
}
