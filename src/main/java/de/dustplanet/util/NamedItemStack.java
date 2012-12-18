package de.dustplanet.util;

import java.util.ArrayList;
import net.minecraft.server.v1_4_5.NBTTagCompound;
import net.minecraft.server.v1_4_5.NBTTagList;
import net.minecraft.server.v1_4_5.NBTTagString;
import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * @author nisovin
 * Taken from: http://forums.bukkit.org/threads/item-name-changing-class.104249/#post-1365792
 * Thanks for sharing!
 *
 */

public class NamedItemStack {
	private net.minecraft.server.v1_4_5.ItemStack itemStack;

	public NamedItemStack(ItemStack item) {
		itemStack = CraftItemStack.asNMSCopy(item);
		if (itemStack == null) System.out.println("null here ");
		else {
			System.out.println(itemStack.r() + " " + itemStack.id);
		}
		NBTTagCompound tag = itemStack.tag;
		if (tag == null) {
			tag = new NBTTagCompound();
			tag.setCompound("display", new NBTTagCompound());
			itemStack.tag = tag;
		}
	}

	public NamedItemStack setName(String name) {
		NBTTagCompound tag = itemStack.tag.getCompound("display");
		tag.setString("Name", name);
		itemStack.tag.setCompound("display", tag);
		return this;
	}

	public String getName() {
		NBTTagCompound tag = itemStack.tag.getCompound("display");
		return tag.getString("Name");
	}

	public NamedItemStack addLore(String lore) {
		NBTTagCompound tag = itemStack.tag.getCompound("display");
		NBTTagList list = tag.getList("Lore");
		if (list == null) list = new NBTTagList();
		list.add(new NBTTagString(lore));
		tag.set("Lore", list);
		itemStack.tag.setCompound("display", tag);
		return this;
	}

	public NamedItemStack setLore(String... lore) {
		NBTTagCompound tag = itemStack.tag.getCompound("display");
		NBTTagList list = new NBTTagList();
		for (String l : lore) {
			list.add(new NBTTagString(l));
		}
		tag.set("Lore", list);
		itemStack.tag.setCompound("display", tag);
		return this;
	}

	public String[] getLore() {
		NBTTagCompound tag = itemStack.tag;
		NBTTagList list = tag.getCompound("display").getList("Lore");
		ArrayList<String> strings = new ArrayList<String>();
		String[] lores = new String[] {};
		for (int i = 0; i < strings.size(); i++)
			strings.add(((NBTTagString) list.get(i)).data);
		strings.toArray(lores);
		return lores;
	}

	public ItemStack getItemStack() {
		return CraftItemStack.asBukkitCopy(itemStack);
	}
}