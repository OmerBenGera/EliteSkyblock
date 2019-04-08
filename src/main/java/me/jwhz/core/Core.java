package me.jwhz.core;

import com.google.common.collect.Maps;
import me.jwhz.core.command.CommandManager;
import me.jwhz.core.listener.EventManager;
import me.jwhz.core.mines.BlockBreakListener;
import me.jwhz.core.mines.OreRegenManager;
import me.jwhz.core.scoreboard.Scoreboard;
import me.jwhz.core.scoreboard.ScoreboardManager;
import me.jwhz.core.skyblock.SkyblockManager;
import me.jwhz.core.skyblock.islands.Island;
import me.jwhz.core.skyblock.leveling.LevelSystem;
import me.jwhz.core.skyblock.schematics.SchematicsManager;
import me.jwhz.core.tpqueue.TPQueue;
import me.jwhz.core.utils.FileManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class Core extends JavaPlugin implements Listener {

    private static Core instance;
    public static Economy economy;
    public static Chat chat;

    public EventManager eventManager;
    public CommandManager commandManager;
    public SkyblockManager skyblockManager;
    public SchematicsManager schematicsManager;
    public LevelSystem levelSystem;
    public TPQueue tpQueue;
    public ScoreboardManager scoreboardManager;
    public FileManager worth;

    public static HashMap<String, Long> worthValues = Maps.newHashMap();

    public boolean usingPlaceholderAPI;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        if (!setupEconomy()) {
            System.out.println("No proper economy plugin setup! Disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        worth = new FileManager(this, "worth", getDataFolder().getAbsolutePath());

        OreRegenManager.init(this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        usingPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        ConfigurationSerialization.registerClass(Island.class, "Island");

        tpQueue = new TPQueue();

        eventManager = new EventManager();

        commandManager = new CommandManager();

        skyblockManager = new SkyblockManager();

        schematicsManager = new SchematicsManager();

        levelSystem = new LevelSystem();

        scoreboardManager = new ScoreboardManager();

        getServer().getPluginManager().registerEvents(this, this);

        fetchValues();

        if (scoreboardManager.getYamlConfiguration().getBoolean("scoreboard-enabled"))
            for (Player player : Bukkit.getOnlinePlayers())
                scoreboardManager.getList().add(new Scoreboard(player));

        new BukkitRunnable() {
            @Override
            public void run() {
                levelSystem.reloadTopTen();
            }
        }.runTaskTimer(this, 1L, 20 * 60);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Scoreboard scoreboard : scoreboardManager.getList())
                    scoreboard.update();
            }
        }.runTaskTimer(this, 20L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers())
                    if (player.getWorld().getName().equalsIgnoreCase("skyblock"))
                        skyblockManager.sendWorldBorder(player);
            }
        }.runTaskTimer(this, 0, 20);

    }

    public static Core getInstance() {

        return instance;

    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null)
            return false;
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void fetchValues(){
        worthValues.clear();
        worth.getConfiguration().getConfigurationSection("Worth").getKeys(false).forEach(key -> worthValues.put(key, worth.getConfiguration().getLong("Worth." + key)));
    }

}
