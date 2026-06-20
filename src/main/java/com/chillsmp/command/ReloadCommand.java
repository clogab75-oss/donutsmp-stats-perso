package com.chillsmp.command;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final ChillSMPPlugin plugin;
    public ReloadCommand(ChillSMPPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        plugin.reloadAll();
        String msg = Msg.color(plugin.getConfig().getString("messages.prefix", "&8[&bChillSMP&8] &r")
            + plugin.getConfig().getString("messages.reload-success", "&aPlugin rechargé !"));
        sender.sendMessage(msg);
        return true;
    }
}
