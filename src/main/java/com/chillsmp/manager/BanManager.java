package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {

    private final ChillSMPPlugin plugin;
    private FileConfiguration data;
    private File dataFile;

    public BanManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        load();
        // Vérification périodique des bans expirés
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkExpired, 200L, 200L);
    }

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void save() {
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void tempBan(UUID uuid, String name, long durationMs, String reason, String bannedBy) {
        long expiry = System.currentTimeMillis() + durationMs;
        data.set("bans." + uuid + ".name", name);
        data.set("bans." + uuid + ".expiry", expiry);
        data.set("bans." + uuid + ".reason", reason);
        data.set("bans." + uuid + ".banned-by", bannedBy);
        save();

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            String kickMsg = color("&c&lVous avez été banni temporairement !\n\n")
                + color("&7Raison: &c" + reason + "\n")
                + color("&7Expire dans: &c" + formatDuration(durationMs) + "\n")
                + color("&7Banni par: &c" + bannedBy);
            player.kick(net.kyori.adventure.text.Component.text(kickMsg));
        }
    }

    public void unban(UUID uuid) {
        data.set("bans." + uuid, null);
        save();
    }

    public boolean isBanned(UUID uuid) {
        if (!data.contains("bans." + uuid)) return false;
        long expiry = data.getLong("bans." + uuid + ".expiry");
        if (System.currentTimeMillis() > expiry) {
            unban(uuid);
            return false;
        }
        return true;
    }

    public String getBanMessage(UUID uuid) {
        if (!isBanned(uuid)) return null;
        long expiry = data.getLong("bans." + uuid + ".expiry");
        String reason = data.getString("bans." + uuid + ".reason", "Aucune raison");
        String bannedBy = data.getString("bans." + uuid + ".banned-by", "Admin");
        long remaining = expiry - System.currentTimeMillis();
        return "&c&lVous êtes banni !\n\n"
            + "&7Raison: &c" + reason + "\n"
            + "&7Expire dans: &c" + formatDuration(remaining) + "\n"
            + "&7Banni par: &c" + bannedBy;
    }

    private void checkExpired() {
        if (!data.contains("bans")) return;
        for (String key : data.getConfigurationSection("bans").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                if (!isBanned(uuid)) unban(uuid);
            } catch (Exception ignored) {}
        }
    }

    public static long parseDuration(String input) {
        long total = 0;
        String num = "";
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                num += c;
            } else {
                if (num.isEmpty()) continue;
                long val = Long.parseLong(num);
                num = "";
                switch (Character.toLowerCase(c)) {
                    case 's' -> total += val * 1000L;
                    case 'm' -> total += val * 60000L;
                    case 'h' -> total += val * 3600000L;
                    case 'd' -> total += val * 86400000L;
                }
            }
        }
        return total;
    }

    public static String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + "j " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m " + (seconds % 60) + "s";
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}
