package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.block.Hopper;

import java.util.HashMap;
import java.util.Map;

public class HopperListener implements Listener {

    private final ChillSMPPlugin plugin;
    // Dernière transfert par hopper (clé = location hash)
    private final Map<Long, Long> lastTick = new HashMap<>();

    public HopperListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Hopper hopper)) return;

        int delay = plugin.getHopperManager().getTransferDelay();
        if (delay <= 0) return;

        long key = hopper.getLocation().hashCode();
        long current = org.bukkit.Bukkit.getServer().getCurrentTick();
        long last = lastTick.getOrDefault(key, 0L);

        if (current - last < delay) {
            event.setCancelled(true);
            return;
        }

        lastTick.put(key, current);
    }
}
