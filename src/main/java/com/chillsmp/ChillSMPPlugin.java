package com.chillsmp;

import com.chillsmp.command.*;
import com.chillsmp.gui.InspectManager;
import com.chillsmp.listener.*;
import com.chillsmp.manager.*;
import com.chillsmp.tab.TabManager;
import com.chillsmp.hud.HudManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ChillSMPPlugin extends JavaPlugin {

    private static ChillSMPPlugin instance;

    private StatsManager statsManager;
    private VanishManager vanishManager;
    private XRayManager xrayManager;
    private TradeManager tradeManager;
    private TabManager tabManager;
    private HudManager hudManager;
    private HopperManager hopperManager;
    private StackManager stackManager;
    private BanManager banManager;
    private CustomCommandsManager customCommandsManager;
    private InspectManager inspectManager;
    private NicknameManager nicknameManager;

    private FileConfiguration customCommandsConfig;
    private File customCommandsFile;

    @Override
    public void onEnable() {
        instance = this;

        // Sauvegarder les configs par défaut
        saveDefaultConfig();
        saveResource("custom-commands.yml", false);
        saveResource("players.yml", false);

        // Initialiser les managers
        statsManager = new StatsManager(this);
        vanishManager = new VanishManager(this);
        xrayManager = new XRayManager(this);
        tradeManager = new TradeManager(this);
        tabManager = new TabManager(this);
        hudManager = new HudManager(this);
        hopperManager = new HopperManager(this);
        stackManager = new StackManager(this);
        banManager = new BanManager(this);
        customCommandsManager = new CustomCommandsManager(this);
        inspectManager = new InspectManager(this);
        nicknameManager = new NicknameManager(this);

        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new HeadDropListener(this), this);
        getServer().getPluginManager().registerEvents(new VeinMinerListener(this), this);
        getServer().getPluginManager().registerEvents(new AntiXrayListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new TradeListener(this), this);
        getServer().getPluginManager().registerEvents(new StackListener(this), this);
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);
        getServer().getPluginManager().registerEvents(new StatsListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiGuardListener(this), this);

        // Enregistrer les commandes standards
        getCommand("xray").setExecutor(new XRayCommand(this));
        getCommand("staff").setExecutor(new StaffCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("hud").setExecutor(new HudCommand(this));
        getCommand("interface").setExecutor(new HudCommand(this));
        getCommand("smpreload").setExecutor(new ReloadCommand(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this));
        getCommand("ban").setExecutor(new TempBanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("nickname").setExecutor(new NicknameCommand(this));

        // Enregistrer les commandes custom
        customCommandsManager.registerAll();

        getLogger().info("╔══════════════════════════════╗");
        getLogger().info("║   ChillSMP Plugin  v1.0.0    ║");
        getLogger().info("║   Activé avec succès !       ║");
        getLogger().info("╚══════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) statsManager.saveAll();
        if (tabManager != null) tabManager.cleanup();
        if (hudManager != null) hudManager.cleanup();
        getLogger().info("ChillSMP désactivé. À bientôt !");
    }

    public void reloadAll() {
        reloadConfig();
        loadCustomCommandsConfig();
        statsManager.reload();
        xrayManager.reload();
        hopperManager.reload();
        stackManager.reload();
        tabManager.reload();
        hudManager.reload();
        customCommandsManager.reload();
        nicknameManager.reload();
    }

    public FileConfiguration getCustomCommandsConfig() {
        if (customCommandsConfig == null) loadCustomCommandsConfig();
        return customCommandsConfig;
    }

    public void loadCustomCommandsConfig() {
        customCommandsFile = new File(getDataFolder(), "custom-commands.yml");
        if (!customCommandsFile.exists()) saveResource("custom-commands.yml", false);
        customCommandsConfig = YamlConfiguration.loadConfiguration(customCommandsFile);
    }

    public static ChillSMPPlugin getInstance() { return instance; }
    public StatsManager getStatsManager() { return statsManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public XRayManager getXRayManager() { return xrayManager; }
    public TradeManager getTradeManager() { return tradeManager; }
    public TabManager getTabManager() { return tabManager; }
    public HudManager getHudManager() { return hudManager; }
    public HopperManager getHopperManager() { return hopperManager; }
    public StackManager getStackManager() { return stackManager; }
    public BanManager getBanManager() { return banManager; }
    public CustomCommandsManager getCustomCommandsManager() { return customCommandsManager; }
    public InspectManager getInspectManager() { return inspectManager; }
    public NicknameManager getNicknameManager() { return nicknameManager; }
}
