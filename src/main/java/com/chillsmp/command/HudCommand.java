package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HudCommand implements CommandExecutor {
    private final ChillSMPPlugin plugin;
    public HudCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        String prefix = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r"));
        plugin.getHudManager().toggleHud(player);
        if (plugin.getHudManager().isHudEnabled(player)) {
            player.sendMessage(prefix + Msg.color("&aHUD activé."));
        } else {
            player.sendMessage(prefix + Msg.color("&cHUD désactivé."));
        }
        return true;
    }
}
