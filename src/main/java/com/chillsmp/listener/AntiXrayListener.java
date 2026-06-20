package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class AntiXrayListener implements Listener {

    private final ChillSMPPlugin plugin;
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public AntiXrayListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!plugin.getXRayManager().isEnabled()) return;

        Player player = event.getPlayer();
        plugin.getXRayManager().recordBlock(player.getUniqueId(), event.getBlock().getType());

        if (plugin.getXRayManager().isSuspicious(player.getUniqueId())) {
            notifyOps(player);
        }
    }

    private void notifyOps(Player suspect) {
        double ratio = plugin.getXRayManager().getRatio(suspect.getUniqueId());
        String msg = plugin.getConfig().getString("messages.xray-alert",
            "&c⚠ &lALERTE XRAY &c⚠ &r&c{player} &7a un ratio suspect (&e{ratio}%&7)");
        msg = msg.replace("{player}", suspect.getName())
                 .replace("{ratio}", String.format("%.1f", ratio));

        Component alertComponent = SERIALIZER.deserialize(msg)
            .hoverEvent(HoverEvent.showText(
                SERIALIZER.deserialize("&7Cliquez pour voir les détails")))
            .clickEvent(ClickEvent.runCommand("/xray " + suspect.getName()));

        for (Player op : Bukkit.getOnlinePlayers()) {
            if (op.isOp()) op.sendMessage(alertComponent);
        }

        // Une seule alerte toutes les 60s par joueur (reset + log dans un fichier)
        plugin.getXRayManager().logAndResetOnAlert(suspect);
    }
}
