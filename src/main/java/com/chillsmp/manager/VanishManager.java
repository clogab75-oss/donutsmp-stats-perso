package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final ChillSMPPlugin plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public void vanish(Player player) {
        vanished.add(player.getUniqueId());
        // Cacher aux non-ops
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.isOp() && !other.equals(player)) {
                other.hidePlayer(plugin, player);
            }
        }
        // Retirer du tab list
        player.setPlayerListName(null);
        // Faux message de déconnexion
        String msg = plugin.getConfig().getString("vanish.fake-quit-message",
            "&7{player} a quitté le serveur.");
        String formatted = msg.replace("{player}", player.getName());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', formatted));
            }
        }
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(plugin, player);
        }
        // Faux message de connexion
        String msg = plugin.getConfig().getString("vanish.fake-join-message",
            "&7{player} a rejoint le serveur.");
        String formatted = msg.replace("{player}", player.getName());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', formatted));
            }
        }
    }

    public void applyVanishOnJoin(Player newPlayer) {
        // Cacher les vanishés au nouveau joueur
        for (UUID uuid : vanished) {
            Player vanishedPlayer = Bukkit.getPlayer(uuid);
            if (vanishedPlayer != null && !newPlayer.isOp()) {
                newPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
}
