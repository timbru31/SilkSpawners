package de.dustplanet.silkspawners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SilkSpawnersPlayerListener implements Listener {


	private SilkSpawners plugin;

	public SilkSpawnersPlayerListener(SilkSpawners plugin) {
		this.plugin = plugin;
	}
	/**
	 * To show a chat message that a player is holding a mob spawner and its type
	 * @param event
	 * @author Chris Churchwell (thedudeguy)
	 */
	@EventHandler
	public void OnHoldSpawner(PlayerItemHeldEvent event) {
		if (plugin.getConfig().getBoolean("notifyOnHold") && plugin.hasPermission((Player)event.getPlayer(), "silkspawners.info") && event.getPlayer().getInventory().getItem(event.getNewSlot()) != null && event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.MOB_SPAWNER)) {
			if (SilkSpawners.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot())) == 0 &&	plugin.defaultEntityID == 0) {
				return ;
			}

			short entityID = SilkSpawners.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot()));
			if (entityID == 0) entityID = plugin.defaultEntityID;

			String spawnerName = plugin.getCreatureName(entityID);

			if(plugin.spoutEnabled && ((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()) {
				((SpoutPlayer)event.getPlayer()).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
			} else {
				event.getPlayer().sendMessage(" ");
				event.getPlayer().sendMessage("-- Monster Spawner --");
				event.getPlayer().sendMessage("-- Type: " + spawnerName);
			}
		}
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

					String mobID = plugin.eid2MobID.get(entityID);

					boolean allowed = plugin.getConfig().getBoolean("spawnEggOverrideSpawnDefault", true);
					if (mobID != null) {
						allowed = plugin.getConfig().getBoolean("creatures."+mobID+".enableSpawnEggOverrideAllowSpawn", allowed);
					}
					if (!allowed) {
						player.sendMessage("Spawning "+entityID+" denied");
						event.setCancelled(true);
						return;
					}


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
						((net.minecraft.server.EntityLiving)entity).aH();
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
