package me.jwhz.core.events;

import me.jwhz.core.Core;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BlockGeneration implements Listener {
    @EventHandler
    public void onFromTo(BlockFromToEvent event) {
        Material type = event.getBlock().getType();
        Block b;
        int amount;
        if ((type == Material.WATER) || (type == Material.STATIONARY_WATER) || (type == Material.LAVA) || (type == Material.STATIONARY_LAVA)) {
            b = event.getToBlock();
            if ((b.getType() == Material.AIR) &&
                    (generatesCobble(type, b))) {
                Random random = ThreadLocalRandom.current();
                amount = random.nextInt(100) + 1;
                for (String s : Core.getInstance().getConfig().getStringList("Ore.generation")) {
                    String[] index = s.split(":");
                    if (amount <= Integer.valueOf(index[1]).intValue()) {
                        b.setType(Material.getMaterial(index[0]));
                    }
                }
            }
        }
    }

    private final BlockFace[] faces = {BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public boolean generatesCobble(Material type, Block b) {
        Material mirrorID1 = (type == Material.WATER) || (type == Material.STATIONARY_WATER) ? Material.LAVA : Material.WATER;
        Material mirrorID2 = (type == Material.WATER) || (type == Material.STATIONARY_WATER) ? Material.STATIONARY_LAVA : Material.STATIONARY_WATER;
        for (BlockFace face : this.faces) {
            Block r = b.getRelative(face, 1);
            if ((r.getType() == mirrorID1) || (r.getType() == mirrorID2)) {
                return true;
            }
        }
        return false;
    }
}
