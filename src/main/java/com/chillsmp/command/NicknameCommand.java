package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class NicknameCommand implements CommandExecutor {

    private final ChillSMPPlugin plugin;

    public NicknameCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        String prefix = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r"));

        if (args.length < 1) {
            sender.sendMessage(prefix + Msg.color("&7Usage: &b/nickname <joueur> <nouveau pseudo>"));
            sender.sendMessage(prefix + Msg.color("&7Exemple: &b/nickname Steve &c❤ &fSteve"));
            sender.sendMessage(prefix + Msg.color("&7Pour retirer: &b/nickname <joueur> reset"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(prefix + Msg.color(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(prefix + Msg.color("&7Usage: &b/nickname <joueur> <nouveau pseudo>"));
            sender.sendMessage(prefix + Msg.color("&7Pour retirer: &b/nickname <joueur> reset"));
            return true;
        }

        if (args[1].equalsIgnoreCase("reset")) {
            plugin.getNicknameManager().clearNickname(target.getUniqueId());
            sender.sendMessage(prefix + Msg.color("&aPseudo custom de &b" + target.getName() + " &aretiré."));
            plugin.getTabManager().updateTab(target);
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        String nickname = sb.toString().trim();

        plugin.getNicknameManager().setNickname(target.getUniqueId(), nickname);
        sender.sendMessage(prefix + Msg.color("&aPseudo de &b" + target.getName() + " &amis à jour: " + Msg.color(nickname)));
        plugin.getTabManager().updateTab(target);
        return true;
    }
}
