package me.jwhz.core.skyblock.leveling;

import me.jwhz.core.Core;
import me.jwhz.core.config.ConfigFile;
import me.jwhz.core.skyblock.islands.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class LevelSystem extends ConfigFile {

    public HashMap<Island, Long> cooldowns = new HashMap<>();
    public HashMap<Island, Integer> levels = new HashMap<>();
    public ArrayList<TopIsland> topIslands = new ArrayList<>();

    public LevelSystem() {

        super("leveling");

        for (Island island : Core.getInstance().skyblockManager.getList())
            if (getYamlConfiguration().contains(island.getIdentifier()))
                levels.put(island, getYamlConfiguration().getInt(island.getIdentifier()));
            else
                levels.put(island, 0);

        reloadTopTen();

    }

    public boolean contains(Island island) {

        for (TopIsland topIsland : topIslands)
            if (topIsland.island.equals(island))
                return true;
        return false;

    }

    public int getSoftLevel(Island island) {

        for (TopIsland topIsland : topIslands)
            if (topIsland.island.equals(island))
                return topIslands.indexOf(topIsland) + 1;

        return -1;

    }

    public void removeTopIsland(Island island) {

        TopIsland toRemove = null;

        for (TopIsland topIsland : topIslands)
            if (topIsland.island.equals(island))
                toRemove = topIsland;

        if (toRemove != null)
            topIslands.remove(toRemove);

    }

    public void reloadTopTen() {

        topIslands.clear();

        for (Map.Entry<Island, Integer> entries : levels.entrySet())
            topIslands.add(new TopIsland(entries.getKey(), entries.getValue()));

        topIslands.sort((o1, o2) -> o2.level - o1.level);

    }

    public int getLevel(Island island) {
        int level = 0;

        if (hasCooldown(island)) {
            return levels.get(island);
        }

        for (int x = island.min.x; x <= island.max.x; x++)
            for (int z = island.min.y; z <= island.max.y; z++)
                for (int y = 0; y < 256; y++)
                    if (island.spawn.getWorld().getBlockAt(x, y, z) != null || island.spawn.getWorld().getBlockAt(x, y, z).getType() != Material.AIR)
                        level += getWorth(island.spawn.getWorld().getBlockAt(x, y, z));

        cooldowns.put(island, System.currentTimeMillis() + 1200000);
        levels.put(island, level);

        getYamlConfiguration().set(island.getIdentifier(), level);
        try {
            getYamlConfiguration().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return level;

    }

    public boolean hasCooldown(Island island) {

        return cooldowns.containsKey(island) && cooldowns.get(island) > System.currentTimeMillis();

    }

    public long getLeftover(Island island) {

        return cooldowns.get(island) - System.currentTimeMillis();

    }

    public class TopIsland {

        public Island island;
        public int level;

        public TopIsland(Island island, int level) {

            this.island = island;
            this.level = level;

        }

    }

    public long getWorth(Block block) {
        AtomicLong worth = new AtomicLong();
        for (String s : Core.getInstance().worthValues.keySet()) {
            if (block.getType() == Material.getMaterial(s)) {
                worth.set(Core.getInstance().worthValues.get(s));
            }
        }
        return worth.get();
    }

}
