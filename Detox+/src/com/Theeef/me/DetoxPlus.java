package com.Theeef.me;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DetoxPlus extends JavaPlugin implements Listener {

	public static DetoxPlus plugin;

	public void onEnable() {
		plugin = DetoxPlus.getPlugin(DetoxPlus.class);

		getServer().getPluginManager().registerEvents(new Events(), this);

		loadConfig();

		getServer().getPluginCommand("detox").setExecutor(new DetoxCommand());
		getServer().getPluginCommand("d").setExecutor(new DetoxCommand());
		getServer().getPluginCommand("clearchat").setExecutor(new ClearChat());
	}

	public static Set<String> getDetoxed() {
		return Sets.newHashSet(plugin.getConfig().getStringList("detoxed"));
	}

	public static Set<Player> getPlayersDetoxVisible() {
		Set<Player> players = new HashSet<Player>();

		for (Player player : Bukkit.getOnlinePlayers())
			if (detoxVisible(player))
				players.add(player);

		return players;
	}

	public static void setDetoxed(UUID uuid, boolean detoxed, String reason, UUID staffUUID) {
		String base = "users." + uuid + ".detoxStatus.";
		Set<String> detoxSet = getDetoxed();
		OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
		Player staff = staffUUID == null ? null : plugin.getServer().getPlayer(staffUUID);

		setDetoxVisibile(uuid, true);
		setInDetoxChat(uuid, true);

		plugin.getConfig().set(base + "detoxed", detoxed);
		plugin.getConfig().set(base + "staff", staffUUID == null ? null : staffUUID.toString());
		plugin.getConfig().set(base + "reason", reason);
		plugin.getConfig().set(base + "date", (new Date()).toString());

		if (detoxed) {
			detoxSet.add(player.getUniqueId().toString());

			for (Player online : Bukkit.getOnlinePlayers()) {
				String string = "been detoxed by " + highlightColor() + (staff == null ? "CONSOLE" : staff.getName())
						+ (reason == null ? ""
								: baseColor() + " for \"" + highlightColor() + reason + baseColor() + "\"")
						+ baseColor() + ".";

				if (player.getPlayer() != null && online.equals(player.getPlayer()))
					online.sendMessage(baseColor() + "You have " + string + baseColor()
							+ " Only players with toxic messages enabled can see you talk now.");
				else
					online.sendMessage(highlightColor() + player.getName() + baseColor() + " has " + string
							+ " If you would like to see toxic messages, enable them using " + highlightColor()
							+ "/detox" + baseColor() + ".");
			}
		}

		else {
			detoxSet.remove(player.getUniqueId().toString());

			for (Player online : Bukkit.getOnlinePlayers())
				online.sendMessage(highlightColor() + (staff == null ? "CONSOLE" : staff.getName()) + baseColor()
						+ " has undetoxed " + highlightColor() + player.getName());
		}

		plugin.getConfig().set("detoxed", Lists.newArrayList(detoxSet));
		plugin.saveConfig();

		if (player.getPlayer() != null && player.getPlayer().getOpenInventory().getTitle().equals("Detox Menu")
				&& player.getPlayer().getOpenInventory().getTopInventory().getHolder() == null)
			DetoxCommand.constructDetoxGUI(player.getPlayer());
	}

	public static boolean isDetoxed(Player player) {
		return isDetoxed(player.getUniqueId());
	}

	public static boolean isDetoxed(UUID uuid) {
		return plugin.getConfig().contains("users." + uuid + ".detoxStatus.detoxed")
				? plugin.getConfig().getBoolean("users." + uuid + ".detoxStatus.detoxed") : false;
	}

	public static void setDetoxVisibile(Player player, boolean visible) {
		setDetoxVisibile(player.getUniqueId(), visible);
	}

	public static void setDetoxVisibile(UUID uuid, boolean visible) {
		plugin.getConfig().set("users." + uuid + ".detoxVisibile", visible);
		plugin.saveConfig();
	}

	public static boolean detoxVisible(Player player) {
		return detoxVisible(player.getUniqueId());
	}

	public static boolean detoxVisible(UUID uuid) {
		return plugin.getConfig().contains("users." + uuid + ".detoxVisibile")
				? plugin.getConfig().getBoolean("users." + uuid + ".detoxVisibile") : false;
	}

	public static void setInDetoxChat(Player player, boolean value) {
		setInDetoxChat(player.getUniqueId(), value);
	}

	public static void setInDetoxChat(UUID uuid, boolean value) {
		plugin.getConfig().set("users." + uuid + ".inDetoxChat", value);
		plugin.saveConfig();
	}

	public static boolean getInDetoxChat(Player player) {
		return plugin.getConfig().contains("users." + player.getUniqueId() + ".inDetoxChat")
				? plugin.getConfig().getBoolean("users." + player.getUniqueId() + ".inDetoxChat") : false;
	}

	public static ChatColor baseColor() {
		try {
			return ChatColor.valueOf(plugin.getConfig().getString("baseColor"));
		} catch (EnumConstantNotPresentException e) {
			return ChatColor.MAGIC;
		}
	}

	public static ChatColor highlightColor() {
		try {
			return ChatColor.valueOf(plugin.getConfig().getString("highlightColor"));
		} catch (EnumConstantNotPresentException e) {
			return ChatColor.MAGIC;
		}
	}

	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
}
