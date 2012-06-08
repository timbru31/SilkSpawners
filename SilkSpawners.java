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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


class SilkSpawnersBlockListener implements Listener {
    static Logger log = Logger.getLogger("Minecraft");

    SilkSpawners plugin;

    public SilkSpawnersBlockListener(SilkSpawners plugin) {
        this.plugin = plugin;
        
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean hasSilkTouch(ItemStack tool) {
        int minLevel = plugin.getConfig().getInt("minSilkTouchLevel", 1);
        if (minLevel == 0) {
            return true;    // always have it
        }

        if (tool == null) {
            return false;    // no silk touch fists..
        }


        // This check isn't actually necessary, since containsEnchantment just checks level>0,
        // but is kept here for clarity, and in case Bukkit allows level-0 enchantments like vanilla
        if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return false;
        }

        return tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= minLevel;
    }

    /**
	 * To show a chat message that a player clicked on an mob spawner
	 * @param event
	 * @author Chris Churchwell (thedudeguy)
	 */
	@EventHandler
	public void OnClickSpawner(InventoryClickEvent event) {
		
		if (
				plugin.getConfig().getBoolean("notifyOnClick") &&
				event.getCurrentItem() != null &&
				event.getCurrentItem().getType().equals(Material.MOB_SPAWNER) &&
				event.getWhoClicked() instanceof Player &&
				plugin.hasPermission((Player)event.getWhoClicked(), "silkspawners.info")
				) {
			
			if (
					SilkSpawners.getStoredSpawnerItemEntityID(event.getCurrentItem()) == 0 &&
					plugin.defaultEntityID == 0
					) {
				return ;
			}
			
			short entityID = SilkSpawners.getStoredSpawnerItemEntityID(event.getCurrentItem());
			if (entityID == 0) entityID = plugin.defaultEntityID;
			
			String spawnerName = plugin.getCreatureName(entityID);
			
			if (
					plugin.spoutEnabled &&
					((SpoutPlayer)event.getWhoClicked()).isSpoutCraftEnabled()
					) {
				((SpoutPlayer)event.getWhoClicked()).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
			} else {
				Player player = (Player)event.getWhoClicked();
				player.sendMessage(" ");
				player.sendMessage("-- Monster Spawner --");
				player.sendMessage("-- Type: " + spawnerName);
			}
		}
		
	}
    
