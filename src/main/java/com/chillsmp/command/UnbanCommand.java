package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import org.bukkit.*;
import org.bukkit.command.*;

import java.util.UUID;

public class UnbanCommand implements CommandExecutor {
    private final ChillSMPPlugin plugin;
    public UnbanCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        String prefix = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r"));
        if (args.length < 1) {
            sender.sendMessage(prefix + Msg.color("&7Usage: &b/unban <joueur>"));
            return true;
        }

        // On tente de retrouver l'UUID via un joueur offline connu de Bukkit
        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offline.getUniqueId();

        if (!plugin.getBanManager().isBanned(uuid)) {
            sender.sendMessage(prefix + Msg.color("&cCe joueur n'est pas banni (ou introuvable)."));
            return true;
        }

        plugin.getBanManager().unban(uuid);
        sender.sendMessage(prefix + Msg.color("&b" + args[0] + " &aa été débanni."));
        return true;
    }
}
