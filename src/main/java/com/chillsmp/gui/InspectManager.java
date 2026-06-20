package com.chillsmp.gui;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.GuiItem;
import com.chillsmp.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ouvre une vue "miroir" interactive de l'inventaire (ou de l'ender chest) d'un joueur,
 * armure et main secondaire incluses. Contrairement à une vraie PlayerInventory native,
 * Bukkit ne permet pas nativement d'afficher l'armure/offhand d'un AUTRE joueur dans une
 * vue interactive (c'est une limitation connue de l'API, contournée habituellement par
 * des plugins comme OpenInv via du code interne au serveur). On reproduit donc le même
 * principe : un inventaire custom qui se resynchronise automatiquement avec le joueur
 * cible, et qui réécrit immédiatement chaque modification dans son vrai inventaire.
 */
public class InspectManager implements Listener {

    private final ChillSMPPlugin plugin;
    private final Map<UUID, BukkitTask> refreshTasks = new HashMap<>();
    private final Map<UUID, UUID> viewerToTarget = new HashMap<>();

    private static final int[] ARMOR_SLOTS = {37, 38, 39, 40}; // casque, plastron, jambières, bottes (affichage)
    private static final int OFFHAND_SLOT = 41;
    private static final int CLOSE_SLOT = 49;

    public InspectManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openInventoryView(Player viewer, Player target) {
        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.INSPECT_INVENTORY, target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 54,
            Msg.color("&8Inventaire &7| &b" + target.getName()));
        holder.setInventory(inv);

        fillStaticDecorations(inv);
        pushFromTarget(inv, target, true);

