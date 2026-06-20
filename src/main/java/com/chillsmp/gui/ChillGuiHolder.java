package com.chillsmp.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Marqueur attaché à tous les inventaires custom du plugin.
 * Permet de les identifier de façon fiable (au lieu de comparer le titre,
 * ce qui était fragile à cause des codes couleur et causait le bug
 * permettant de récupérer les items affichés dans /xray).
 */
public class ChillGuiHolder implements InventoryHolder {

    public enum Type {
        XRAY_LIST,
        XRAY_DETAIL,
        STAFF_LIST,
        STAFF_DETAIL,
        STATS,
        INSPECT_INVENTORY,
        INSPECT_ENDERCHEST
    }

    private final Type type;
    private final UUID targetUUID;
    private Inventory inventory;

    public ChillGuiHolder(Type type) {
        this(type, null);
    }

    public ChillGuiHolder(Type type, UUID targetUUID) {
        this.type = type;
        this.targetUUID = targetUUID;
    }

    public Type getType() { return type; }
    public UUID getTargetUUID() { return targetUUID; }

    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    @Override
    public Inventory getInventory() { return inventory; }
}
