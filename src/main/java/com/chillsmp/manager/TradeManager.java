package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TradeManager {

    private final ChillSMPPlugin plugin;

    // UUID sender -> UUID receiver (demandes en attente)
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    // UUID joueur -> UUID partenaire de trade actif
    private final Map<UUID, UUID> activeTradePartner = new HashMap<>();
    // UUID joueur -> inventaire de trade (double coffre)
    private final Map<UUID, Inventory> tradeInventories = new HashMap<>();
    // Joueurs ayant accepté
    private final Set<UUID> accepted = new HashSet<>();

    public TradeManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player sender, Player target) {
        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        int timeout = plugin.getConfig().getInt("trade.request-timeout", 30);
        // Annuler après timeout
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(target.getUniqueId())
                && pendingRequests.get(target.getUniqueId()).equals(sender.getUniqueId())) {
                pendingRequests.remove(target.getUniqueId());
                if (sender.isOnline()) sender.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                    "&cLa demande de trade a expiré."));
                if (target.isOnline()) target.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                    "&cLa demande de trade de &b" + sender.getName() + "&c a expiré."));
            }
        }, timeout * 20L);
    }

    public boolean hasPendingRequest(Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }

    public UUID getPendingRequestSender(Player player) {
        return pendingRequests.get(player.getUniqueId());
    }

    public void acceptRequest(Player accepter) {
        UUID senderUUID = pendingRequests.remove(accepter.getUniqueId());
        if (senderUUID == null) return;
        Player sender = Bukkit.getPlayer(senderUUID);
        if (sender == null || !sender.isOnline()) {
            accepter.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                "&cCe joueur n'est plus connecté."));
            return;
        }
        startTrade(sender, accepter);
    }

    public void startTrade(Player p1, Player p2) {
        activeTradePartner.put(p1.getUniqueId(), p2.getUniqueId());
        activeTradePartner.put(p2.getUniqueId(), p1.getUniqueId());

        // Créer deux inventaires liés
        Inventory inv1 = createTradeInventory(p1, p2);
        Inventory inv2 = createTradeInventory(p2, p1);

        tradeInventories.put(p1.getUniqueId(), inv1);
        tradeInventories.put(p2.getUniqueId(), inv2);

        p1.openInventory(inv1);
        p2.openInventory(inv2);
    }

    private Inventory createTradeInventory(Player viewer, Player partner) {
        Inventory inv = Bukkit.createInventory(null, 54,
            net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&',
                "&8Trade: &b" + viewer.getName() + " &7↔ &b" + partner.getName()));

        // Remplissage des bordures avec verre gris
        org.bukkit.inventory.ItemStack gray = com.chillsmp.util.GuiItem.glass(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        // Séparateur central (colonne 4)
        for (int row = 0; row < 6; row++) {
            inv.setItem(row * 9 + 4, gray);
        }
        // Slots offre propre: 0-3 sur chaque ligne (0-3, 9-12, 18-21, 27-30)
        // Slots offre partenaire: 5-8 sur chaque ligne
        // Ligne de statut: slots 45-53
        for (int i = 45; i < 54; i++) inv.setItem(i, gray);

        // Bouton accepter (vert)
        org.bukkit.inventory.ItemStack accept = com.chillsmp.util.GuiItem.make(
            org.bukkit.Material.GREEN_STAINED_GLASS_PANE,
            "&a&lACCEPTER",
            "&7Cliquez pour accepter le trade."
        );
        inv.setItem(49, accept);

        // Bouton annuler (rouge)
        org.bukkit.inventory.ItemStack cancel = com.chillsmp.util.GuiItem.make(
            org.bukkit.Material.RED_STAINED_GLASS_PANE,
            "&c&lANNULER",
            "&7Cliquez pour annuler le trade."
        );
        inv.setItem(45, cancel);

        return inv;
    }

    public boolean isInTrade(Player player) {
        return activeTradePartner.containsKey(player.getUniqueId());
    }

    public Player getPartner(Player player) {
        UUID partnerUUID = activeTradePartner.get(player.getUniqueId());
        if (partnerUUID == null) return null;
        return Bukkit.getPlayer(partnerUUID);
    }

    public Inventory getTradeInventory(Player player) {
        return tradeInventories.get(player.getUniqueId());
    }

    public void setAccepted(Player player) { accepted.add(player.getUniqueId()); }
    public boolean hasAccepted(Player player) { return accepted.contains(player.getUniqueId()); }
    public void removeAccepted(Player player) { accepted.remove(player.getUniqueId()); }

    public void completeTrade(Player p1, Player p2) {
        Inventory inv1 = tradeInventories.get(p1.getUniqueId());
        Inventory inv2 = tradeInventories.get(p2.getUniqueId());

        // Collecter les items de chaque côté (slots 0-3, 9-12, 18-21, 27-30)
        List<ItemStack> items1 = getOfferSlots(inv1);
        List<ItemStack> items2 = getOfferSlots(inv2);

        // Retirer d'abord les items des inventaires des joueurs
        for (ItemStack item : items1) {
            if (item != null) p1.getInventory().removeItem(item);
        }
        for (ItemStack item : items2) {
            if (item != null) p2.getInventory().removeItem(item);
        }

        // Donner les items
        for (ItemStack item : items2) {
            if (item != null) {
                Map<Integer, ItemStack> leftover = p1.getInventory().addItem(item);
                for (ItemStack lo : leftover.values()) p1.getWorld().dropItemNaturally(p1.getLocation(), lo);
            }
        }
        for (ItemStack item : items1) {
            if (item != null) {
                Map<Integer, ItemStack> leftover = p2.getInventory().addItem(item);
                for (ItemStack lo : leftover.values()) p2.getWorld().dropItemNaturally(p2.getLocation(), lo);
            }
        }

        cancelTrade(p1);
    }

    private List<ItemStack> getOfferSlots(Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        int[] slots = {0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30};
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null) items.add(item.clone());
        }
        return items;
    }

    public void cancelTrade(Player player) {
        UUID partnerUUID = activeTradePartner.remove(player.getUniqueId());
        activeTradePartner.remove(partnerUUID);
        accepted.remove(player.getUniqueId());
        if (partnerUUID != null) {
            accepted.remove(partnerUUID);
            Player partner = Bukkit.getPlayer(partnerUUID);
            if (partner != null) {
                tradeInventories.remove(partner.getUniqueId());
                partner.closeInventory();
            }
        }
        tradeInventories.remove(player.getUniqueId());
        player.closeInventory();
    }

    public boolean isSeparator(int slot) {
        // Colonne 4 + ligne 6
        return slot % 9 == 4 || slot >= 45;
    }

    public boolean isOfferSlot(int slot) {
        int col = slot % 9;
        int row = slot / 9;
        return col <= 3 && row <= 4;
    }
}
