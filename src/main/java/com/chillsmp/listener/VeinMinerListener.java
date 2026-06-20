package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class VeinMinerListener implements Listener {

    private final ChillSMPPlugin plugin;
    private final Set<UUID> processing = new HashSet<>();

    public VeinMinerListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID uuid = player.getUniqueId();

        if (processing.contains(uuid)) return;

        boolean isVein = plugin.getConfig().getBoolean("vein-miner.enabled", true)
            && isVeinBlock(block.getType())
            && checkTool(player, "vein-miner")
            && (!plugin.getConfig().getBoolean("vein-miner.require-sneak", true) || player.isSneaking());

        boolean isTree = plugin.getConfig().getBoolean("tree-feller.enabled", true)
            && isLogBlock(block.getType())
            && checkTool(player, "tree-feller")
            && (!plugin.getConfig().getBoolean("tree-feller.require-sneak", true) || player.isSneaking());

        if (!isVein && !isTree) return;

        processing.add(uuid);
        try {
            if (isVein) veinMine(player, block);
            else treeFell(player, block);
        } finally {
            processing.remove(uuid);
        }
    }

    private void veinMine(Player player, Block origin) {
        int maxBlocks = plugin.getConfig().getInt("vein-miner.max-blocks", 64);
        int durPerBlock = plugin.getConfig().getInt("vein-miner.durability-per-block", 1);
        Material type = origin.getType();
        Set<Block> toBreak = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(origin);

        while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
            Block current = queue.poll();
            if (!toBreak.add(current)) continue;
            for (Block neighbor : getAdjacentBlocks(current)) {
                if (neighbor.getType() == type && !toBreak.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        toBreak.remove(origin);
        for (Block b : toBreak) {
            if (!canBreakMore(player, durPerBlock)) break;
            b.breakNaturally(player.getInventory().getItemInMainHand());
            applyDurability(player, durPerBlock);
        }
    }

    private void treeFell(Player player, Block origin) {
        int maxBlocks = plugin.getConfig().getInt("tree-feller.max-blocks", 150);
        int durPerBlock = plugin.getConfig().getInt("tree-feller.durability-per-block", 1);
        Set<Block> toBreak = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(origin);

        while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
            Block current = queue.poll();
            if (!toBreak.add(current)) continue;
            for (Block neighbor : getAdjacentBlocksTree(current)) {
                if (isLogBlock(neighbor.getType()) && !toBreak.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        toBreak.remove(origin);
        for (Block b : toBreak) {
            if (!canBreakMore(player, durPerBlock)) break;
            b.breakNaturally(player.getInventory().getItemInMainHand());
            applyDurability(player, durPerBlock);
        }
    }

    private List<Block> getAdjacentBlocks(Block block) {
        List<Block> blocks = new ArrayList<>();
        Location loc = block.getLocation();
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++)
                for (int z = -1; z <= 1; z++)
                    if (x != 0 || y != 0 || z != 0)
                        blocks.add(loc.clone().add(x, y, z).getBlock());
        return blocks;
    }

    private List<Block> getAdjacentBlocksTree(Block block) {
        List<Block> blocks = new ArrayList<>();
        Location loc = block.getLocation();
        for (int x = -1; x <= 1; x++)
            for (int y = 0; y <= 1; y++)
                for (int z = -1; z <= 1; z++)
                    if (x != 0 || y != 0 || z != 0)
                        blocks.add(loc.clone().add(x, y, z).getBlock());
        return blocks;
    }

    private boolean checkTool(Player player, String configKey) {
        String required = plugin.getConfig().getString(configKey + ".required-tool", "PICKAXE").toUpperCase();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return false;
        String itemName = item.getType().name();
        return itemName.contains(required);
    }

    private boolean canBreakMore(Player player, int durCost) {
        if (durCost <= 0) return true;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!(item.getItemMeta() instanceof Damageable meta)) return true;
        return meta.getDamage() + durCost < item.getType().getMaxDurability();
    }

    private void applyDurability(Player player, int amount) {
        if (amount <= 0) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!(item.getItemMeta() instanceof Damageable meta)) return;
        meta.setDamage(meta.getDamage() + amount);
        item.setItemMeta(meta);
    }

    private boolean isVeinBlock(Material mat) {
        List<String> blocks = plugin.getConfig().getStringList("vein-miner.vein-blocks");
        return blocks.contains(mat.name());
    }

    private boolean isLogBlock(Material mat) {
        List<String> blocks = plugin.getConfig().getStringList("tree-feller.log-blocks");
        return blocks.contains(mat.name());
    }
}
