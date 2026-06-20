package com.chillsmp.hud;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class HudManager {

    private final ChillSMPPlugin plugin;
    private BukkitTask task;
    private final Set<UUID> hudDisabled = new HashSet<>();
    // Lignes custom par joueur
    private final Map<UUID, List<String>> customLines = new HashMap<>();

    public HudManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hudDisabled.contains(player.getUniqueId())) {
                    updateHud(player);
                }
            }
        }, 20L, 20L);
    }

    @SuppressWarnings("deprecation")
    public void updateHud(Player player) {
        if (!plugin.getConfig().getBoolean("hud.enabled", true)) return;

        ScoreboardManager sm = Bukkit.getScoreboardManager();
        Scoreboard board = sm.getNewScoreboard();

        String title = plugin.getConfig().getString("hud.title", "&b&l✦ Chill SMP");
        Objective obj = board.registerNewObjective("hud", Criteria.DUMMY,
            color(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Masquer les chiffres affichés à droite de chaque ligne (1.20.5+)
        try {
            obj.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        } catch (Throwable ignored) {
            // Si la version de Paper ne supporte pas cette API, on garde les chiffres par défaut
        }

        List<String> lines = customLines.getOrDefault(player.getUniqueId(),
            plugin.getConfig().getStringList("hud.default-lines"));

        String biome = "";
        try {
            biome = player.getLocation().getBlock().getBiome().name()
                .replace("_", " ").toLowerCase();
            biome = biome.substring(0, 1).toUpperCase() + biome.substring(1);
        } catch (Exception ignored) {}

        int score = lines.size() + 1;
        // Ligne vide en haut
        obj.getScore(" ").setScore(score--);

        for (String line : lines) {
            String processed = replacePlaceholders(line, player, biome);
            String coloredLine = color(processed);
            // Éviter les doublons
            while (board.getEntries().contains(coloredLine)) coloredLine += "§r";
            obj.getScore(coloredLine).setScore(score--);
        }

        // Ligne vide en bas
        obj.getScore("  ").setScore(score);

        player.setScoreboard(board);
    }

    private String replacePlaceholders(String line, Player player, String biome) {
        var stats = plugin.getStatsManager();
        return line
            .replace("{player}", player.getName())
            .replace("{playtime}", stats.getPlaytimeFormatted(player.getUniqueId()))
            .replace("{kills}", String.valueOf(stats.getKills(player.getUniqueId())))
            .replace("{deaths}", String.valueOf(stats.getDeaths(player.getUniqueId())))
            .replace("{kd}", stats.getKD(player.getUniqueId()))
            .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
            .replace("{ping}", String.valueOf(player.getPing()))
            .replace("{biome}", biome)
            .replace("{x}", String.valueOf((int) player.getLocation().getX()))
            .replace("{y}", String.valueOf((int) player.getLocation().getY()))
            .replace("{z}", String.valueOf((int) player.getLocation().getZ()))
            .replace("{coords}", (int) player.getLocation().getX() + " / "
                + (int) player.getLocation().getY() + " / "
                + (int) player.getLocation().getZ());
    }

    public void toggleHud(Player player) {
        UUID uuid = player.getUniqueId();
        if (hudDisabled.contains(uuid)) {
            hudDisabled.remove(uuid);
            updateHud(player);
        } else {
            hudDisabled.add(uuid);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public boolean isHudEnabled(Player player) {
        return !hudDisabled.contains(player.getUniqueId());
    }

    public void reload() {
        if (task != null) task.cancel();
        start();
    }

    public void cleanup() {
        if (task != null) task.cancel();
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
