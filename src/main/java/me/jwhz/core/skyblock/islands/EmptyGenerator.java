package me.jwhz.core.skyblock.islands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class EmptyGenerator extends ChunkGenerator {
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return new ArrayList<>();
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;

    }

    @Override
    public byte[] generate(World world, Random random, int cx, int cz) {
        int height = world.getMaxHeight();
        byte[] result = new byte[256 * height];
        return result;
    }
}