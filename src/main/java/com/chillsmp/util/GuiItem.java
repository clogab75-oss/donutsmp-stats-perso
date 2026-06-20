package com.chillsmp.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiItem {

    public static ItemStack make(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(Msg.color(name));
        if (lore.length > 0) {
            List<String> colored = Arrays.stream(lore)
                .map(Msg::color)
                .collect(Collectors.toList());
            meta.setLore(colored);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack glass(Material glassColor) {
        ItemStack item = new ItemStack(glassColor);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack skull(Player player, String... lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.setOwningPlayer(player);
        meta.setDisplayName(Msg.color("&b" + player.getName()));
        if (lore.length > 0) {
            List<String> colored = Arrays.stream(lore)
                .map(Msg::color)
                .collect(Collectors.toList());
            meta.setLore(colored);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack fill(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }
}
