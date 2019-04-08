package me.jwhz.core.mines;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.jwhz.core.Core;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BlockBreakListener implements Listener {

    private int multiplier;

    @Getter
    private HashMap<Material, Material> oreSmelt = Maps.newHashMap();

    public HashMap<Material, Material> getSmeltableOres() {
        oreSmelt.put(Material.IRON_ORE, Material.IRON_INGOT);
        oreSmelt.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        return oreSmelt;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Core.getInstance().getConfig().getConfigurationSection("Mines").getKeys(false).contains(player.getWorld().getName())) {
            return;
        }
        if (OreRegenManager.hasBlock(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
        if (!event.getBlock().getType().name().contains("ORE"))
            return;
        if ((player.isOp() || player.hasPermission("eliteskyblock.mines.bypass")) && player.isSneaking())
            return;
        OreRegenManager.addBlockBreak(event.getBlock());
        event.setCancelled(true);
        try {
            multiplier = Core.getInstance().getConfig().getInt("Mines." + player.getWorld().getName() + ".multiplier");
        } catch (NullPointerException ex) {
            multiplier = 1;
        }
        if (getSmeltableOres().containsKey(event.getBlock().getType())) {
            player.getInventory().addItem(event.getBlock().getDrops(player.getItemInHand()).stream().map(itemStack -> {
                itemStack.setType(getSmeltableOres().get(itemStack.getType()));
                itemStack.setAmount(itemStack.getAmount() * multiplier);
                return itemStack;
            }).toArray(ItemStack[]::new))
                    .forEach((integer, itemStack) -> {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l[!] &c-" + itemStack.getAmount() + " " + StringUtils.capitalize(itemStack.getType()
                                .name()
                                .toLowerCase()
                                .replace("_", " ")) + " (Inventory Full)"));
                    });
        } else {
            player.getInventory().addItem(event.getBlock().getDrops(player.getItemInHand()).stream().map(itemStack -> {
                itemStack.setAmount(itemStack.getAmount() * multiplier);
                return itemStack;
            }).toArray(ItemStack[]::new))
                    .forEach((integer, itemStack) -> {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l[!] &c-" + itemStack.getAmount() + " " + StringUtils.capitalize(itemStack.getType()
                                .name()
                                .toLowerCase()
                                .replace("_", " ")) + " (Inventory Full)"));
                    });
        }
        event.getBlock().setType(Material.STONE);
    }
}
