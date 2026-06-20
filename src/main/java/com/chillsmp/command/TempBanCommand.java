package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.manager.BanManager;
import com.chillsmp.util.Msg;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TempBanCommand implements CommandExecutor {
    private final ChillSMPPlugin plugin;
    public TempBanCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        String prefix = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r"));
        if (args.length < 2) {
            sender.sendMessage(prefix + Msg.color("&7Usage: &b/tempban <joueur> <durée> [raison]"));
            sender.sendMessage(prefix + Msg.color("&7Exemple: &b/tempban Steve 7d Triche"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        String name = args[0];
        java.util.UUID uuid = target != null ? target.getUniqueId() : null;
        if (uuid == null) {
            sender.sendMessage(prefix + Msg.color(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }

        long duration = BanManager.parseDuration(args[1]);
        if (duration <= 0) {
            sender.sendMessage(prefix + Msg.color("&cDurée invalide. Exemples: &e30m, 2h, 7d"));
            return true;
        }

        String reason = "Aucune raison fournie";
        if (args.length >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) sb.append(args[i]).append(" ");
            reason = sb.toString().trim();
        }

        String bannedBy = sender instanceof Player p ? p.getName() : "Console";
        plugin.getBanManager().tempBan(uuid, name, duration, reason, bannedBy);

        sender.sendMessage(prefix + Msg.color("&b" + name + " &abanni pour &e"
            + BanManager.formatDuration(duration) + "&a. Raison: &7" + reason));

        // Annoncer aux ops
        for (Player op : Bukkit.getOnlinePlayers()) {
            if (op.isOp()) op.sendMessage(prefix + Msg.color("&c" + name
                + " &7a été banni pour &e" + BanManager.formatDuration(duration)
                + "&7 par &c" + bannedBy + "&7. Raison: " + reason));
        }
        return true;
    }
}
