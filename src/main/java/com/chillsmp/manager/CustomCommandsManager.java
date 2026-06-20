package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import com.chillsmp.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class CustomCommandsManager {

    private final ChillSMPPlugin plugin;
    private final List<String> registeredCommands = new ArrayList<>();

    public CustomCommandsManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        plugin.loadCustomCommandsConfig();
        ConfigurationSection section = plugin.getCustomCommandsConfig().getConfigurationSection("commands");
        if (section == null) return;

        for (String cmdName : section.getKeys(false)) {
            registerCommand(cmdName);
        }
        plugin.getLogger().info("[CustomCmds] " + registeredCommands.size() + " commandes custom enregistrées.");
    }

    private void registerCommand(String cmdName) {
        String message = plugin.getCustomCommandsConfig().getString("commands." + cmdName + ".message", "");
        String hover = plugin.getCustomCommandsConfig().getString("commands." + cmdName + ".hover", null);
        String url = plugin.getCustomCommandsConfig().getString("commands." + cmdName + ".url", null);
        boolean opOnly = plugin.getCustomCommandsConfig().getBoolean("commands." + cmdName + ".op-only", false);

        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            CommandMap commandMap = (CommandMap) f.get(Bukkit.getServer());

            PluginCommand cmd = plugin.getCommand(cmdName) != null
                ? plugin.getCommand(cmdName)
                : createPluginCommand(cmdName);

            if (cmd == null) {
                // Créer une commande dynamique
                commandMap.register(cmdName, "chillsmp", new Command(cmdName) {
                    @Override
                    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                        if (opOnly && !sender.isOp()) {
                            sender.sendMessage(Msg.color(plugin.getConfig().getString("messages.no-permission",
                                "&cVous n'avez pas la permission.")));
                            return true;
                        }
                        if (sender instanceof Player player) {
                            sendCustomMessage(player, message, hover, url);
                        } else {
                            Bukkit.getConsoleSender().sendMessage(Msg.color(message));
                        }
                        return true;
                    }
                });
            }
            registeredCommands.add(cmdName);
        } catch (Exception e) {
            plugin.getLogger().warning("[CustomCmds] Impossible d'enregistrer /" + cmdName + ": " + e.getMessage());
        }
    }

    private void sendCustomMessage(Player player, String message, String hover, String url) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

        // Gérer les messages multi-lignes
        String[] lines = message.split("\n");
        for (String line : lines) {
            Component comp = serializer.deserialize(line);
            if (hover != null) {
                comp = comp.hoverEvent(HoverEvent.showText(serializer.deserialize(hover)));
            }
            if (url != null) {
                comp = comp.clickEvent(ClickEvent.openUrl(url));
            }
            player.sendMessage(comp);
        }
    }

    public void reload() {
        registeredCommands.clear();
        registerAll();
    }

    private PluginCommand createPluginCommand(String name) {
        try {
            java.lang.reflect.Constructor<PluginCommand> c =
                PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            c.setAccessible(true);
            return c.newInstance(name, plugin);
        } catch (Exception e) {
            return null;
        }
    }
}
