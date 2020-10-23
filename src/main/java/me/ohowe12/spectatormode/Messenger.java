package me.ohowe12.spectatormode;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Messenger {

    private static ConfigManager configManager;

    public static void init(@NotNull ConfigManager configManager){
        Messenger.configManager = configManager;
    }

    public static void sendChat(@NotNull CommandSender target, @NotNull String msgkey){
        sendChat(target, msgkey, "");
    }

    public static void sendChat(@NotNull CommandSender target, @NotNull String msgkey, @NotNull String extra){
        String cfgmsg = Objects.requireNonNull(configManager, "Messenger not initialized")
                .getColorizedString(msgkey)
                .replaceAll("/target/", target.getName());
        ChatMessageType type = cfgmsg.startsWith("/actionbar/") ? ChatMessageType.ACTION_BAR : ChatMessageType.CHAT;
        cfgmsg = cfgmsg.replace("/actionbar/", "");
        cfgmsg += extra;
        if (!(target instanceof Player))
            target.spigot().sendMessage(new TextComponent(cfgmsg));
        else
            ((Player)target).spigot().sendMessage(type, new TextComponent(cfgmsg));
    }
}
