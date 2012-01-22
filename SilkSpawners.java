package me.exphc.SilkSpawners;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Formatter;
import java.lang.Byte;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.Material.*;
import org.bukkit.material.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.inventory.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.scheduler.*;
import org.bukkit.enchantments.*;
import org.bukkit.*;

import org.bukkit.craftbukkit.block.CraftCreatureSpawner;

class SilkSpawnersBlockListener implements Listener {
    static Logger log = Logger.getLogger("Minecraft");

    SilkSpawners plugin;

    public SilkSpawnersBlockListener(SilkSpawners pl) {
        plugin = pl;
        
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(final BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);

        plugin.informPlayer(player, "Broke "+spawner.getCreatureType().getName()+" spawner");

        // If using silk touch, drop spawner itself 
        ItemStack tool = player.getItemInHand();
        boolean silkTouch = tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH);

        ItemStack eggItem = plugin.creature2Egg.get(spawner.getCreatureType());
        ItemStack dropItem;

        if (silkTouch && player.hasPermission("silkspawners.silkdrop")) {
            // Drop spawner
            short entityID = eggItem.getDurability();

            dropItem = plugin.newSpawnerItem(entityID);
        } else if (player.hasPermission("silkspawners.eggdrop")) {
            // Drop egg
            dropItem = eggItem;
        } else {
            // No permission to drop anything
            return;
        }

        World world = player.getWorld();
        world.dropItemNaturally(block.getLocation(), dropItem);

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(final BlockPlaceEvent event) {
        Block blockPlaced = event.getBlockPlaced();

        if (blockPlaced.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();

        // BUG: event.getItemInHand() loses enchantments! (tested on craftbukkit-1.1-R1-20120121.235721-81.jar) Cannot use it
        //ItemStack item = event.getItemInHand();
        ItemStack item = player.getItemInHand();

        // Get data from item
        short entityID = plugin.getStoredSpawnerItemEntityID(item);
        if (entityID == 0) {
            plugin.informPlayer(player, "Placed default spawner");
            return;
        }

        CreatureType creature = plugin.eid2Creature.get(entityID);
        if (creature == null) {
            plugin.informPlayer(player, "No creature associated with spawner");
            return;
        }
        plugin.informPlayer(player, "Placed "+creature.getName()+" spawner");

        CraftCreatureSpawner spawner = new CraftCreatureSpawner(blockPlaced);
        if (spawner == null) {
            plugin.informPlayer(player, "Failed to find placed spawner, creature not set");
            return;
        }
        spawner.setCreatureType(creature); 
    }

}

public class SilkSpawners extends JavaPlugin {
    static Logger log = Logger.getLogger("Minecraft");
    SilkSpawnersBlockListener blockListener;

    ConcurrentHashMap<CreatureType,ItemStack> creature2Egg;
    ConcurrentHashMap<Short,CreatureType> eid2Creature;

    public void onEnable() {
        loadConfig();
        loadRecipes();

        // Listeners
        blockListener = new SilkSpawnersBlockListener(this);

        log.info("SilkSpawners enabled");
    }

    private void loadConfig() {
        // Load creature to egg map
        creature2Egg = new ConcurrentHashMap<CreatureType,ItemStack>();
        eid2Creature = new ConcurrentHashMap<Short,CreatureType>();

        MemorySection eggSection = (MemorySection)getConfig().get("eggs");

    
        for (String creatureString: eggSection.getKeys(false)) {
            CreatureType creatureType = CreatureType.fromName(creatureString);
            if (creatureType == null) {
                log.info("Invalid creature type: " + creatureString);
                continue;
            }

            // TODO: http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs in Bukkit?
            short entityID = (short)getConfig().getInt("eggs."+creatureString+".entityID");

            ItemStack eggItem = new ItemStack(Material.MONSTER_EGG, 1, entityID);

            creature2Egg.put(creatureType, eggItem);
            eid2Creature.put(new Short(entityID), creatureType);
        }
    }

    private void loadRecipes() {
        for (ItemStack egg: creature2Egg.values()) {
            short entityID = egg.getDurability();

            ItemStack spawnerItem = newSpawnerItem(entityID);

            ShapelessRecipe recipe = new ShapelessRecipe(spawnerItem);

            // TODO: ShapedRecipe, box
            recipe.addIngredient(8, Material.IRON_FENCE);
            recipe.addIngredient(Material.MONSTER_EGG, (int)entityID);

            Bukkit.getServer().addRecipe(recipe);
        }
    }

    public void onDisable() {
        log.info("SilkSpawners disabled");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("spawner")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            // Would like to handle the non-player (console command) case, but, I use the block the
            // player is looking at, so...
            return false;
        }

        Player player = (Player)sender;

        Block block = player.getTargetBlock(null, getConfig().getInt("lookDistance", 6));
        if (block == null || block.getType() != Material.MOB_SPAWNER) {
            sender.sendMessage("You must be looking directly at a spawner to use this command");
            return true;
        }

        if (args.length == 0) {
            CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);

            sender.sendMessage(spawner.getCreatureType().getName() + " spawner");
        } else {
            String creatureString = args[0];

            sender.sendMessage("TODO: set to "+creatureString);
        }

        return true;
    }


    // Create a tagged a mob spawner _item_ with its entity ID so we know what it spawns
    // This is not part of vanilla
    public static ItemStack newSpawnerItem(short entityID) {
        ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1, entityID);

        // Tag the entity ID several ways, for compatibility

        // Bukkit bug resets durability on spawners
        item.setDurability(entityID);

        // TODO: Creaturebox compatibility
        // see http://dev.bukkit.org/server-mods/creaturebox/pages/trading-mob-spawners/
        //item.addUnsafeEnchantment(Enchantment.OXYGEN, entityID);
        item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, entityID);

        return item;
    }

    // Get the entity ID
    public static short getStoredSpawnerItemEntityID(ItemStack item) {
        short id = item.getDurability();
        if (id != 0) {
            return id;
        }

        // TODO: compatibility with Creaturebox's 0-22
        /*
        id = (short)item.getEnchantmentLevel(Enchantment.OXYGEN);
        if (id != 0) {
            return id;
        }*/

        id = (short)item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
        if (id != 0) {
            return id;
        }

        return 0;
    }

    public static void informPlayer(Player player, String message) {
        if (player.hasPermission("silkspawners.info")) {
            player.sendMessage(message);
        }
    }
}


