/*
Copyright (c) 2012, Mushroom Hostage
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
import org.bukkit.entity.*;
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

import net.minecraft.server.CraftingManager;        
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;

class SilkSpawnersBlockListener implements Listener {
    static Logger log = Logger.getLogger("Minecraft");

    SilkSpawners plugin;

    public SilkSpawnersBlockListener(SilkSpawners plugin) {
        this.plugin = plugin;
        
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();

        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);

        CreatureType creatureType = spawner.getCreatureType();

        plugin.informPlayer(player, plugin.getCreatureName(creatureType)+" spawner broken");

        // If using silk touch, drop spawner itself 
        ItemStack tool = player.getItemInHand();
        boolean silkTouch = tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH);

        ItemStack dropItem;
        World world = player.getWorld();

        if (silkTouch && plugin.hasPermission(player, "silkspawners.silkdrop")) {
            // Drop spawner
            dropItem = plugin.newSpawnerItem(creatureType);
            world.dropItemNaturally(block.getLocation(), dropItem);
            return;
        } 

        if (plugin.hasPermission(player, "silkspawners.destroydrop")) {
            if (plugin.getConfig().getBoolean("destroyDropEgg")) {
                // Drop egg
                dropItem = plugin.creature2Egg.get(creatureType);
                world.dropItemNaturally(block.getLocation(), dropItem);
            }

            int addXP = plugin.getConfig().getInt("destroyDropXP");
            if (addXP != 0) {
                ExperienceOrb orb = world.spawn(block.getLocation(), ExperienceOrb.class);
                orb.setExperience(addXP);
            }

            int dropBars = plugin.getConfig().getInt("destroyDropBars");
            if (dropBars != 0) {
                world.dropItem(block.getLocation(), new ItemStack(Material.IRON_FENCE, dropBars));
            }
        } 
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block blockPlaced = event.getBlockPlaced();

        if (blockPlaced.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();

        // https://bukkit.atlassian.net/browse/BUKKIT-596 - BlockPlaceEvent getItemInHand() loses enchantments
        // so, have to get item from player instead
        //ItemStack item = event.getItemInHand();
        ItemStack item = player.getItemInHand();

        // Get data from item
        short entityID = plugin.getStoredSpawnerItemEntityID(item);
        if (entityID == 0) {
            plugin.informPlayer(player, "Placing default spawner");
            entityID = plugin.defaultEntityID;

            if (entityID == 0) {
                // "default default"; defer to Minecraft
                return;
            }
        }

        CreatureType creature = plugin.eid2Creature.get(entityID);
        if (creature == null) {
            plugin.informPlayer(player, "No creature associated with spawner");
            return;
        }
        plugin.informPlayer(player, plugin.getCreatureName(creature)+" spawner placed");

        // Bukkit 1.1-R3 regressed from 1.1-R1, ignores block state update on onBlockPlace
        // TODO: file or find bug about this, get it fixed so can remove this lame workaround
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new SilkSpawnersSetCreatureTask(creature, blockPlaced, plugin, player), 0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = event.getItem();

            // Clicked spawner with monster egg to change type
            if (item != null && item.getTypeId() == plugin.SPAWN_EGG_ID) {
                Block block = event.getClickedBlock();
                Player player = event.getPlayer();

                short entityID = item.getDurability();
                CreatureType creatureType = plugin.eid2Creature.get(entityID);

                if (creatureType == null) {
                    player.sendMessage("Unrecognized creature in spawn egg ("+entityID+")");
                    return;
                }

                plugin.setSpawnerType(block, creatureType, player);

                // Consume egg
                if (plugin.getConfig().getBoolean("consumeEgg", true)) {
                    player.getInventory().removeItem(new ItemStack(plugin.SPAWN_EGG_ID, 1, entityID));
                }
            }
        }
    }
}

class SilkSpawnersSetCreatureTask implements Runnable {
    CreatureType creature;
    Block blockPlaced;
    SilkSpawners plugin;
    Player player;

    public SilkSpawnersSetCreatureTask(CreatureType creature, Block blockPlaced, SilkSpawners plugin, Player player) {
        this.creature = creature;
        this.blockPlaced = blockPlaced;
        this.plugin = plugin;
        this.player = player;
    }

    public void run() {
        /* non-Bukkit-API method 
        CraftCreatureSpawner spawner = new CraftCreatureSpawner(blockPlaced);
        if (spawner == null) {
            plugin.informPlayer(player, "Failed to find placed spawner, creature not set");
            return;
        }
        spawner.setCreatureType(creature);
        */

       
        BlockState bs = blockPlaced.getState(); // yes, it is bs
        if (!(bs instanceof CreatureSpawner)) {
            plugin.informPlayer(player, "Failed to get block state, creature not set");
            return;
        }

        CreatureSpawner spawner = (CreatureSpawner)bs;

        spawner.setCreatureType(creature);

        bs.update();
    }
}

