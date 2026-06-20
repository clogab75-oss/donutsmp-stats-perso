package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatsListener implements Listener {

    private final ChillSMPPlugin plugin;

    public StatsListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().onJoin(player);
        plugin.getVanishManager().applyVanishOnJoin(player);
        plugin.getTabManager().updateTab(player);

        // Vérifier ban
        if (plugin.getBanManager().isBanned(player.getUniqueId())) {
            String msg = plugin.getBanManager().getBanMessage(player.getUniqueId());
            player.kick(net.kyori.adventure.text.Component.text(
                org.bukkit.ChatColor.translateAlternateColorCodes('&', msg)));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().onQuit(player);
        plugin.getTradeManager().cancelTrade(player);
        plugin.getXRayManager().logAndResetOnDisconnect(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!(event.getEntity() instanceof Player dead)) return;
        plugin.getStatsManager().addDeath(dead.getUniqueId());

        if (dead.getKiller() instanceof Player killer) {
            plugin.getStatsManager().addKill(killer.getUniqueId());
        }
    }
}
