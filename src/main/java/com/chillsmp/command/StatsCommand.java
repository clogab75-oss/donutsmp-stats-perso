package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.gui.ChillGuiHolder;
import com.chillsmp.util.GuiItem;
import com.chillsmp.util.Msg;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;

public class StatsCommand implements CommandExecutor, Listener {

    private final ChillSMPPlugin plugin;

    public StatsCommand(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player viewer)) return true;

        Player target = viewer;
        if (args.length >= 1) {
            Player found = Bukkit.getPlayer(args[0]);
            if (found == null) {
                viewer.sendMessage(Msg.color(plugin.getConfig().getString("messages.prefix", "")
                    + plugin.getConfig().getString("messages.player-not-found")));
                return true;
            }
            target = found;
        }

        openStatsGUI(viewer, target);
        return true;
    }

    private void openStatsGUI(Player viewer, Player target) {
        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.STATS, target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 27, Msg.color("&8Stats &7| &b" + target.getName()));
        holder.setInventory(inv);

        ItemStack gray = GuiItem.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, gray);

        var stats = plugin.getStatsManager();

        // Tête
        inv.setItem(4, GuiItem.skull(target,
            "&7Toutes les statistiques de",
            "&b" + target.getName()
        ));

        // Playtime (montre)
        inv.setItem(10, GuiItem.make(Material.CLOCK, "&e⏱ Playtime",
            "&7Temps de jeu: &b" + stats.getPlaytimeFormatted(target.getUniqueId()),
            "&8Depuis le premier join."
        ));

        // Kills (épée)
        inv.setItem(11, GuiItem.make(Material.DIAMOND_SWORD, "&a⚔ Kills",
            "&7Joueurs tués: &a" + stats.getKills(target.getUniqueId())
        ));

        // Deaths (tête de zombie)
        inv.setItem(12, GuiItem.make(Material.SKELETON_SKULL, "&c☠ Deaths",
            "&7Fois mort: &c" + stats.getDeaths(target.getUniqueId())
        ));

        // K/D (livre)
        inv.setItem(13, GuiItem.make(Material.BOOK, "&e📊 Ratio K/D",
            "&7K/D: &e" + stats.getKD(target.getUniqueId())
        ));

        // Ping (la localisation/coordonnées a été retirée pour la confidentialité)
        if (target.isOnline()) {
            inv.setItem(14, GuiItem.make(Material.PAPER, "&7📶 Ping",
                "&7Latence: &a" + target.getPing() + "ms"
            ));
        }

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // La sécurité (empêcher de prendre les items) est gérée globalement
        // par GuiGuardListener via le ChillGuiHolder.
    }
}
