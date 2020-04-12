package com.Theeef.me;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;

public class DetoxCommand implements CommandExecutor, TabCompleter {

	public static final String permissions = ChatColor.RED + "Insufficient Permissions!";
	public static DetoxPlus plugin = DetoxPlus.getPlugin(DetoxPlus.class);

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {

		if (label.equalsIgnoreCase("detox")) {
			if (sender instanceof Player && args.length == 1 && args[0].equalsIgnoreCase("list")
					&& sender.hasPermission("detox.admin"))
				constructDetoxList((Player) sender, 1);
			else if (args.length >= 1 && sender.hasPermission("detox.admin"))
				tryDetox(args[0], args, sender);
			else if (sender instanceof Player)
				constructDetoxGUI((Player) sender);
			else
				sender.sendMessage(ChatColor.RED + "Only players can use that command!");

		}

		else if (label.equalsIgnoreCase("d")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;

				if (args.length == 0)
					player.sendMessage(DetoxPlus.baseColor() + "Proper Usage: /d <" + DetoxPlus.highlightColor()
							+ "message" + DetoxPlus.baseColor() + ">");
				else {
					boolean inDetox = DetoxPlus.getInDetoxChat(player);

					if (!inDetox)
						DetoxPlus.setInDetoxChat(player, true);

					((Player) sender).chat(concat(args));

					if (!inDetox)
						DetoxPlus.setInDetoxChat(player, false);
				}
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> list = new ArrayList<String>();

		if (sender.hasPermission("detox.admin")) {
			if (cmd.getName().equalsIgnoreCase("detox") && args.length == 1) {
				for (Player player : Bukkit.getOnlinePlayers())
					if (!player.hasPermission("detox.admin") && player.getName().toLowerCase().startsWith(args[0]))
						list.add(player.getName());

				Collections.sort(list);

				if ("list".startsWith(args[0].toLowerCase()))
					list.add(0, "list");
			}

			else if (cmd.getName().equalsIgnoreCase("detox") && args.length == 2
					&& plugin.getServer().getPlayer(args[0]) != null) {
				list.addAll(Lists.newArrayList("Hard-R", "Anti-LGBTQ Language", "Flaming"));
				Collections.sort(list);
			}
		}

		return list;
	}

	public void tryDetox(String name, String[] args, CommandSender sender) {
		UUID uuid = UserData.getUUID(name);
		UUID staff = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
		String reason = args.length > 1 ? "" : null;

		if (uuid == null)
			sender.sendMessage(ChatColor.RED + name
					+ " has never played here before, or has changed their name since their last login. Checking namemc.com if you believe their name has changed.");
		else if (plugin.getServer().getPlayer(uuid) != null
				&& plugin.getServer().getPlayer(uuid).hasPermission("detox.admin"))
			sender.sendMessage(ChatColor.RED + "That player cannot be detoxed.");
		else {
			for (int i = 1; i < args.length; i++)
				reason += " " + args[i];

			if (reason != null)
				reason = reason.trim();

			DetoxPlus.setDetoxed(uuid, !DetoxPlus.isDetoxed(uuid), reason, staff);
		}
	}

	public static void constructDetoxList(Player player, int page) {
		List<String> list = Lists.newArrayList(DetoxPlus.getDetoxed());
		Inventory inventory = player.getServer().createInventory(null, 9 * 6, "Detoxed Players");

		for (int i = (page - 1) * 45; i < page * 45; i++) {

			if (i >= list.size())
				break;

			inventory.addItem(toxicItem(UUID.fromString(list.get(i))));
		}

		if (page > 1)
			inventory.setItem(45, itemPreviousPage(page));
		if (page < list.size() / 45.0)
			inventory.setItem(45 + 8, itemNextPage(page));

		player.openInventory(inventory);
	}

	public static ItemStack itemNextPage(int page) {
		ItemStack item = new ItemStack(Material.SNOWBALL, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "Next Page");
		item.setItemMeta(meta);

		return NBTHandler.addString(item, "page", "" + (page + 1));
	}

	public static ItemStack itemPreviousPage(int page) {
		ItemStack item = new ItemStack(Material.SNOWBALL, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "Previous Page");
		item.setItemMeta(meta);

		return NBTHandler.addString(item, "page", "" + (page - 1));
	}

	public static ItemStack toxicItem(UUID uuid) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		String staff = plugin.getServer()
				.getOfflinePlayer(UUID.fromString(plugin.getConfig().getString("users." + uuid + ".detoxStatus.staff")))
				.getName();
		String date = plugin.getConfig().getString("users." + uuid + ".detoxStatus.date");
		String reason = plugin.getConfig().getString("users." + uuid + ".detoxStatus.reason");
		List<String> lore = Lists.newArrayList(
				DetoxPlus.baseColor() + "Marked by: " + DetoxPlus.highlightColor() + staff,
				DetoxPlus.baseColor() + "Date: " + DetoxPlus.highlightColor() + date);

		if (reason != null)
			lore.add(DetoxPlus.baseColor() + "Reason: " + DetoxPlus.highlightColor() + reason);

		meta.setLore(lore);
		meta.setDisplayName(DetoxPlus.highlightColor() + plugin.getServer().getOfflinePlayer(uuid).getName());
		item.setItemMeta(meta);

		return item;
	}

