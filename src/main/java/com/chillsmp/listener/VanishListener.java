package com.chillsmp.listener;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {

    private final ChillSMPPlugin plugin;

    public VanishListener(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Si le joueur rejoint et est op, on le montre à tous (reset)
        // Les joueurs vanishés sont cachés dans applyVanishOnJoin
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Si le joueur quitte alors qu'il est vanish, on supprime le message de départ
        // (déjà envoyé un faux quit message lors du /vanish)
        if (plugin.getVanishManager().isVanished(player)) {
            event.quitMessage(null);
        }
    }
}