public class SilkSpawners extends JavaPlugin {
    static Logger log = Logger.getLogger("Minecraft");
    SilkSpawnersBlockListener blockListener;

    ConcurrentHashMap<CreatureType,ItemStack> creature2Egg;
    ConcurrentHashMap<Short,CreatureType> eid2Creature;
    ConcurrentHashMap<CreatureType,Short> creature2Eid;
    
    static ConcurrentHashMap<Short,Short> legacyID2Eid;

    ConcurrentHashMap<CreatureType,String> creature2DisplayName;
    ConcurrentHashMap<String,CreatureType> name2Creature;

    short defaultEntityID;
    boolean usePermissions;

    // Some modded versions of craftbukkit-1.1-R3 lack Material.MONSTER_EGG, so hardcode the ID
    final static int SPAWN_EGG_ID = 383;    // http://www.minecraftwiki.net/wiki/Data_values

    public void onEnable() {
        loadConfig();

        if (getConfig().getBoolean("craftableSpawners", true)) {
            loadRecipes();
        }

        // Listeners
        blockListener = new SilkSpawnersBlockListener(this);

        log.info("SilkSpawners enabled");
    }

    public boolean hasPermission(Player player, String node) {
        if (usePermissions) {
            return player.hasPermission(node);
        } else {
            if (node.equals("silkspawners.info") ||
                node.equals("silkspawners.silkdrop") ||
                node.equals("silkspawners.destroydrop") ||
                node.equals("silkspawners.viewtype")) {
                return true;
            } else {
                return player.isOp();
            }
        }
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        creature2Egg = new ConcurrentHashMap<CreatureType,ItemStack>();
        eid2Creature = new ConcurrentHashMap<Short,CreatureType>();
        creature2Eid = new ConcurrentHashMap<CreatureType,Short>();
        
        legacyID2Eid = new ConcurrentHashMap<Short,Short>();

        creature2DisplayName = new ConcurrentHashMap<CreatureType,String>();
        name2Creature = new ConcurrentHashMap<String,CreatureType>();

        // Creature info
        MemorySection creatureSection = (MemorySection)getConfig().get("creatures");
    
        for (String creatureString: creatureSection.getKeys(false)) {
            CreatureType creatureType = CreatureType.fromName(creatureString);
            if (creatureType == null) {
                log.info("Invalid creature type: " + creatureString);
                continue;
            }

            // TODO: http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs in Bukkit?
            short entityID = (short)getConfig().getInt("creatures."+creatureString+".entityID");

            //ItemStack eggItem = new ItemStack(Material.MONSTER_EGG, 1, entityID);
            ItemStack eggItem = new ItemStack(SPAWN_EGG_ID, 1, entityID);

            creature2Egg.put(creatureType, eggItem);
            eid2Creature.put(new Short(entityID), creatureType);
            creature2Eid.put(creatureType, new Short(entityID));

            short legacyID = (short)getConfig().getInt("creatures."+creatureString+".legacyID");
            legacyID2Eid.put(new Short(legacyID), new Short(entityID));


            // In-game name for user display, and other recognized names for user input lookup

            String displayName = getConfig().getString("creatures."+creatureString+".displayName");
            if (displayName == null) {
                displayName = creatureString;
            }

            creature2DisplayName.put(creatureType, displayName);

            List<String> aliases = getConfig().getStringList("creatures."+creatureString+".aliases");

            aliases.add(displayName.toLowerCase().replace(" ", ""));
            aliases.add(creatureString.toLowerCase().replace(" ", ""));
            aliases.add(entityID+"");
            aliases.add("#"+legacyID);

            for (String alias: aliases) {
                name2Creature.put(alias, creatureType);
            }
        }

        // Get the entity ID of the creatures to spawn on damage 0 spawners, or otherwise not override
        // (then will default to Minecraft's default of pigs)
        defaultEntityID = 0;

        String defaultCreatureString = getConfig().getString("defaultCreature", null);
        if (defaultCreatureString != null) {
            CreatureType defaultCreatureType = name2Creature.get(defaultCreatureString);
            if (defaultCreatureType != null) {
                ItemStack defaultItemStack = creature2Egg.get(defaultCreatureType);
                if (defaultItemStack != null) {
                    defaultEntityID = defaultItemStack.getDurability();
                    log.info("Default monster spawner set to "+creature2DisplayName.get(defaultCreatureType));
                } else {
                    log.info("Unable to lookup name of " + defaultCreatureString);
                }
            } else {
                log.info("Invalid creature type: " + defaultCreatureString);
            }
        }

        usePermissions = getConfig().getBoolean("usePermissions", false);
    }

