package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final ChillSMPPlugin plugin;

    public VanishCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.isOp()) {
            player.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        String prefix = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r"));
        if (plugin.getVanishManager().isVanished(player)) {
            plugin.getVanishManager().unvanish(player);
            player.sendMessage(prefix + Msg.color(plugin.getConfig().getString("messages.vanish-off", "&cVous êtes visible.")));
        } else {
            plugin.getVanishManager().vanish(player);
            player.sendMessage(prefix + Msg.color(plugin.getConfig().getString("messages.vanish-on", "&aVous êtes invisible.")));
        }
        return true;
    }
}
