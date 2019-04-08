package me.jwhz.core.mines;


import me.jwhz.core.Core;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class OreRegenManager {
    private static OreRegenManager oreRegenManager;
    private static File dataFile;
    private static YamlConfiguration yamlConfiguration;
    private static Plugin plugin;

    public static void init(Plugin plugin){
        OreRegenManager.plugin = plugin;
        OreRegenManager.oreRegenManager = new OreRegenManager();
        OreRegenManager.dataFile = new File(plugin.getDataFolder(), "regenBlocks.yml");
        plugin.getDataFolder().mkdirs();
        OreRegenManager.yamlConfiguration = YamlConfiguration.loadConfiguration(dataFile);

        for (String location : yamlConfiguration.getKeys(false)) {
            Material type = Material.valueOf(yamlConfiguration.getString(location));
            String[] locPoints = location.split(";");
            World world = Bukkit.getWorld(UUID.fromString(locPoints[0]));
            if (world == null)
                continue;
            Location location1 = new Location(world, Integer.valueOf(locPoints[1]), Integer.valueOf(locPoints[2]), Integer.valueOf(locPoints[3]));
            Chunk chunk = world.getChunkAt(location1);
            chunk.load();
            world.getBlockAt(location1).setType(type);
            chunk.unload();
            yamlConfiguration.set(location, null);
        }
        save();

        Bukkit.getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
    }


    /**
     * Is this block still being regenned?
     * @param block The block to check.
     * @return Whether or not the block has been mined and made to stone.
     */
    public static boolean hasBlock(Block block){
        if (!isEnabled())
            return false;
        return yamlConfiguration.contains(serialize(block.getLocation()));
    }

    /**
     * Will throw if you haven't called init yet.
     * @return Will throw an exception or return true.
     */
    public static boolean isEnabled(){
        if (oreRegenManager == null)
            throw new IllegalStateException("You need to call the init function first!");
        return true;
    }

    private static String serialize(Location location){
        return String.join(";", location.getWorld().getUID().toString(), String.valueOf(location.getBlockX()), String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ()));
    }

    /**
     * Add the block break, in case server crashed we want to make sure it gets reverted on restart.
     * @param block The block that was broken.
     */
    public static void addBlockBreak(Block block){
        if (!isEnabled())
            return;
        int time = Core.getInstance().getConfig().getInt("Mines-Configuration.delay");
        if (time == 0) time = 5;
        final Material material = block.getType();
        final String location = serialize(block.getLocation());
        yamlConfiguration.set(location, material.name());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->{
            block.setType(material);
            yamlConfiguration.set(location, null);
            save();
        }, time * 20);
        save();
    }


    /**
     * Get the master plugin, that this manager belongs to (slave)
     * @return master plugin.
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * Save our configuration file.
     */
    private static void save(){
        try {
            yamlConfiguration.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OreRegenManager() {}
}
