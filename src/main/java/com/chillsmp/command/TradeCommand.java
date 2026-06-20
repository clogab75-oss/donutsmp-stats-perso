package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {

    private final ChillSMPPlugin plugin;
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public TradeCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        String prefix = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r"));

        // Accepter une demande en attente si pas d'argument
        if (args.length == 0) {
            if (plugin.getTradeManager().hasPendingRequest(player)) {
                plugin.getTradeManager().acceptRequest(player);
                return true;
            }
            player.sendMessage(prefix + Msg.color("&7Usage: &b/trade <joueur>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(prefix + Msg.color(plugin.getConfig().getString("messages.player-not-found")));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(prefix + Msg.color("&cVous ne pouvez pas trader avec vous-même."));
            return true;
        }

        if (plugin.getTradeManager().isInTrade(player)) {
            player.sendMessage(prefix + Msg.color("&cVous êtes déjà dans un trade."));
            return true;
        }

        // Envoyer la demande
        plugin.getTradeManager().sendRequest(player, target);

        // Message à l'envoyeur
        String sentMsg = plugin.getConfig().getString("messages.trade-request-sent",
            "&aProposition de trade envoyée à &b{player}&a.");
        player.sendMessage(prefix + Msg.color(sentMsg.replace("{player}", target.getName())));

        // Message cliquable au destinataire
        String recvMsg = plugin.getConfig().getString("messages.trade-request-received",
            "&b{player} &avous propose un trade.");
        recvMsg = recvMsg.replace("{player}", player.getName());

        Component msg = SERIALIZER.deserialize(recvMsg + " ")
            .append(SERIALIZER.deserialize("&e[Cliquer pour accepter]")
                .hoverEvent(HoverEvent.showText(SERIALIZER.deserialize("&7Cliquez pour accepter le trade")))
                .clickEvent(ClickEvent.runCommand("/trade")));

        target.sendMessage(msg);
        return true;
    }
}
