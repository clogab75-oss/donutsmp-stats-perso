package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class StackListener implements Listener {

    private final ChillSMPPlugin plugin;

    public StackListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (plugin.getStackManager().hasCustomStack(item.getType())) {
            plugin.getStackManager().applyToItem(item);
            event.getItem().setItemStack(item);
        }
    }
}