	/**
	 * To show a chat message that a player is holding a mob spawner and its type
	 * @param event
	 * @author Chris Churchwell (thedudeguy)
	 */
	@EventHandler
	public void OnHoldSpawner(PlayerItemHeldEvent event) {
		if (
				plugin.getConfig().getBoolean("notifyOnHold") &&
				plugin.hasPermission((Player)event.getPlayer(), "silkspawners.info") &&
				event.getPlayer().getInventory().getItem(event.getNewSlot()) != null &&
				event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.MOB_SPAWNER)
				) {
			
			if (
					SilkSpawners.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot())) == 0 &&
					plugin.defaultEntityID == 0
					) {
				return ;
			}
			
			short entityID = SilkSpawners.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot()));
			if (entityID == 0) entityID = plugin.defaultEntityID;
			
			String spawnerName = plugin.getCreatureName(entityID);
			
			if(
					plugin.spoutEnabled && 
					((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()
					) {
				((SpoutPlayer)event.getPlayer()).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
			} else {
				event.getPlayer().sendMessage(" ");
				event.getPlayer().sendMessage("-- Monster Spawner --");
				event.getPlayer().sendMessage("-- Type: " + spawnerName);
			}
		}
	}
	
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockBreak(final BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.canBuildHere(player, block.getLocation())) {
            return;
        }


        short entityID = plugin.getSpawnerEntityID(block);

        plugin.informPlayer(player, plugin.getCreatureName(entityID)+" spawner broken");

        // If using silk touch, drop spawner itself 
        ItemStack tool = player.getItemInHand();
        boolean silkTouch = hasSilkTouch(tool);

        ItemStack dropItem;
        World world = player.getWorld();

        if (silkTouch && plugin.hasPermission(player, "silkspawners.silkdrop")) {
            // Drop spawner
            dropItem = SilkSpawners.newSpawnerItem(entityID);
            world.dropItemNaturally(block.getLocation(), dropItem);
            return;
        } 

        if (plugin.hasPermission(player, "silkspawners.destroydrop")) {
            if (plugin.getConfig().getBoolean("destroyDropEgg")) {
                // Drop egg
                world.dropItemNaturally(block.getLocation(), SilkSpawners.newEggItem(entityID));
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        Block blockPlaced = event.getBlockPlaced();

        if (blockPlaced.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.canBuildHere(player, blockPlaced.getLocation())) {
            return;
        }



        // https://bukkit.atlassian.net/browse/BUKKIT-596 - BlockPlaceEvent getItemInHand() loses enchantments
        // so, have to get item from player instead
        //ItemStack item = event.getItemInHand();
        ItemStack item = player.getItemInHand();

        // Get data from item
        short entityID = SilkSpawners.getStoredSpawnerItemEntityID(item);
        if (entityID == 0) {
            plugin.informPlayer(player, "Placing default spawner");
            entityID = plugin.defaultEntityID;

            if (entityID == 0) {
                // "default default"; defer to Minecraft
                return;
            }
        }

        plugin.informPlayer(player, plugin.getCreatureName(entityID)+" spawner placed");

        // Bukkit 1.1-R3 regressed from 1.1-R1, ignores block state update on onBlockPlace
        // TODO: file or find bug about this, get it fixed so can remove this lame workaround
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new SilkSpawnersSetCreatureTask(entityID, blockPlaced, plugin, player), 0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (item != null && item.getType() == SilkSpawners.SPAWN_EGG) {
            short entityID = item.getDurability();

            // Clicked spawner with monster egg to change type
            if (event.getAction() == Action.LEFT_CLICK_BLOCK &&
                block != null && block.getType() == Material.MOB_SPAWNER) {

                if (!plugin.canBuildHere(player, block.getLocation())) {
                    return;
                }

                if (!plugin.hasPermission(player, "silkspawners.changetypewithegg")) {
                    player.sendMessage("You do not have permission to change spawners with spawn eggs");
                    return;
                }


                plugin.setSpawnerType(block, entityID, player);

                // Consume egg
                if (plugin.getConfig().getBoolean("consumeEgg", true)) {
                    PlayerInventory inventory = player.getInventory();
                    int slot = inventory.getHeldItemSlot();

                    ItemStack eggs = inventory.getItem(slot);

                    if (eggs.getAmount() == 1) {
                        // Common case.. one egg, used up
                        inventory.clear(slot);
                    } else {
                        // Cannot legitimately get >1 egg per slot (in 1.1, but supposedly 1.2 will support it), but should support it regardless
                        inventory.setItem(slot, SilkSpawners.newEggItem(entityID, eggs.getAmount() - 1));
                    }
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Using spawn egg
                if (plugin.getConfig().getBoolean("spawnEggOverride", false)) { // disabled by default, since it is dangerous

                    // CB blacklists dragon (63) and 48,49 for some reason.. and it also prevents spawning of entities without
                    // its CB EntityType wrapper class, or entities that aren't living. Proof:
                    // https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/ItemMonsterEgg.java
                    //       if (world.isStatic || itemstack.getData() == 48 || itemstack.getData() == 49 || itemstack.getData() == 63) { // CraftBukkit
                    /*
                    Entity entity = EntityTypes.a(i, world);

                    if (entity != null && entity instanceof EntityLiving) { // CraftBukkit
                        entity.setPositionRotation(d0, d1, d2, world.random.nextFloat() * 360.0F, 0.0F);
                        world.addEntity(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG); // CraftBukkit
                        ((EntityLiving) entity).az();
                    }
                    */

                    // Its mob spawner also tries to detect "bad" entities", but is less stringent, in that it can spawn dragons!
                    // https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/TileEntityMobSpawner.java
                    /*
                    Entity mob = EntityTypes.createEntityByName(this.mobName, this.world);
                    if (!(mob instanceof EntityLiving)) {
                        mobName = "Pig";
                        return;
                    }
                    EntityLiving entityliving = (EntityLiving) ((EntityLiving) mob);
                    // CraftBukkit end
                    */

                    // where is EntityTypes? it isn't in CB, but can be found decompiled in mc-dev:
                    // https://github.com/MinecraftPortCentral/mc-dev/blob/master/net/minecraft/server/EntityTypes.java
                    // nms EntityTypes.a() will let you spawn by entity id

                    if (plugin.hasPermission(player, "silkspawners.info")) {
                        player.sendMessage("Spawning entity " + entityID);
                    }

                    net.minecraft.server.World world = ((CraftWorld)player.getWorld()).getHandle();

                    net.minecraft.server.Entity entity = net.minecraft.server.EntityTypes.a(entityID, world);

                    if (entity == null) {
                        if (plugin.hasPermission(player, "silkspawners.info")) {
                            player.sendMessage("Failed to spawn, falling through");
                        }
                        return; // not cancelled
                    }

                    // Spawn on top of targetted block
                    Location location = block.getLocation().add(0, 1, 0);

                    double x = location.getX();
                    double y = location.getY();
                    double z = location.getZ();

                    // Magic
                    entity.setPositionRotation(x, y, z, world.random.nextFloat() * 360.0f, 0.0f);
                    world.addEntity(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
                    if (entity instanceof net.minecraft.server.EntityLiving) {
                        ((net.minecraft.server.EntityLiving)entity).az();
                    }

                    // Remove item from player hand
                    if (item.getAmount() == 1) {
                        player.setItemInHand(null);
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        player.setItemInHand(item);
                    }
                    
                    // prevent normal spawning
                    event.setCancelled(true);
                }
            }
        }
    }
}

class SilkSpawnersSetCreatureTask implements Runnable {
    short entityID;
    Block block;
    SilkSpawners plugin;
    Player player;

    public SilkSpawnersSetCreatureTask(short entityID, Block block, SilkSpawners plugin, Player player) {
        this.entityID = entityID;
        this.block = block;
        this.plugin = plugin;
        this.player = player;
    }

    public void run() {
        try {
            plugin.setSpawnerEntityID(block, entityID);
        } catch (Exception e) {
            plugin.informPlayer(player, "Failed to set type: " + e);
        }
    }
}

public class SilkSpawners extends JavaPlugin {
    static Logger log = Logger.getLogger("Minecraft");
    SilkSpawnersBlockListener blockListener;

    ConcurrentHashMap<Short,String> eid2DisplayName;    // human-readable friendly name
    ConcurrentHashMap<Short,String> eid2MobID;          // internal String used by spawners
    ConcurrentHashMap<String,Short> mobID2Eid;
    ConcurrentHashMap<String,Short> name2Eid;           // aliases to entity ID

    short defaultEntityID;
    boolean usePermissions;

    Field tileField, mobIDField;

    // To avoid confusing with badly name MONSTER_EGGS (silverfish), set our own material
    final static Material SPAWN_EGG = Material.MONSTER_EGG;
    
    public boolean spoutEnabled = false;

    public void onEnable() {
        loadConfig();
        
        //check for spout
      	if (Bukkit.getPluginManager().isPluginEnabled("Spout")) {
      		if (getConfig().getBoolean("useSpout")) {
      			Bukkit.getLogger().log(Level.INFO, "[SilkSpawners] Spout present. Enabling Spout features.");
      			spoutEnabled = true;
      		} else {
      			Bukkit.getLogger().log(Level.INFO, "[SilkSpawners] Disabling Spout features even though Spout is present.");
      		}
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

    // Copy default configuration
    // Sure we could use getConfig().options().copyDefaults(true);, but it strips all comments :(
    public boolean newConfig(File file) {
        FileWriter fileWriter;
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try {
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            log.severe("Couldn't write config file: " + e.getMessage());
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("config.yml"))));
        BufferedWriter writer = new BufferedWriter(fileWriter);
        try {
            String line = reader.readLine();
            while (line != null) {
                writer.write(line + System.getProperty("line.separator"));
                line = reader.readLine();
            }
            log.info("Wrote default config");
        } catch (IOException e) {
            log.severe("Error writing config: " + e.getMessage());
        } finally {
            try {
                writer.close();
                reader.close();
            } catch (IOException e) {
                log.severe("Error saving config: " + e.getMessage());
                Bukkit.getServer().getPluginManager().disablePlugin(this);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void scanEntityMap() {
        SortedMap<Integer,String> sortedMap = new TreeMap<Integer,String>();

        // Use reflection to dump native EntityTypes
        // This bypasses Bukkit's wrappers, so it works with mods
        try {
            // https://github.com/MinecraftPortCentral/mc-dev/blob/master/net/minecraft/server/EntityTypes.java
            // f.put(s, Integer.valueOf(i));
            Field field = net.minecraft.server.EntityTypes.class.getDeclaredField(getConfig().getString("entityMapField", "f"));
            field.setAccessible(true);
            Map<String,Integer> map = (Map<String,Integer>)field.get(null);

            for (Map.Entry<String,Integer> entry: ((Map<String,Integer>)map).entrySet()) {
                sortedMap.put(entry.getValue(), entry.getKey());
            }
        } catch (Exception e) {
            log.severe("Failed to dump entity map: " + e);
        }

        for (Map.Entry<Integer,String> entry: sortedMap.entrySet()) {
            short eid = (short)(int)entry.getKey();
            String mobID = entry.getValue();

            if (getConfig().getBoolean("dumpEntityMap", false)) {
                log.info("Entity " + eid + " = " + mobID);
            }

            eid2MobID.put(eid, mobID);
            mobID2Eid.put(mobID, eid);
        }
    }

    private void loadConfig() {
        String filename = getDataFolder() + System.getProperty("file.separator") + "config.yml";
        File file = new File(filename);

        if (!file.exists()) {
            if (!newConfig(file)) {
                throw new IllegalArgumentException("Could not create new configuration file");
            }
        }
        reloadConfig();

        boolean verbose = getConfig().getBoolean("verboseConfig", true);

        eid2MobID = new ConcurrentHashMap<Short,String>();
        mobID2Eid = new ConcurrentHashMap<String,Short>();

        scanEntityMap();

        eid2DisplayName = new ConcurrentHashMap<Short,String>();
        name2Eid = new ConcurrentHashMap<String,Short>();

        // Creature info
        MemorySection creatureSection = (MemorySection)getConfig().get("creatures");
    
        for (String creatureString: creatureSection.getKeys(false)) {
            if (!getConfig().getBoolean("useExtraMobs", false) && !isRecognizedMob(creatureString)) {
                if (verbose) { 
                    log.info("Skipping unrecognized mob "+creatureString+", set useExtraMob: true to enable");
                }
                continue;
            }

            // TODO: http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs in Bukkit?
            // TODO: there is in 1.1-R5! see getSpawnedType(), and EntityType - but check if it works with mods!
            // http://forums.bukkit.org/threads/branch-getcreaturetype.61838/
            short entityID = (short)getConfig().getInt("creatures."+creatureString+".entityID");

            // Internal mob ID used for spawner type 
            eid2MobID.put(entityID, creatureString);
            mobID2Eid.put(creatureString, entityID);

            // TODO: replace config file with built-in info! see
            // http://forums.bukkit.org/threads/help-how-to-get-an-animals-type-id.60156/
            // "[HELP]How to get an animal's type id"


            // In-game name for user display, and other recognized names for user input lookup

            String displayName = getConfig().getString("creatures."+creatureString+".displayName");
            if (displayName == null) {
                displayName = creatureString;
            }

            eid2DisplayName.put(entityID, displayName);

            List<String> aliases = getConfig().getStringList("creatures."+creatureString+".aliases");

            aliases.add(displayName.toLowerCase().replace(" ", ""));
            aliases.add(creatureString.toLowerCase().replace(" ", ""));
            aliases.add(entityID+"");

            for (String alias: aliases) {
                name2Eid.put(alias, entityID);
            }
        }

        // Get the entity ID of the creatures to spawn on damage 0 spawners, or otherwise not override
        // (then will default to Minecraft's default of pigs)
        defaultEntityID = 0;

        String defaultCreatureString = getConfig().getString("defaultCreature", null);
        if (defaultCreatureString != null) {
            if (name2Eid.containsKey(defaultCreatureString)) {
                short defaultEid = name2Eid.get(defaultCreatureString);
                ItemStack defaultItemStack = newEggItem(defaultEid);
                if (defaultItemStack != null) {
                    defaultEntityID = defaultItemStack.getDurability();
                    if (verbose) { 
                        log.info("Default monster spawner set to "+eid2DisplayName.get(defaultEid));
                    }
                } else {
                    log.warning("Unable to lookup name of " + defaultCreatureString+", default monster spawner not set");
                }
            } else {
                log.warning("Invalid creature type: " + defaultCreatureString+", default monster spawner not set");
            }
        }

        usePermissions = getConfig().getBoolean("usePermissions", false);

        if (getConfig().getBoolean("craftableSpawners", false)) {
            loadRecipes();
        }

        if (getConfig().getBoolean("useReflection", true)) {
            try {
                tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
                tileField.setAccessible(true);

                mobIDField = net.minecraft.server.TileEntityMobSpawner.class.getDeclaredField("mobName");  // MCP "mobID"
                mobIDField.setAccessible(true);
            } catch (Exception e) {
                log.warning("Failed to reflect, falling back to wrapper methods: " + e);
                tileField = null;
                mobIDField = null;
            }
        } else {
            tileField = null;
            mobIDField = null;
        }

        // Optionally make spawners unstackable in an attempt to be more compatible with CraftBukkit++
        // Requested on http://dev.bukkit.org/server-mods/silkspawners/#c25
        if (getConfig().getBoolean("spawnersUnstackable", false)) {
            // http://forums.bukkit.org/threads/setting-max-stack-size.66364/
            try {
                Field maxStackSizeField = net.minecraft.server.Item.class.getDeclaredField(getConfig().getString("spawnersUnstackableField", "maxStackSize"));
                
                maxStackSizeField.setAccessible(true);
                maxStackSizeField.setInt(net.minecraft.server.Item.byId[Material.MOB_SPAWNER.getId()], 1);
            } catch (Exception e) {
                log.warning("Failed to set max stack size, ignoring spawnersUnstackable: " + e);
            }
        }
    }

    private void loadRecipes() {
        for (short entityID: eid2MobID.keySet()) {
            String mobID = eid2MobID.get(entityID);

            if (!getConfig().getBoolean("creatures."+mobID+".enableCraftingSpawner", true)) {
                if (getConfig().getBoolean("verboseConfig", true)) {
                    log.info("Skipping crafting recipe for "+mobID+" per config");
                }
                continue;
            }

            ItemStack spawnerItem = newSpawnerItem(entityID);

            ShapedRecipe recipe = new ShapedRecipe(spawnerItem);

            recipe.shape(new String[] { "AAA", "ABA", "AAA" });
            recipe.setIngredient('A', Material.IRON_FENCE);
            recipe.setIngredient('B', Material.MONSTER_EGG, (int)entityID);

            Bukkit.getServer().addRecipe(recipe);
        }
    }

    public void onDisable() {
        log.info("SilkSpawners disabled");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("silkspawners")) {
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

            try {
                short entityID = getSpawnerEntityID(block);

                sender.sendMessage(getCreatureName(entityID) + " spawner");
            } catch (Exception e) {
                informPlayer(player, "Failed to identify spawner: " + e);
            }

        } else {
            // Set or get spawner

            Block block = getSpawnerFacing(player);

            String creatureString = args[0];
            if (creatureString.equalsIgnoreCase("all")) {
                // Get list of all creatures..anyone can do this
                showAllCreatures(player);
                return true;
            }

            boolean isEgg = false;

            if (creatureString.endsWith("egg")) {
                isEgg = true;
                creatureString = creatureString.replaceFirst("egg$", "");
            }

            if (!name2Eid.containsKey(creatureString)) {
                player.sendMessage("Unrecognized creature "+creatureString);
                return true;
            }

            short entityID = name2Eid.get(creatureString);

            if (block != null && !isEgg) {
                if (!hasPermission(player, "silkspawners.changetype")) {
                    player.sendMessage("You do not have permission to change spawners with /spawner");
                    return true;
                }

                setSpawnerType(block, entityID, player);
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

                if (isEgg) {
                    player.setItemInHand(newEggItem(entityID));
                    sender.sendMessage(getCreatureName(entityID) + " spawn egg");
                } else {
                    player.setItemInHand(newSpawnerItem(entityID));
                    sender.sendMessage(getCreatureName(entityID) + " spawner");
                }
            }
        }

        return true;
    }

    // Set spawner type from user
    public void setSpawnerType(Block block, short entityID, Player player) {
        if (!canBuildHere(player, block.getLocation())) {
            player.sendMessage("Changing spawner type denied by WorldGuard protection");
            return;
        }

        try {
            setSpawnerEntityID(block, entityID);
        } catch (Exception e) {
            informPlayer(player, "Failed to set type: " + e);
        }

        player.sendMessage(getCreatureName(entityID) + " spawner");
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
    // Internal mob names are are like 'LavaSlime', this will return
    // the in-game name like 'Magma Cube'
    public String getCreatureName(short entityID) {
        String displayName = eid2DisplayName.get(entityID);

        if (displayName == null) {
            EntityType ct = EntityType.fromId(entityID);
            if (ct != null) {
                displayName = "("+ct.getName()+")";
            } else {
                displayName = String.valueOf(entityID);
            }
        }
    
        return displayName;
    }

    public static ItemStack newEggItem(short entityID, int amount) {
        return new ItemStack(SPAWN_EGG, 1, entityID);
    }

    public static ItemStack newEggItem(short entityID) {
        return newEggItem(entityID, 1);
    }


    // Create a tagged a mob spawner _item_ with its entity ID so we know what it spawns
    // This is not part of vanilla, but our own convention
    public static ItemStack newSpawnerItem(short entityID) {
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

        return 0;
    }

    public void showAllCreatures(Player player) {
        String message = "";
        for (String displayName: eid2DisplayName.values()) {
            displayName = displayName.replaceAll(" ", "");
            message += displayName + ", ";
        }
        message = message.substring(0, message.length() - ", ".length());
        player.sendMessage(message);
    }

    public void informPlayer(Player player, String message) {
        if (hasPermission(player, "silkspawners.info")) {
            player.sendMessage(message);
        }
    }

    // Return whether mob is recognized by Bukkit's wrappers
    public boolean isRecognizedMob(String mobID) {
        return EntityType.fromName(mobID) != null;
    }

    // Better methods for setting/getting spawner type
    // These don't rely on CreatureSpawner, if possible, and instead set/get the 
    // mobID directly from the tile entity

    public short getSpawnerEntityID(Block block) {
        BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            throw new IllegalArgumentException("getSpawnerEntityID called on non-spawner block: " + block);
        }

        CraftCreatureSpawner spawner = ((CraftCreatureSpawner)blockState);

        // Get the mob ID ourselves if we can
        if (tileField != null && mobIDField != null) {
            try {
                net.minecraft.server.TileEntityMobSpawner tile = (net.minecraft.server.TileEntityMobSpawner)tileField.get(spawner);
                //log.info("tile ="+tile);

                String mobID = (String)mobIDField.get(tile);
                //log.info("mobID ="+mobID);

                return mobID2Eid.get(mobID);
            } catch (Exception e) {
                log.info("Reflection failed: " + e);
                // fall through
            } 
        }

        // or ask Bukkit if we have to
        //int entityID = spawner.getSpawnedType().getTypeId();    // TODO: 1.1-R5
        return spawner.getSpawnedType().getTypeId();
    }
    
    public void setSpawnerEntityID(Block block, short entityID) {
        BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            throw new IllegalArgumentException("setSpawnerEntityID called on non-spawner block: " + block);
        }

        CraftCreatureSpawner spawner = ((CraftCreatureSpawner)blockState);

        // Try the more powerful native methods first
        if (tileField != null && mobIDField != null) {
            try {
                String mobID = eid2MobID.get(entityID);

                net.minecraft.server.TileEntityMobSpawner tile = (net.minecraft.server.TileEntityMobSpawner)tileField.get(spawner);
                //log.info("tile ="+tile);

                tile.a(mobID);      // MCP setMobID
                return;
            } catch (Exception e) {
                log.info("Reflection failed: " + e);
                // fall through
            }
        }

        // Fallback to wrapper
        EntityType ct = EntityType.fromId(entityID);
        if (ct == null) {
            throw new IllegalArgumentException("Failed to find creature type for "+entityID);
        }

        spawner.setSpawnedType(ct);
        //spawner.setSpawnedType(EntityType.fromId(entityID)); // TODO: 1.1-R5
        blockState.update();
   }

    // http://wiki.sk89q.com/wiki/WorldGuard/Regions/API
    public WorldGuardPlugin getWorldGuard() {
        if (!getConfig().getBoolean("useWorldGuard", true)) {
            return null;
        }

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin)plugin;
    }

    public boolean canBuildHere(Player player, Location location) {
        WorldGuardPlugin wg = getWorldGuard();
        if (wg == null) {
            return true;
        }

        return wg.canBuild(player, location);
    }
}


