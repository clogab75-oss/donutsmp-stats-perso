package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.gui.ChillGuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Verrou de sécurité global : empêche de prendre, déplacer ou drag les items
 * affichés dans n'importe quel menu en lecture seule du plugin (xray, staff,
 * stats, inspect inventaire/ender chest). Le menu de trade gère ses propres
 * règles séparément car il autorise certains slots.
 */
public class GuiGuardListener implements Listener {

    private final ChillSMPPlugin plugin;

    public GuiGuardListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isReadOnlyGui(InventoryHolder holder) {
        if (!(holder instanceof ChillGuiHolder chillHolder)) return false;
        return switch (chillHolder.getType()) {
            case XRAY_LIST, XRAY_DETAIL, STAFF_LIST, STAFF_DETAIL, STATS -> true;
            case INSPECT_INVENTORY, INSPECT_ENDERCHEST -> false; // géré par InspectManager
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (isReadOnlyGui(topHolder)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (isReadOnlyGui(topHolder)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ChillGuiHolder chillHolder
            && (chillHolder.getType() == ChillGuiHolder.Type.INSPECT_INVENTORY
                || chillHolder.getType() == ChillGuiHolder.Type.INSPECT_ENDERCHEST)) {
            plugin.getInspectManager().stopRefresh(player);
        }
    }
}
