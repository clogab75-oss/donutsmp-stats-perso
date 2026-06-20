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

import java.util.UUID;

public class StaffCommand implements CommandExecutor, Listener {

    private final ChillSMPPlugin plugin;

    public StaffCommand(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.isOp()) {
            player.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        openMainDashboard(player);
        return true;
    }

    private void openMainDashboard(Player viewer) {
        int size = Math.max(9, (int) Math.ceil(Bukkit.getOnlinePlayers().size() / 9.0) * 9);
        size = Math.min(54, size);

        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.STAFF_LIST);
        Inventory inv = Bukkit.createInventory(holder, size, Msg.color("&8Staff Dashboard &7| &bJoueurs"));
        holder.setInventory(inv);

        for (Player target : Bukkit.getOnlinePlayers()) {
            var stats = plugin.getStatsManager();
            ItemStack skull = GuiItem.skull(target,
                "&7Kills: &a" + stats.getKills(target.getUniqueId()),
                "&7Deaths: &c" + stats.getDeaths(target.getUniqueId()),
                "&7Playtime: &b" + stats.getPlaytimeFormatted(target.getUniqueId()),
                "&7Ping: &e" + target.getPing() + "ms",
                "&7OP: " + (target.isOp() ? "&aOui" : "&cNon"),
                "",
                "&7Cliquez pour gérer ce joueur."
            );
            inv.addItem(skull);
        }
        viewer.openInventory(inv);
    }

    private void openPlayerMenu(Player viewer, Player target) {
        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.STAFF_DETAIL, target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 54, Msg.color("&8Staff &7| &b" + target.getName()));
        holder.setInventory(inv);

        ItemStack gray = GuiItem.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);

        var stats = plugin.getStatsManager();
        ItemStack skull = GuiItem.skull(target,
            "&7Kills: &a" + stats.getKills(target.getUniqueId()),
            "&7Deaths: &c" + stats.getDeaths(target.getUniqueId()),
            "&7Playtime: &b" + stats.getPlaytimeFormatted(target.getUniqueId()),
            "&7Ping: &e" + target.getPing() + "ms",
            "&7OP: " + (target.isOp() ? "&aOui" : "&cNon"),
            "&7Monde: &b" + target.getWorld().getName()
        );
        inv.setItem(4, skull);

        inv.setItem(19, GuiItem.make(Material.DIAMOND_SWORD, "&c&lKill", "&7Tuer ce joueur."));
        inv.setItem(20, GuiItem.make(Material.ENDER_EYE, "&b&lTP à moi", "&7Téléporter ce joueur à vous."));
        inv.setItem(21, GuiItem.make(Material.COMPASS, "&b&lSe TP à lui", "&7Vous téléporter à ce joueur."));
        inv.setItem(22, GuiItem.make(Material.CHEST, "&e&lInventaire",
            "&7Voir l'inventaire de ce joueur,",
            "&7armure et main secondaire incluses.",
            "&8Lecture seule, mis à jour en direct."));
        inv.setItem(23, GuiItem.make(Material.ENDER_CHEST, "&d&lE. Chest",
            "&7Voir l'ender chest de ce joueur.",
            "&8Lecture seule, mis à jour en direct."));
        inv.setItem(24, GuiItem.make(Material.BARRIER, "&c&lBan Temp (1h)",
            "&7Cliquez pour bannir 1h rapidement.",
            "&7Pour plus de contrôle:",
            "&b/tempban <joueur> <durée> [raison]"));
        inv.setItem(25, GuiItem.make(Material.BOOK, "&f&lStats XRay", "&7Voir les stats anti-xray."));

        inv.setItem(49, GuiItem.make(Material.ARROW, "&7Retour", "&7Revenir à la liste des joueurs."));

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof ChillGuiHolder holder)) return;

        ItemStack clicked = event.getCurrentItem();

        if (holder.getType() == ChillGuiHolder.Type.STAFF_LIST) {
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;
            if (!(clicked.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta meta)) return;
            if (meta.getOwningPlayer() == null) return;
            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
            if (target != null) openPlayerMenu(viewer, target);
            return;
        }

        if (holder.getType() == ChillGuiHolder.Type.STAFF_DETAIL) {
            if (clicked == null) return;
            UUID targetUUID = holder.getTargetUUID();
            if (targetUUID == null) return;
            Player target = Bukkit.getPlayer(targetUUID);

            switch (event.getSlot()) {
                case 19 -> {
                    if (target != null) {
                        target.setHealth(0);
                        viewer.sendMessage(Msg.color("&aVous avez tué &b" + target.getName() + "&a."));
                    }
                }
                case 20 -> {
                    if (target != null) {
                        target.teleport(viewer.getLocation());
                        viewer.sendMessage(Msg.color("&b" + target.getName() + " &atéléporté à vous."));
                        target.sendMessage(Msg.color("&aVous avez été téléporté à &b" + viewer.getName() + "&a."));
                    }
                }
                case 21 -> {
                    if (target != null) {
                        viewer.teleport(target.getLocation());
                        viewer.sendMessage(Msg.color("&aTéléporté à &b" + target.getName() + "&a."));
                    }
                }
                case 22 -> {
                    if (target != null) {
                        viewer.closeInventory();
                        plugin.getInspectManager().openInventoryView(viewer, target);
                    }
                }
                case 23 -> {
                    if (target != null) {
                        viewer.closeInventory();
                        plugin.getInspectManager().openEnderChestView(viewer, target);
                    }
                }
                case 24 -> {
                    viewer.closeInventory();
                    if (target != null) {
                        plugin.getBanManager().tempBan(target.getUniqueId(), target.getName(),
                            3600000L, "Banni par un staff", viewer.getName());
                        viewer.sendMessage(Msg.color("&b" + target.getName() + " &abanni pour &e1h&a."));
                    }
                }
                case 25 -> {
                    if (target != null) {
                        viewer.closeInventory();
                        viewer.performCommand("xray " + target.getName());
                    }
                }
                case 49 -> openMainDashboard(viewer);
            }
        }
    }
}
