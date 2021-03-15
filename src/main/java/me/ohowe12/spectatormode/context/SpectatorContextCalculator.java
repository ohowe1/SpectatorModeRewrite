package me.ohowe12.spectatormode.context;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.ohowe12.spectatormode.SpectatorMode;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;

public class SpectatorContextCalculator implements ContextCalculator<Player> {

    private final SpectatorMode plugin;

    public static void initalizeSpectatorContext(SpectatorMode plugin) {
        plugin.getLogger().info("Attempting to get provider for LuckPerms");
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            plugin.getLogger().info("Got provider!\nInitalizing with LuckPerms Context");
            api.getContextManager().registerCalculator(new SpectatorContextCalculator(plugin));
        }
    }

    public SpectatorContextCalculator(SpectatorMode plugin) {
        this.plugin = plugin;
    }

    @Override
    public void calculate(Player target, ContextConsumer contextConsumer) {
        contextConsumer.accept("SMP Spectator",
                plugin.getSpectatorCommand().inState(target.getUniqueId().toString()) ? "true" : "false");
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add("SMP Spectator", "true");
        builder.add("SMP Spectator", "false");
        return builder.build();
    }

}