        viewer.openInventory(inv);
        viewerToTarget.put(viewer.getUniqueId(), target.getUniqueId());
        startRefresh(viewer, inv, true);
    }

    public void openEnderChestView(Player viewer, Player target) {
        ChillGuiHolder holder = new ChillGuiHolder(ChillGuiHolder.Type.INSPECT_ENDERCHEST, target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, 27,
            Msg.color("&8Ender Chest &7| &b" + target.getName()));
        holder.setInventory(inv);

        pushFromTarget(inv, target, false);

        viewer.openInventory(inv);
        viewerToTarget.put(viewer.getUniqueId(), target.getUniqueId());
        startRefresh(viewer, inv, false);
    }

    private void startRefresh(Player viewer, Inventory inv, boolean isMainInventory) {
        stopRefresh(viewer);
        // Rafraîchissement périodique pour rester "en direct" si la cible bouge ses items
        // de son côté. Intervalle volontairement assez large pour limiter les conflits
        // si le staff est en train de glisser un item au même moment.
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            UUID targetUUID = viewerToTarget.get(viewer.getUniqueId());
            if (targetUUID == null || !viewer.isOnline()) {
                stopRefresh(viewer);
                return;
            }
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null || !target.isOnline()) {
                viewer.closeInventory();
                viewer.sendMessage(Msg.color("&cLe joueur s'est déconnecté."));
                stopRefresh(viewer);
                return;
            }
            if (viewer.getOpenInventory() == null
                || !viewer.getOpenInventory().getTopInventory().equals(inv)) {
                stopRefresh(viewer);
                return;
            }
            pushFromTarget(inv, target, isMainInventory);
        }, 20L, 20L);
        refreshTasks.put(viewer.getUniqueId(), task);
    }

    public void stopRefresh(Player viewer) {
        BukkitTask task = refreshTasks.remove(viewer.getUniqueId());
        if (task != null) task.cancel();
        viewerToTarget.remove(viewer.getUniqueId());
    }

    private void fillStaticDecorations(Inventory inv) {
        ItemStack gray = GuiItem.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 42; i < 54; i++) {
            if (i == CLOSE_SLOT) continue;
            inv.setItem(i, gray);
        }
        inv.setItem(CLOSE_SLOT, GuiItem.make(Material.BARRIER, "&c&lFermer",
            "&7Ferme cette vue."));
        // Petits labels au-dessus des slots armure/offhand au premier affichage uniquement
        // (seront écrasés par les vrais items dès que la cible en porte)
    }

    /** Copie les données du joueur cible VERS l'inventaire affiché (lecture cible -> GUI). */
    private void pushFromTarget(Inventory inv, Player target, boolean isMainInventory) {
        if (isMainInventory) {
            PlayerInventory pInv = target.getInventory();
            ItemStack[] storage = pInv.getStorageContents();
            for (int i = 0; i < storage.length && i < 36; i++) {
                inv.setItem(i, storage[i] == null ? null : storage[i].clone());
            }
            ItemStack[] armor = pInv.getArmorContents(); // ordre: bottes, jambières, plastron, casque
            inv.setItem(ARMOR_SLOTS[0], placeholderIfEmpty(armor[3], "&fCasque"));
            inv.setItem(ARMOR_SLOTS[1], placeholderIfEmpty(armor[2], "&fPlastron"));
            inv.setItem(ARMOR_SLOTS[2], placeholderIfEmpty(armor[1], "&fJambières"));
            inv.setItem(ARMOR_SLOTS[3], placeholderIfEmpty(armor[0], "&fBottes"));
            inv.setItem(OFFHAND_SLOT, placeholderIfEmpty(pInv.getItemInOffHand(), "&fMain secondaire"));
        } else {
            ItemStack[] contents = target.getEnderChest().getContents();
            for (int i = 0; i < contents.length && i < 27; i++) {
                inv.setItem(i, contents[i] == null ? null : contents[i].clone());
            }
        }
    }

    private ItemStack placeholderIfEmpty(ItemStack item, String label) {
        if (item == null || item.getType() == Material.AIR) {
            ItemStack placeholder = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            org.bukkit.inventory.meta.ItemMeta meta = placeholder.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Msg.color("&7" + label + ": &8vide"));
                meta.setLore(java.util.List.of(Msg.color("&8Déposez un item ici pour l'équiper.")));
                placeholder.setItemMeta(meta);
            }
            return placeholder;
        }
        return item.clone();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof ChillGuiHolder holder)) return;
        if (holder.getType() != ChillGuiHolder.Type.INSPECT_INVENTORY
            && holder.getType() != ChillGuiHolder.Type.INSPECT_ENDERCHEST) return;

        UUID targetUUID = holder.getTargetUUID();
        Player target = targetUUID != null ? Bukkit.getPlayer(targetUUID) : null;
        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        boolean clickedTop = rawSlot < topSize;

        if (holder.getType() == ChillGuiHolder.Type.INSPECT_INVENTORY) {
            if (clickedTop) {
                if (rawSlot == CLOSE_SLOT) {
                    event.setCancelled(true);
                    viewer.closeInventory();
                    return;
                }
                boolean isEditableSlot = rawSlot < 36
                    || rawSlot == ARMOR_SLOTS[0] || rawSlot == ARMOR_SLOTS[1]
                    || rawSlot == ARMOR_SLOTS[2] || rawSlot == ARMOR_SLOTS[3]
                    || rawSlot == OFFHAND_SLOT;
                if (!isEditableSlot) {
                    event.setCancelled(true);
                    return;
                }
            }
            // Bloquer le shift-clic pour éviter qu'un item atterrisse dans une case décorative
            if (event.isShiftClick()) {
                event.setCancelled(true);
                viewer.sendMessage(Msg.color("&7Glissez l'item manuellement (pas de shift-clic dans cette vue)."));
                return;
            }
            if (target != null) {
                Bukkit.getScheduler().runTask(plugin, () -> syncToTargetInventory(event.getInventory(), target));
            }
        } else { // INSPECT_ENDERCHEST
            if (event.isShiftClick()) {
                event.setCancelled(true);
                viewer.sendMessage(Msg.color("&7Glissez l'item manuellement (pas de shift-clic dans cette vue)."));
                return;
            }
            if (target != null) {
                Bukkit.getScheduler().runTask(plugin, () -> syncToTargetEnderChest(event.getInventory(), target));
            }
        }
    }

    private void syncToTargetInventory(Inventory guiInv, Player target) {
        ItemStack[] newStorage = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            ItemStack item = guiInv.getItem(i);
            newStorage[i] = item;
        }
        target.getInventory().setStorageContents(newStorage);

        target.getInventory().setHelmet(unwrapPlaceholder(guiInv.getItem(ARMOR_SLOTS[0])));
        target.getInventory().setChestplate(unwrapPlaceholder(guiInv.getItem(ARMOR_SLOTS[1])));
        target.getInventory().setLeggings(unwrapPlaceholder(guiInv.getItem(ARMOR_SLOTS[2])));
        target.getInventory().setBoots(unwrapPlaceholder(guiInv.getItem(ARMOR_SLOTS[3])));
        target.getInventory().setItemInOffHand(unwrapPlaceholder(guiInv.getItem(OFFHAND_SLOT)));
    }

    private void syncToTargetEnderChest(Inventory guiInv, Player target) {
        ItemStack[] newContents = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            newContents[i] = guiInv.getItem(i);
        }
        target.getEnderChest().setContents(newContents);
    }

    private ItemStack unwrapPlaceholder(ItemStack item) {
        if (item == null) return null;
        if (item.getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE
            && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return null; // c'était notre placeholder "vide", pas un vrai item
        }
        return item;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;
        if (!(event.getInventory().getHolder() instanceof ChillGuiHolder holder)) return;
        if (holder.getType() == ChillGuiHolder.Type.INSPECT_INVENTORY
            || holder.getType() == ChillGuiHolder.Type.INSPECT_ENDERCHEST) {
            stopRefresh(viewer);
        }
    }
}
