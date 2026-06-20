package com.chillsmp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Msg {

    private static final LegacyComponentSerializer SERIALIZER =
        LegacyComponentSerializer.legacyAmpersand();

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static Component component(String text) {
        return SERIALIZER.deserialize(text);
    }

    public static void send(Player player, String text) {
        player.sendMessage(color(text));
    }

    public static void sendComponent(Player player, Component component) {
        player.sendMessage(component);
    }
}
