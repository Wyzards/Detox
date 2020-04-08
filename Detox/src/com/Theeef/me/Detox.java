package com.Theeef.me;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

public class Detox extends JavaPlugin implements Listener {

	public static Detox plugin;

	public void onEnable() {
		loadConfig();

		plugin = getPlugin(Detox.class);

		getServer().getPluginManager().registerEvents(this, this);
		getCommand("detox").setExecutor(new DetoxCommand());

		getServer().getConsoleSender().sendMessage(ChatColor.RESET + "Detox Enabled");
	}

	@EventHandler
	public void processCommand(PlayerCommandPreprocessEvent event) {
		if (!event.getMessage().contains(" ") || !isToxic(event.getPlayer()))
			return;

		List<String> list = Lists.newArrayList("whisper", "w", "msg", "r", "reply");
		String message = event.getMessage();
		String command = message.substring(1, message.contains(" ") ? message.indexOf(" ") : message.length())
				.toLowerCase();
		String recipient = message.substring(message.indexOf(" ") + 1, StringUtils.ordinalIndexOf(message, " ", 2) != -1
				? StringUtils.ordinalIndexOf(message, " ", 2) : message.length() - 1);
		UUID uuid = getUUID(recipient);

		if (list.contains(command) && uuid != null && isDetoxed(uuid)) {
			event.getPlayer().sendMessage(getColor(1)
					+ "You've been marked as toxic, and the player you're trying to message has toxic messages disabled. They must do /detox to enable them.");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerTalk(AsyncPlayerChatEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		List<Player> list = getOnlineToxic();
		list.addAll(getOnlineNotDetoxed());

		if (isToxic(uuid) || !isDetoxed(uuid)) {
			event.getRecipients().retainAll(list);
			event.setFormat(ChatColor.DARK_GREEN + "[Detox] " + ChatColor.RESET + event.getFormat());
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		if (!updateUser(event.getPlayer()))
			detox(event.getPlayer().getUniqueId(), true);

		if (isToxic(event.getPlayer()))
			toxicMessage(event.getPlayer().getUniqueId());
		else {
			if (!isDetoxed(event.getPlayer()))
				event.getPlayer().sendMessage(getColor(1)
						+ "You are currently in the unmoderated (detoxed) chat. Feel free to be toxic. Note that only other detoxed players can see your messages.");
			else
				event.getPlayer().sendMessage(getColor(1)
						+ "You are currently in the moderated chat. No toxicity please. If you'd like to be toxic, enter detoxed chat with /detox");
		}
	}

	@EventHandler
	public void clickToxicGUI(InventoryClickEvent event) {
		if (event.getClickedInventory() != null && event.getView().getTopInventory().getHolder() == null
				&& event.getView().getTitle().equals("Toxic Players")
				&& event.getClickedInventory().equals(event.getView().getTopInventory())) {
			Player player = (Player) event.getWhoClicked();
			ItemStack item = event.getCurrentItem();

			event.setCancelled(true);

			if (item != null && item.getType() == itemNextPage(1).getType()) {
				int page = Integer.parseInt(NBTHandler.getString(item, "page"));
				constructToxicList(player, page);
			}
		}

		if (event.getClickedInventory() != null && event.getView().getTitle().equals("Toxic Players")
				&& event.getView().getTopInventory().getHolder() == null
				&& !event.getClickedInventory().equals(event.getView().getTopInventory())
				&& (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT))
			event.setCancelled(true);
	}

	@EventHandler
	public void dragItem(InventoryDragEvent event) {
		if (event.getInventory() != null && event.getInventory().getHolder() == null
				&& event.getView().getTitle().equals("Toxic Players"))
			event.setCancelled(true);
	}

	public static void constructToxicList(Player player, int page) {
		List<UUID> list = getToxic();
		Inventory inventory = player.getServer().createInventory(null, 9 * 6, "Toxic Players");

		for (int i = (page - 1) * 45; i < page * 45; i++) {
			int index = i - (page - 1) * 45;

			if (index < list.size())
				inventory.addItem(toxicItem(list.get(index)));
			else
				break;
		}

		if (page > 1)
			inventory.setItem(45, itemPreviousPage(page - 1));
		if (page < list.size() / 45.0)
			inventory.setItem(45 + 8, itemNextPage(page + 1));

		player.openInventory(inventory);
	}

	public static ItemStack toxicItem(UUID uuid) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		String name = plugin.getConfig().getString("users." + uuid + ".toxic.markedBy");
		String date = plugin.getConfig().getString("users." + uuid + ".toxic.date");
		String reason = plugin.getConfig().getString("users." + uuid + ".toxic.reason");

		meta.setDisplayName(getColor(1) + getName(uuid));
		List<String> lore = Lists.newArrayList(getColor(2) + "Marked By: " + name, getColor(2) + "Date: " + date);
		if (reason != null)
			lore.add(getColor(2) + "Reason: " + reason);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
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

	/**
	 * 
	 * @param player
	 * @return if the player has played before
	 */
	public static boolean updateUser(Player player) {
		boolean value = plugin.getConfig().contains("users." + player.getUniqueId());
		plugin.getConfig().set("users." + player.getUniqueId() + ".name", player.getName());
		plugin.saveConfig();

		return value;
	}

	public static List<Player> getOnlineNotDetoxed() {
		List<Player> list = new ArrayList<Player>();

		for (Player player : Bukkit.getOnlinePlayers())
			if (!isDetoxed(player))
				list.add(player);

		return list;
	}

	public static List<Player> getOnlineToxic() {
		List<Player> list = new ArrayList<Player>();

		for (Player player : Bukkit.getOnlinePlayers())
			if (isToxic(player.getUniqueId()))
				list.add(player);

		return list;
	}

	public static List<UUID> getToxic() {
		List<UUID> list = new ArrayList<UUID>();
		List<String> list2 = new ArrayList<String>(plugin.getConfig().getConfigurationSection("users").getKeys(false));

		for (String uuid : list2)
			if (isToxic(UUID.fromString(uuid)))
				list.add(UUID.fromString(uuid));

		return list;
	}

	public static void markAsToxic(UUID uuid, CommandSender sender, String reason) {
		if (isToxic(uuid))
			plugin.getConfig().set("users." + uuid + ".toxic", null);

		else {
			plugin.getConfig().set("users." + uuid + ".toxic.reason", reason);
			plugin.getConfig().set("users." + uuid + ".toxic.date", (new Date()).toString());
			plugin.getConfig().set("users." + uuid + ".toxic.markedBy", sender.getName());

			if (!isDetoxed(uuid))
				detox(uuid, false);
		}

		plugin.saveConfig();

		toxicMessage(uuid);
	}

	public static void toxicMessage(UUID uuid) {
		String appealMessage = plugin.getConfig().getString("appealMessage");
		String reason = plugin.getConfig().contains("users." + uuid + ".toxic.reason")
				? plugin.getConfig().getString("users." + uuid + ".toxic.reason") : null;

		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (player.isOnline()) {
			if (isToxic(player.getPlayer()))
				player.getPlayer()
						.sendMessage(ChatColor.translateAlternateColorCodes('&',
								cc(1) + "You've been marked as toxic"
										+ (reason == null ? "" : " for '" + cc(2) + reason + cc(1) + "'")
										+ ". Only players in detox chat can see your messages. " + appealMessage));
			else
				player.getPlayer().sendMessage(getColor(1) + "You are no longer marked as toxic.");
		}
	}

	public static boolean isToxic(Player player) {
		return isToxic(player.getUniqueId());
	}

	public static boolean isToxic(UUID uuid) {
		if (plugin.getConfig().contains("users." + uuid + ".toxic"))
			return true;
		return false;
	}

	public static boolean isDetoxed(Player player) {
		return isDetoxed(player.getUniqueId());
	}

	public static boolean isDetoxed(UUID uuid) {
		return plugin.getConfig().contains("detoxed")
				&& plugin.getConfig().getStringList("detoxed").contains(uuid.toString());
	}

	public static List<String> getDetoxed() {
		return plugin.getConfig().contains("detoxed") ? plugin.getConfig().getStringList("detoxed")
				: new ArrayList<String>();
	}

	public static void detox(Player player, boolean nowEnabled) {
		detox(player.getUniqueId(), nowEnabled);
	}

	public static void detox(UUID uuid, boolean nowEnabled) {
		List<String> detoxed = getDetoxed();

		if (nowEnabled && !detoxed.contains(uuid.toString()))
			detoxed.add(uuid.toString());
		else if (!nowEnabled && detoxed.contains(uuid.toString()))
			detoxed.remove(uuid.toString());

		setDetoxed(detoxed);
	}

	public static String getName(UUID uuid) {
		return plugin.getServer().getOfflinePlayer(uuid).getName();
	}

	public static UUID getUUID(String name) {
		if (plugin.getConfig().contains("users"))
			for (String uuid : plugin.getConfig().getConfigurationSection("users").getKeys(false))
				if (plugin.getConfig().getString("users." + uuid + ".name").equalsIgnoreCase(name))
					return UUID.fromString(uuid);
		return null;
	}

	public static void setDetoxed(List<String> list) {
		plugin.getConfig().set("detoxed", list);
		plugin.saveConfig();
	}

	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public static ChatColor getColor(int colorNum) {
		if (colorNum == 1)
			return ChatColor.valueOf(plugin.getConfig().getString("colorOne"));
		else
			return ChatColor.valueOf(plugin.getConfig().getString("colorTwo"));
	}

	public static String cc(int color) {
		return "&" + getColor(color).getChar();
	}
}
