package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class TradeListener implements Listener {

    private final ChillSMPPlugin plugin;

    public TradeListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getTradeManager().isInTrade(player)) return;

        var tradeInv = plugin.getTradeManager().getTradeInventory(player);
        if (tradeInv == null) return;
        if (!event.getInventory().equals(tradeInv)) return;

        int slot = event.getRawSlot();

        // Bouton Accepter (slot 49)
        if (slot == 49) {
            event.setCancelled(true);
            handleAccept(player);
            return;
        }

        // Bouton Annuler (slot 45)
        if (slot == 45) {
            event.setCancelled(true);
            Player partner = plugin.getTradeManager().getPartner(player);
            plugin.getTradeManager().cancelTrade(player);
            String msg = Msg.color(plugin.getConfig().getString("messages.trade-cancelled", "&cTrade annulé."));
            player.sendMessage(msg);
            if (partner != null) partner.sendMessage(msg);
            return;
        }

        // Séparateurs (col 4, ligne 6) → annuler le clic
        if (plugin.getTradeManager().isSeparator(slot)) {
            event.setCancelled(true);
            return;
        }

        // Côté partenaire (cols 5-8) → lecture seule
        if (slot % 9 >= 5 && slot < 45) {
            event.setCancelled(true);
            return;
        }

        // Empêcher de prendre des items de son propre inventaire hors des slots d'offre
        // (slots d'inventaire joueur : 54+)
        // Autoriser uniquement les slots d'offre 0-3, 9-12, 18-21, 27-30
        if (slot < 45 && !plugin.getTradeManager().isOfferSlot(slot)) {
            event.setCancelled(true);
        }
    }

    private void handleAccept(Player player) {
        plugin.getTradeManager().setAccepted(player);
        Player partner = plugin.getTradeManager().getPartner(player);
        player.sendMessage(Msg.color("&aVous avez accepté le trade. En attente du partenaire..."));

        if (partner != null && plugin.getTradeManager().hasAccepted(partner)) {
            // Les deux ont accepté
            plugin.getTradeManager().completeTrade(player, partner);
            String done = Msg.color(plugin.getConfig().getString("messages.trade-completed", "&aTrade effectué !"));
            player.sendMessage(done);
            partner.sendMessage(done);
        } else if (partner != null) {
            partner.sendMessage(Msg.color("&b" + player.getName() + " &aa accepté le trade. Cliquez sur Accepter pour confirmer."));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.getTradeManager().isInTrade(player)) return;

        var tradeInv = plugin.getTradeManager().getTradeInventory(player);
        if (tradeInv == null) return;
        if (!event.getInventory().equals(tradeInv)) return;

        // Si l'inventaire est fermé sans accepter → annuler
        if (!plugin.getTradeManager().hasAccepted(player)) {
            Player partner = plugin.getTradeManager().getPartner(player);
            plugin.getTradeManager().cancelTrade(player);
            String msg = Msg.color(plugin.getConfig().getString("messages.trade-cancelled", "&cTrade annulé."));
            player.sendMessage(msg);
            if (partner != null) partner.sendMessage(msg);
        }
    }
}
