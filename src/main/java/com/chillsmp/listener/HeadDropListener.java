package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;

public class HeadDropListener implements Listener {

    private final ChillSMPPlugin plugin;
    private final Random random = new Random();

    public HeadDropListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("head-drop.enabled", true)) return;

        Player dead = event.getEntity();
        double chance = plugin.getConfig().getDouble("head-drop.drop-chance", 1.0);

        if (random.nextDouble() > chance) return;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return;

        meta.setOwningPlayer(dead);
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&',
            "&b☠ Tête de " + dead.getName()));
        head.setItemMeta(meta);

        dead.getWorld().dropItemNaturally(dead.getLocation(), head);

        String msg = plugin.getConfig().getString("messages.head-drop",
            "&7La tête de &c{player}&7 a été droppée !");
        msg = org.bukkit.ChatColor.translateAlternateColorCodes('&',
            msg.replace("{player}", dead.getName()));

        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }
}
