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

public class StatsManager {

    private final ChillSMPPlugin plugin;
    private FileConfiguration data;
    private File dataFile;
    private final Map<UUID, Long> sessionStart = new HashMap<>();

    public StatsManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        load();
        // Sauvegarde automatique toutes les 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAll, 6000L, 6000L);
    }

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!dataFile.exists()) plugin.saveResource("players.yml", false);
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void reload() { load(); }

    public void saveAll() {
        // Sauvegarder playtime en cours pour les joueurs connectés
        for (Player p : Bukkit.getOnlinePlayers()) {
            addPlaytime(p.getUniqueId(), getSessionTime(p.getUniqueId()));
            sessionStart.put(p.getUniqueId(), System.currentTimeMillis());
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void onJoin(Player player) {
        UUID uuid = player.getUniqueId();
        sessionStart.put(uuid, System.currentTimeMillis());
        // Init si nouveau joueur
        String path = "players." + uuid;
        if (!data.contains(path)) {
            data.set(path + ".name", player.getName());
            data.set(path + ".kills", 0);
            data.set(path + ".deaths", 0);
            data.set(path + ".playtime", 0L);
        } else {
            data.set(path + ".name", player.getName());
        }
    }

    public void onQuit(Player player) {
        UUID uuid = player.getUniqueId();
        addPlaytime(uuid, getSessionTime(uuid));
        sessionStart.remove(uuid);
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private long getSessionTime(UUID uuid) {
        Long start = sessionStart.get(uuid);
        if (start == null) return 0;
        return System.currentTimeMillis() - start;
    }

    private void addPlaytime(UUID uuid, long ms) {
        String path = "players." + uuid + ".playtime";
        long current = data.getLong(path, 0);
        data.set(path, current + ms);
    }

    public void addKill(UUID uuid) {
        String path = "players." + uuid + ".kills";
        data.set(path, data.getInt(path, 0) + 1);
    }

    public void addDeath(UUID uuid) {
        String path = "players." + uuid + ".deaths";
        data.set(path, data.getInt(path, 0) + 1);
    }

    public int getKills(UUID uuid) {
        return data.getInt("players." + uuid + ".kills", 0);
    }

    public int getDeaths(UUID uuid) {
        return data.getInt("players." + uuid + ".deaths", 0);
    }

    public String getKD(UUID uuid) {
        int kills = getKills(uuid);
        int deaths = getDeaths(uuid);
        if (deaths == 0) return String.valueOf(kills);
        return String.format("%.2f", (double) kills / deaths);
    }

    public long getPlaytimeMs(UUID uuid) {
        long saved = data.getLong("players." + uuid + ".playtime", 0);
        // Ajouter la session en cours si connecté
        if (sessionStart.containsKey(uuid)) {
            saved += getSessionTime(uuid);
        }
        return saved;
    }

    public String getPlaytimeFormatted(UUID uuid) {
        long ms = getPlaytimeMs(uuid);
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + "j " + (hours % 24) + "h " + (minutes % 60) + "m";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m " + (seconds % 60) + "s";
    }
}
