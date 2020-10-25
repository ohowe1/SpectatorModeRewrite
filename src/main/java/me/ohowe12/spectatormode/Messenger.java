package me.ohowe12.spectatormode;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Messenger {

    private static SpectatorMode plugin;

    public static void init(@NotNull SpectatorMode plugin) {
        Messenger.plugin = plugin;
    }

    public static void send(@NotNull CommandSender sender, @NotNull String msgkey) {
        send(sender, sender, msgkey, "");
    }

    public static void send(@NotNull CommandSender sender, @NotNull CommandSender target, @NotNull String msgkey) {
        send(sender, target, msgkey, "");
    }

    public static void send(@NotNull CommandSender sender, @NotNull String msgkey, @NotNull String extra) {
        send(sender, sender, msgkey, extra);
    }

    public static void send(@NotNull CommandSender sender, @NotNull CommandSender target, @NotNull String msgkey, @NotNull String extra) {
        String cfgmsg = Objects.requireNonNull(plugin.getConfigManager(), "Messenger not initialized")
                .getColorizedString(msgkey)
                .replaceAll("/target/", target.getName());
        ChatMessageType type = cfgmsg.startsWith("/actionbar/") ? ChatMessageType.ACTION_BAR : ChatMessageType.CHAT;
        cfgmsg = cfgmsg.replace("/actionbar/", "");
        cfgmsg += extra;
        if (!(sender instanceof Player) || plugin.getUnitTest())
            sender.sendMessage(cfgmsg);
        else
            ((Player)sender).spigot().sendMessage(type, new TextComponent(cfgmsg));
    }
}
