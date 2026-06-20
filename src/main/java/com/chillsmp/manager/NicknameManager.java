package com.chillsmp.manager;

import com.chillsmp.ChillSMPPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NicknameManager {

    private final ChillSMPPlugin plugin;
    private FileConfiguration data;
    private File dataFile;
    private final Map<UUID, String> cache = new HashMap<>();

    public NicknameManager(ChillSMPPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "nicknames.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        cache.clear();
        if (data.contains("nicknames")) {
            for (String key : data.getConfigurationSection("nicknames").getKeys(false)) {
                try {
                    cache.put(UUID.fromString(key), data.getString("nicknames." + key));
                } catch (Exception ignored) {}
            }
        }
    }

    public void reload() { load(); }

    private void save() {
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void setNickname(UUID uuid, String nickname) {
        cache.put(uuid, nickname);
        data.set("nicknames." + uuid, nickname);
        save();
    }

    public void clearNickname(UUID uuid) {
        cache.remove(uuid);
        data.set("nicknames." + uuid, null);
        save();
    }

    public boolean hasNickname(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public String getNickname(UUID uuid) {
        return cache.get(uuid);
    }
}
