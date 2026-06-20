package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XRayManager {

    private final ChillSMPPlugin plugin;
    private Set<Material> trackedOres;
    private double alertThreshold;
    private int minBlocks;
    private long inactivityResetMs;

    // UUID -> Map<Material, count>
    private final Map<UUID, Map<Material, Integer>> oreData = new HashMap<>();
    // UUID -> total stone/dirt/etc mined
    private final Map<UUID, Integer> stoneData = new HashMap<>();
    // UUID -> timestamp du dernier bloc miné (pour détecter l'inactivité)
    private final Map<UUID, Long> lastMineTime = new HashMap<>();
    // UUID -> a déjà été signalé pour la session de minage en cours (évite le spam d'alertes)
    private final Set<UUID> alreadyAlerted = new HashSet<>();

    private File logsDir;

    public XRayManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        reload();

        logsDir = new File(plugin.getDataFolder(), "xray-logs");
        if (!logsDir.exists()) logsDir.mkdirs();

        // Vérifie toutes les minutes si un joueur est inactif depuis trop longtemps
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkInactivity, 1200L, 1200L);
    }

    public void reload() {
        FileConfiguration cfg = plugin.getConfig();
        alertThreshold = cfg.getDouble("anti-xray.alert-threshold", 0.15);
        minBlocks = cfg.getInt("anti-xray.min-blocks-mined", 50);
        int inactivityMinutes = cfg.getInt("anti-xray.inactivity-reset-minutes", 10);
        inactivityResetMs = inactivityMinutes * 60_000L;
        trackedOres = new HashSet<>();
        List<String> oreList = cfg.getStringList("anti-xray.tracked-ores");
        for (String s : oreList) {
            try { trackedOres.add(Material.valueOf(s)); } catch (Exception ignored) {}
        }
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("anti-xray.enabled", true);
    }

    public void recordBlock(UUID uuid, Material material) {
        if (!isEnabled()) return;
        lastMineTime.put(uuid, System.currentTimeMillis());
        if (trackedOres.contains(material)) {
            oreData.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(material, 1, Integer::sum);
        } else if (isStone(material)) {
            stoneData.merge(uuid, 1, Integer::sum);
        }
    }

    private boolean isStone(Material mat) {
        return mat == Material.STONE || mat == Material.DEEPSLATE
            || mat == Material.COBBLESTONE || mat == Material.DIORITE
            || mat == Material.ANDESITE || mat == Material.GRANITE
            || mat == Material.TUFF || mat == Material.NETHERRACK
            || mat == Material.DIRT || mat == Material.GRAVEL
            || mat == Material.SAND;
    }

    public boolean isSuspicious(UUID uuid) {
        int stone = stoneData.getOrDefault(uuid, 0);
        int ores = getTotalOres(uuid);
        if (stone + ores < minBlocks) return false;
        if (stone == 0) return ores > 5;
        double ratio = (double) ores / (stone + ores);
        return ratio >= alertThreshold;
    }

    public double getRatio(UUID uuid) {
        int stone = stoneData.getOrDefault(uuid, 0);
        int ores = getTotalOres(uuid);
        if (stone + ores == 0) return 0;
        return (double) ores / (stone + ores) * 100;
    }

    public int getTotalOres(UUID uuid) {
        Map<Material, Integer> map = oreData.get(uuid);
        if (map == null) return 0;
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<Material, Integer> getOreBreakdown(UUID uuid) {
        return oreData.getOrDefault(uuid, new HashMap<>());
    }

    public int getStoneCount(UUID uuid) {
        return stoneData.getOrDefault(uuid, 0);
    }

    public boolean hasData(UUID uuid) {
        return getTotalOres(uuid) > 0 || getStoneCount(uuid) > 0;
    }

    public void reset(UUID uuid) {
        oreData.remove(uuid);
        stoneData.remove(uuid);
        lastMineTime.remove(uuid);
        alreadyAlerted.remove(uuid);
    }

    /**
     * Appelé quand une alerte xray se déclenche : écrit un fichier de log, prévient
     * (déjà fait ailleurs) puis reset les compteurs pour repartir sur une base neuve.
     */
    public void logAndResetOnAlert(Player player) {
        writeLogFile(player, "ALERTE_XRAY");
        reset(player.getUniqueId());
    }

    /**
     * Appelé à la déconnexion d'un joueur : log (si données présentes) + reset,
     * pour ne pas garder le même historique d'une session à l'autre.
     */
    public void logAndResetOnDisconnect(Player player) {
        UUID uuid = player.getUniqueId();
        if (hasData(uuid)) {
            writeLogFile(player, "DECONNEXION");
        }
        reset(uuid);
    }

    private void checkInactivity() {
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Long last = lastMineTime.get(uuid);
            if (last == null) continue;
            if (!hasData(uuid)) continue;
            if (now - last >= inactivityResetMs) {
                writeLogFile(player, "INACTIVITE_10MIN");
                reset(uuid);
            }
        }
    }

    private void writeLogFile(Player player, String reason) {
        try {
            UUID uuid = player.getUniqueId();
            double ratio = getRatio(uuid);
            int totalOres = getTotalOres(uuid);
            int totalStone = getStoneCount(uuid);
            Map<Material, Integer> breakdown = getOreBreakdown(uuid);

            SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            SimpleDateFormat readableDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String timestamp = fileDateFormat.format(new Date());

            String safeName = player.getName().replaceAll("[^a-zA-Z0-9_]", "_");
            File logFile = new File(logsDir, safeName + "_" + timestamp + ".yml");

            YamlConfiguration log = new YamlConfiguration();
            log.set("player", player.getName());
            log.set("uuid", uuid.toString());
            log.set("date", readableDateFormat.format(new Date()));
            log.set("raison", reason);
            log.set("ratio-pourcent", Math.round(ratio * 100.0) / 100.0);
            log.set("total-minerais", totalOres);
            log.set("total-roche", totalStone);

            for (Map.Entry<Material, Integer> entry : breakdown.entrySet()) {
                log.set("minerais." + entry.getKey().name(), entry.getValue());
            }

            log.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[XRay] Impossible d'écrire le log: " + e.getMessage());
        }
    }
}