    private void loadRecipes() {
        try {
            Material.valueOf("MONSTER_EGG");
        } catch (NoSuchFieldError e) {
            log.info("Your Bukkit is missing Material.MONSTER_EGG; disabling craftableSpawners");
            return;
        }

        for (ItemStack egg: creature2Egg.values()) {
            short entityID = egg.getDurability();
            CreatureType creatureType = eid2Creature.get(entityID);

            ItemStack spawnerItem = newSpawnerItem(creatureType);
            ShapelessRecipe recipe = new ShapelessRecipe(spawnerItem);

            // TODO: ShapedRecipe, box
            recipe.addIngredient(8, Material.IRON_FENCE);
            // Bukkit addIngredient() only accepts Material, not type id, so if MONSTER_EGG isn't
            // available we can't add it
            recipe.addIngredient(Material.MONSTER_EGG, (int)entityID);

            if (getConfig().getBoolean("workaroundBukkitBug602", true)) {
                // Workaround Bukkit bug:
                // https://bukkit.atlassian.net/browse/BUKKIT-602 Enchantments lost on crafting recipe output
                // CraftBukkit/src/main/java/org/bukkit/craftbukkit/inventory/CraftShapelessRecipe.java
                ArrayList<MaterialData> ingred = recipe.getIngredientList();
                Object[] data = new Object[ingred.size()];
                int i = 0;
                for (MaterialData mdata : ingred) {
                    int id = mdata.getItemTypeId();
                    byte dmg = mdata.getData();
                    data[i] = new net.minecraft.server.ItemStack(id, 1, dmg);
                    i++;
                }

                // Convert Bukkit ItemStack to net.minecraft.server.ItemStack
                int id = recipe.getResult().getTypeId();
                int amount = recipe.getResult().getAmount();
                short durability = recipe.getResult().getDurability();
                Map<Enchantment, Integer> enchantments = recipe.getResult().getEnchantments();
                net.minecraft.server.ItemStack result = new net.minecraft.server.ItemStack(id, amount, durability);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    result.addEnchantment(CraftEnchantment.getRaw(entry.getKey()), entry.getValue().intValue());
                }

                CraftingManager.getInstance().registerShapelessRecipe(result, data);

            } else {
                Bukkit.getServer().addRecipe(recipe);
            }
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

        if (args.length == 0) {
            // Get spawner type
            if (!hasPermission(player, "silkspawners.viewtype")) {
                sender.sendMessage("You do not have permission to view the spawner type");
                return true;
            }

            Block block = getSpawnerFacing(player);
            if (block == null) {
                sender.sendMessage("You must be looking directly at a spawner to use this command");
                return false;
            }

            CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);
            if (spawner == null) {
                sender.sendMessage("Failed to find spawner");
                return true;
            }

            CreatureType creatureType = spawner.getCreatureType();

            sender.sendMessage(getCreatureName(creatureType) + " spawner");
        } else {
            // Set or get spawner

            Block block = getSpawnerFacing(player);

            String creatureString = args[0];

            CreatureType creatureType = name2Creature.get(creatureString);
            if (creatureType == null) {
                sender.sendMessage("Unrecognized creature "+creatureString);
                return true;
            }

            if (block != null) {
                setSpawnerType(block, creatureType, player);
            } else {
                // Get free spawner item in hand
                if (!hasPermission(player, "silkspawners.freeitem")) {
                    if (hasPermission(player, "silkspawners.viewtype")) {
                        sender.sendMessage("You must be looking directly at a spawner to use this command");
                    } else {
                        sender.sendMessage("You do not have permission to use this command");
                    }
                    return true;
                }

                if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
                    sender.sendMessage("To use this command, empty your hand (to get a free spawner item) or point at an existing spawner (to change the spawner type)");
                    return true;
                }

                player.setItemInHand(newSpawnerItem(creatureType));
                
                sender.sendMessage(getCreatureName(creatureType) + " spawner");
            }
        }

        return true;
    }

    // Set spawner type from user
    public void setSpawnerType(Block block, CreatureType creatureType, Player player) {
        if (!hasPermission(player, "silkspawners.changetype")) {
            player.sendMessage("You do not have permission to change spawners");
            return;
        }

        // TODO: use Bukkit CreatureSpawner, get block state
        CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);
        if (spawner == null) {
            player.sendMessage("Failed to find spawner, creature not set");
            return;
        }
        spawner.setCreatureType(creatureType); 

        player.sendMessage(getCreatureName(creatureType) + " spawner");
    }

    // Return the spawner block the player is looking at, or null if isn't
    private Block getSpawnerFacing(Player player) {
        Block block = player.getTargetBlock(null, getConfig().getInt("spawnerCommandReachDistance", 6));
        if (block == null || block.getType() != Material.MOB_SPAWNER) {
            return null;
        }

        return block;
    }



    // Get a creature name suitable for displaying to the user
    // CreatureType getName has internal names like 'LavaSlime', this will return
    // the in-game name like 'Magma Cube'
    public String getCreatureName(CreatureType creature) {
        String displayName = creature2DisplayName.get(creature);

        if (displayName == null) {
            displayName = "("+creature.getName()+")";
        }
    
        return displayName;
    }


    // Create a tagged a mob spawner _item_ with its entity ID so we know what it spawns
    // This is not part of vanilla
    public ItemStack newSpawnerItem(CreatureType creatureType) {
        Short entityIDObject = creature2Eid.get(creatureType);
        if (entityIDObject == null) {
            log.info("newSpawnerItem("+creatureType+") unexpectedly failed to lookup entityID");
            return null;
        }

        short entityID = entityIDObject.shortValue();
        ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1, entityID);

        // Tag the entity ID several ways, for compatibility

        // Bukkit bug resets durability on spawners
        // https://bukkit.atlassian.net/browse/BUKKIT-329 MobSpawner should retains durability/data values.
        // Try it anyways, just in case the bug has been fixed
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

        // Creaturebox compatibility
        short legacyID = (short)item.getEnchantmentLevel(Enchantment.OXYGEN);
        // Bukkit API doesn't allow you tell if an enchantment is present vs. level 0 (=pigs),
        // so ignore if level 0. This is a disadvantage of creaturebox's tagging system.
        if (legacyID != 0) {
            id = legacyID2Eid.get(legacyID);
            if (id != 0) {
                return id;
            }
        }

        return 0;
    }

    public void informPlayer(Player player, String message) {
        if (hasPermission(player, "silkspawners.info")) {
            player.sendMessage(message);
        }
    }
}


