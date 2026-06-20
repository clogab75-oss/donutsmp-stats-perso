package com.chillsmp.tab;

import com.chillsmp.ChillSMPPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TabManager {

    private final ChillSMPPlugin plugin;
    private BukkitTask task;
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public TabManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTab(player);
            }
        }, 20L, 40L);
    }

    public void updateTab(Player player) {
        String serverName = plugin.getConfig().getString("server-name", "Chill SMP");
        String header = plugin.getConfig().getString("tab.header",
            "&b&l✦ {server-name} &b&l✦\n&7Joueurs en ligne: &b{online}&7/&b{max}");
        String footer = plugin.getConfig().getString("tab.footer",
            "&7Discord: &bdiscord.gg/example");

        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        long ping = player.getPing();

        header = header
            .replace("{server-name}", serverName)
            .replace("{online}", String.valueOf(online))
            .replace("{max}", String.valueOf(max))
            .replace("{player}", player.getName())
            .replace("{ping}", String.valueOf(ping));

        footer = footer
            .replace("{server-name}", serverName)
            .replace("{online}", String.valueOf(online))
            .replace("{max}", String.valueOf(max))
            .replace("{player}", player.getName())
            .replace("{ping}", String.valueOf(ping));

        Component headerComp = SERIALIZER.deserialize(header);
        Component footerComp = SERIALIZER.deserialize(footer);
        player.sendPlayerListHeaderAndFooter(headerComp, footerComp);

        // Format du nom dans le TAB (utilise le pseudo custom s'il existe)
        String nameFormat = plugin.getConfig().getString("tab.name-format", "&7{player}");
        if (plugin.getNicknameManager().hasNickname(player.getUniqueId())) {
            nameFormat = nameFormat.replace("{player}", plugin.getNicknameManager().getNickname(player.getUniqueId()));
        } else {
            nameFormat = nameFormat.replace("{player}", player.getName());
        }
        if (player.isOp()) nameFormat = nameFormat.replace("{rank}", "&c[OP] ");
        else nameFormat = nameFormat.replace("{rank}", "");
        if (plugin.getVanishManager().isVanished(player)) {
            nameFormat = "&7[Vanish] " + nameFormat;
        }
        player.playerListName(SERIALIZER.deserialize(nameFormat));
    }

    public void reload() {
        if (task != null) task.cancel();
        start();
    }

    public void cleanup() {
        if (task != null) task.cancel();
    }
}
