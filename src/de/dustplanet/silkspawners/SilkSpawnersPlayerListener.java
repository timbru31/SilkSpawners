package de.dustplanet.silkspawners;

import org.bukkit.ChatColor;
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
	private SilkUtil su;

	public SilkSpawnersPlayerListener(SilkSpawners instance, SilkUtil util) {
		plugin = instance;
		su = util;
	}

	/**
	 * To show a chat message that a player is holding a mob spawner and it's type
	 * @param event
	 * @author (former) mushroomhostage
	 * @author xGhOsTkiLLeRx
	 */

	@EventHandler
	public void onPlayerHoldItem(PlayerItemHeldEvent event) {
		// Check if we should notify the player. The second condition is the permission and that the slot isn't null and the item is a mob spawner
		if (plugin.getConfig().getBoolean("notifyOnHold") && plugin.hasPermission((Player)event.getPlayer(), "silkspawners.info") && event.getPlayer().getInventory().getItem(event.getNewSlot()) != null && event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.MOB_SPAWNER)) {
			// Don't spam with pigs
			if (su.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot())) == 0 && su.defaultEntityID == 0) return;
			short entityID = su.getStoredSpawnerItemEntityID(event.getPlayer().getInventory().getItem(event.getNewSlot()));
			if (entityID == 0) entityID = su.defaultEntityID;
			// Get the name from the entityID
			String spawnerName = su.getCreatureName(entityID);
			Player player = event.getPlayer();
			// If the player uses SpoutCraft and we use Spout we send a notification
			if (plugin.spoutEnabled && ((SpoutPlayer) player).isSpoutCraftEnabled()) {
				((SpoutPlayer) player).sendNotification("Monster Spawner", spawnerName, Material.MOB_SPAWNER);
			}
			// Else the normal message
			else {
				player.sendMessage(" ");
				player.sendMessage("-- Monster Spawner --");
				player.sendMessage("-- Type: " + spawnerName);
				player.sendMessage("-- EntityID: " + entityID);
			}
		}
	}


	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		// If we use a spawn egg
		if (item != null && item.getType() == su.SPAWN_EGG) {
			// Get the entityID
			short entityID = item.getDurability();

			// Clicked spawner with monster egg to change type
			if (event.getAction() == Action.LEFT_CLICK_BLOCK &&	block != null && block.getType() == Material.MOB_SPAWNER) {
				// WorldGuard region protection
				if (!su.canBuildHere(player, block.getLocation())) return;

				if (!plugin.hasPermission(player, "silkspawners.changetypewithegg")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to change spawners with spawn eggs");
					return;
				}
				su.setSpawnerType(block, entityID, player);
				player.sendMessage(ChatColor.GREEN + "Successfully changed the spawner to a " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " spawner");

				// Consume egg
				if (plugin.getConfig().getBoolean("consumeEgg", true)) {
					PlayerInventory inventory = player.getInventory();
					// Get slot & egg
					int slot = inventory.getHeldItemSlot();
					ItemStack eggs = inventory.getItem(slot);
					// Make it empty
					if (eggs.getAmount() == 1) {
						// Common case.. one egg, used up
						inventory.clear(slot);
					}
					// Reduce
					else {
						// Cannot legitimately get >1 egg per slot (in 1.1, but supposedly 1.2 will support it), but should support it regardless
						inventory.setItem(slot, su.newEggItem(entityID, eggs.getAmount() - 1));
					}
				}
				// Normal spawning
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// Disabled by default, since it is dangerous
				if (plugin.getConfig().getBoolean("spawnEggOverride", false)) {
					// Name
					String mobID = su.eid2MobID.get(entityID);
					// Are we allowed to spawn?
					boolean allowed = plugin.getConfig().getBoolean("spawnEggOverrideSpawnDefault", true);
					if (mobID != null) {
						allowed = plugin.getConfig().getBoolean("creatures." + mobID + ".enableSpawnEggOverrideAllowSpawn", allowed);
					}
					// Deny spawning
					if (!allowed) {
						player.sendMessage(ChatColor.RED + "Spawning of " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " (ID " + entityID + ")" + ChatColor.RED + " denied");
						event.setCancelled(true);
						return;
					}
					// Bukkit doesn't allow us to spawn wither or dragons and so on. NMS here we go!
					// https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/ItemMonsterEgg.java#L22

					// Notify
					if (plugin.hasPermission(player, "silkspawners.info")) player.sendMessage(ChatColor.GREEN + "Spawning " + ChatColor.YELLOW + su.getCreatureName(entityID).toLowerCase() + " (ID " + entityID + ")" );

					// We can spawn using the direct method from EntityTypes
					// https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/EntityTypes.java#L67
					net.minecraft.server.World world = ((CraftWorld)player.getWorld()).getHandle();
					net.minecraft.server.Entity entity = net.minecraft.server.EntityTypes.a(entityID, world);
					// Should acutally never happen since the method above contains a null check, too
					if (entity == null) {
						if (plugin.hasPermission(player, "silkspawners.info")) {
							player.sendMessage(ChatColor.RED + "Failed to spawn, falling through. You should report this!");
						}
						return;
					}

					// Spawn on top of targetted block
					Location location = block.getLocation().add(0, 1, 0);
					double x = location.getX(), y = location.getY(), z = location.getZ();

					// Random facing
					entity.setPositionRotation(x, y, z, world.random.nextFloat() * 360.0f, 0.0f);
					// We need to add the entity to the world, reason is of course a spawn egg so that other events can handle this
					world.addEntity(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
					if (entity instanceof net.minecraft.server.EntityLiving) {
						((net.minecraft.server.EntityLiving)entity).aO();
					}

					// Remove item from player hand
					if (item.getAmount() == 1) player.setItemInHand(null);
					else {
						item.setAmount(item.getAmount() - 1);
						player.setItemInHand(item);
					}

					// Prevent normal spawning
					event.setCancelled(true);
				}
			}
		}
	}
}