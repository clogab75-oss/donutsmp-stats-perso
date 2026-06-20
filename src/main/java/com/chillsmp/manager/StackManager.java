package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class StackManager {

    private final ChillSMPPlugin plugin;
    private final Map<Material, Integer> customStacks = new HashMap<>();

    public StackManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        customStacks.clear();
        if (!plugin.getConfig().getBoolean("custom-stacking.enabled", true)) return;

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("custom-stacking.stackable-items");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key.toUpperCase());
                int size = Math.min(99, section.getInt(key, 64));
                customStacks.put(mat, size);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[Stack] Material inconnu dans la config: " + key);
            }
        }
        plugin.getLogger().info("[Stack] " + customStacks.size() + " items avec stack custom chargés.");
    }

    public boolean hasCustomStack(Material material) {
        return customStacks.containsKey(material);
    }

    public int getMaxStack(Material material) {
        return customStacks.getOrDefault(material, material.getMaxStackSize());
    }

    public void applyToItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        if (!hasCustomStack(item.getType())) return;
        int max = getMaxStack(item.getType());
        // Utiliser DataComponentTypes si disponible, sinon on gère par event
        try {
            item.setData(
                io.papermc.paper.datacomponent.DataComponentTypes.MAX_STACK_SIZE,
                max
            );
        } catch (Exception ignored) {
            // Fallback silencieux si l'API n'est pas disponible
        }
    }

    public Map<Material, Integer> getCustomStacks() {
        return customStacks;
    }
}
