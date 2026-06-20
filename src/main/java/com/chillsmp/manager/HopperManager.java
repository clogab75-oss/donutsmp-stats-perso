package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class HopperManager {

    private final ChillSMPPlugin plugin;
    private int transferDelay;
    private int itemsPerTransfer;
    private BukkitTask task;

    // Map<chunk coords, last transfer time>
    private final Map<Long, Long> lastTransfer = new HashMap<>();

    public HopperManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        transferDelay = plugin.getConfig().getInt("hoppers.transfer-delay-ticks", 8);
        itemsPerTransfer = plugin.getConfig().getInt("hoppers.items-per-transfer", 1);
        if (task != null) task.cancel();
        // La vitesse est gérée via l'event InventoryMoveItemEvent dans HopperListener
        plugin.getLogger().info("[Hoppers] Délai: " + transferDelay + " ticks | Items/tick: " + itemsPerTransfer);
    }

    public int getTransferDelay() { return transferDelay; }
    public int getItemsPerTransfer() { return itemsPerTransfer; }
}
