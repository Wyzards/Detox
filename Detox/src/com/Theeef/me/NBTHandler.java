package com.Theeef.me;

import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;

import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class NBTHandler {

	public static NBTTagCompound getTag(org.bukkit.inventory.ItemStack item) {
		ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

		return nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
	}

	private static org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack item, NBTTagCompound tag) {
		ItemStack itemNms = CraftItemStack.asNMSCopy(item);
		itemNms.setTag(tag);
		return CraftItemStack.asBukkitCopy(itemNms);
	}

	public static org.bukkit.inventory.ItemStack addString(org.bukkit.inventory.ItemStack item, String name,
			String value) {
		NBTTagCompound tag = getTag(item);
		tag.setString(name, value);
		return setTag(item, tag);
	}

	public static boolean hasString(org.bukkit.inventory.ItemStack item, String name) {
		NBTTagCompound tag = getTag(item);
		return tag.hasKey(name);
	}

	public static String getString(org.bukkit.inventory.ItemStack item, String name) {
		NBTTagCompound tag = getTag(item);
		return tag.getString(name);
	}
}
