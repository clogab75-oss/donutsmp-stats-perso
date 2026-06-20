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

import java.util.Map;
import java.util.UUID;

public class XRayCommand implements CommandExecutor, Listener {

    private final ChillSMPPlugin plugin;

    public XRayCommand(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande joueur uniquement.");
            return true;
        }
        if (!player.isOp()) {
            player.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length >= 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Msg.color(plugin.getConfig().getString("messages.player-not-found")));
                return true;
            }
            openPlayerStats(player, target);
            return true;
        }

        openPlayerList(player);
        return true;
    }

    private void openPlayerList(Player viewer) {
        int size = Math.max(9, ((int) Math.ceil(Bukkit.getOnlinePlayers().size() / 9.0)) * 9);
        size = Math.min(54, size);

        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.XRAY_LIST);
        Inventory inv = Bukkit.createInventory(holder, size, Msg.color("&8Anti-XRay &7| &bJoueurs connectés"));
        holder.setInventory(inv);

        for (Player target : Bukkit.getOnlinePlayers()) {
            double ratio = plugin.getXRayManager().getRatio(target.getUniqueId());
            int ores = plugin.getXRayManager().getTotalOres(target.getUniqueId());
            int stone = plugin.getXRayManager().getStoneCount(target.getUniqueId());
            boolean sus = plugin.getXRayManager().isSuspicious(target.getUniqueId());

            String ratioColor = sus ? "&c" : ratio > 5 ? "&e" : "&a";

            ItemStack skull = GuiItem.skull(target,
                "&7Minerais: &b" + ores,
                "&7Roche: &b" + stone,
                "&7Ratio: " + ratioColor + String.format("%.1f", ratio) + "%",
                sus ? "&c⚠ SUSPECT" : "&aOK",
                "",
                "&7Cliquez pour voir les détails."
            );
            inv.addItem(skull);
        }

        viewer.openInventory(inv);
    }

    private void openPlayerStats(Player viewer, Player target) {
        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.XRAY_DETAIL, target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 54, Msg.color("&8XRay &7| &b" + target.getName()));
        holder.setInventory(inv);

        ItemStack gray = GuiItem.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);

        double ratio = plugin.getXRayManager().getRatio(target.getUniqueId());
        boolean sus = plugin.getXRayManager().isSuspicious(target.getUniqueId());
        ItemStack skull = GuiItem.skull(target,
            "&7Total minerais: &b" + plugin.getXRayManager().getTotalOres(target.getUniqueId()),
            "&7Total roche: &b" + plugin.getXRayManager().getStoneCount(target.getUniqueId()),
            "&7Ratio: &e" + String.format("%.1f", ratio) + "%",
            sus ? "&c⚠ ACTIVITÉ SUSPECTE" : "&a✓ Normal"
        );
        inv.setItem(4, skull);

        Map<Material, Integer> breakdown = plugin.getXRayManager().getOreBreakdown(target.getUniqueId());
        int slot = 19;
        for (Map.Entry<Material, Integer> entry : breakdown.entrySet()) {
            if (slot > 26) break;
            ItemStack oreItem = GuiItem.make(entry.getKey(),
                "&b" + formatMat(entry.getKey().name()),
                "&7Quantité: &e" + entry.getValue()
            );
            inv.setItem(slot++, oreItem);
        }

        inv.setItem(45, GuiItem.make(Material.ARROW, "&7Retour",
            "&7Revenir à la liste des joueurs."));
        inv.setItem(49, GuiItem.make(Material.BARRIER, "&cReset les données",
            "&7Efface les statistiques de ce joueur."));

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof ChillGuiHolder holder)) return;

        // La sécurité (cancel) est gérée globalement par GuiGuardListener.
        // Ici on traite uniquement les actions des clics.
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        switch (holder.getType()) {
            case XRAY_LIST -> {
                if (clicked.getType() != Material.PLAYER_HEAD) return;
                if (!(clicked.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta meta)) return;
                if (meta.getOwningPlayer() == null) return;
                Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                if (target != null) openPlayerStats(player, target);
            }
            case XRAY_DETAIL -> {
                UUID targetUUID = holder.getTargetUUID();
                if (targetUUID == null) return;
                Player target = Bukkit.getPlayer(targetUUID);

                if (clicked.getType() == Material.ARROW) {
                    openPlayerList(player);
                } else if (clicked.getType() == Material.BARRIER && target != null) {
                    plugin.getXRayManager().reset(target.getUniqueId());
                    player.sendMessage(Msg.color("&aDonnées XRay de &b" + target.getName() + " &areset."));
                    player.closeInventory();
                }
            }
            default -> {}
        }
    }

    private String formatMat(String name) {
        String s = name.replace("_", " ").toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
