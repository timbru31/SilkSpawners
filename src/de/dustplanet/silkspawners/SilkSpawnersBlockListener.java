package de.dustplanet.silkspawners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class SilkSpawnersBlockListener implements Listener {

	private SilkSpawners plugin;

	public SilkSpawnersBlockListener(SilkSpawners plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockBreak(final BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		// To prevent silk touch exploit by breaking blocks holding spawner (since abuses Silk Touch enchantment), cancel event
		ItemStack heldItem = player.getItemInHand();
		if (heldItem != null && heldItem.getType() == Material.MOB_SPAWNER && plugin.getConfig().getBoolean("denyBreakHoldingSpawner", true)) {
			event.setCancelled(true);
			return;
		}


		if (block.getType() != Material.MOB_SPAWNER) return;

		if (!plugin.canBuildHere(player, block.getLocation())) return;

		short entityID = plugin.getSpawnerEntityID(block);

		// mcMMO sends its own FakeBlockBreakEvent with super breaker ability, causing us to think
		// the spawner is broken when it isn't, allowing for duping: http://www.youtube.com/watch?v=GGlyZmph8NM
		// Ignore these events if configured
		if (event.getClass() != BlockBreakEvent.class && plugin.getConfig().getBoolean("ignoreFakeBreakEvents", true)) return;

		plugin.informPlayer(player, plugin.getCreatureName(entityID) + " spawner broken");

		// If using silk touch, drop spawner itself 
		ItemStack tool = player.getItemInHand();
		boolean silkTouch = hasSilkTouch(tool);

		ItemStack dropItem;
		World world = player.getWorld();

		// Prevent XP farming/duping
		event.setExpToDrop(0);
		if (plugin.hasPermission(player, "silkspawners.silkdrop") || plugin.hasPermission(player, "silkspawners.destroydrop")) {
			int addXP = plugin.getConfig().getInt("destroyDropXP");
			if (addXP != 0) {
				event.setExpToDrop(addXP);
			}
		}

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

		/*
		 * RESEARCH SAYS IT'S FIXED!
		 * 
		 */
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

		plugin.informPlayer(player, plugin.getCreatureName(entityID) + " spawner placed");

		// Bukkit 1.1-R3 regressed from 1.1-R1, ignores block state update on onBlockPlace
		// TODO: file or find bug about this, get it fixed so can remove this lame workaround
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new SilkSpawnersSetCreatureTask(entityID, blockPlaced, plugin, player), 0);
	}

	// Checks if the given ItemStack has got the SilkTouch
	private boolean hasSilkTouch(ItemStack tool) {
		int minLevel = plugin.getConfig().getInt("minSilkTouchLevel", 1);
		// Always have it
		if (minLevel == 0) return true;

		//  No silk touch fists..
		if (tool == null) return false;


		// This check isn't actually necessary, since containsEnchantment just checks level>0,
		// but is kept here for clarity, and in case Bukkit allows level-0 enchantments like vanilla
		if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) return false;

		return tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= minLevel;
	}
}