	public static void constructDetoxGUI(Player player) {
		Inventory inventory = player.getServer().createInventory(null, 9, "Detox Menu");

		if (!DetoxPlus.isDetoxed(player)) {
			inventory.addItem(itemMessageVisibility(player));
			inventory.addItem(itemModeratedChat(player));
		} else
			inventory.addItem(itemDetoxInfo(player));

		player.openInventory(inventory);
	}

	public static ItemStack itemDetoxInfo(Player player) {
		DetoxStatus detox = new DetoxStatus(player.getUniqueId());

		ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = Lists.newArrayList(
				DetoxPlus.baseColor() + "Marked by: " + DetoxPlus.highlightColor()
						+ (detox.staff == null ? "CONSOLE"
								: plugin.getServer().getOfflinePlayer(detox.staff).getName()),
				DetoxPlus.baseColor() + "Detoxed on: " + DetoxPlus.highlightColor() + detox.date);

		if (detox.reason != null)
			lore.add(ChatColor.GRAY + "Reason: " + DetoxPlus.highlightColor() + detox.reason);

		meta.setDisplayName(ChatColor.GREEN + "You are detoxed!");
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack itemMessageVisibility(Player player) {
		boolean visible = DetoxPlus.detoxVisible(player);
		ItemStack item = new ItemStack(visible ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
				1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(visible ? ChatColor.GREEN + "Toxicity is enabled" : ChatColor.RED + "Toxicity is disabled");
		meta.setLore(Lists.newArrayList(
				ChatColor.GRAY + "Click to " + (visible ? "hide" : "unhide") + " toxic messages in chat."));
		item.setItemMeta(meta);

		return NBTHandler.addString(item, "detoxVisibility", "tag");
	}

	public static ItemStack itemModeratedChat(Player player) {
		boolean inDetox = DetoxPlus.getInDetoxChat(player);
		ItemStack item = new ItemStack(inDetox ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
				1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(
				inDetox ? ChatColor.GREEN + "Speaking in Detox Chat" : ChatColor.RED + "Speaking in Public Chat");
		meta.setLore(
				Lists.newArrayList(ChatColor.GRAY + "Click to switch to " + (inDetox ? "Public" : "Detox") + " chat."));
		item.setItemMeta(meta);

		return NBTHandler.addString(item, "talkingInDetox", "tag");
	}

	public static String concat(String[] args) {
		String string = "";

		for (String stringg : args)
			string += stringg + " ";

		return string.trim();
	}
}
