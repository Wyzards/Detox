package com.Theeef.me;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Events implements Listener {

	public static DetoxPlus plugin = DetoxPlus.getPlugin(DetoxPlus.class);

	@EventHandler
	public void processCommand(PlayerCommandPreprocessEvent event) {
		if (!event.getMessage().contains(" ") || !DetoxPlus.isDetoxed(event.getPlayer()))
			return;

		List<String> list = Lists.newArrayList("whisper", "w", "msg", "r", "reply");
		String message = event.getMessage();
		String command = message.substring(1, message.contains(" ") ? message.indexOf(" ") : message.length())
				.toLowerCase();
		String recipient = message.substring(message.indexOf(" ") + 1, StringUtils.ordinalIndexOf(message, " ", 2) != -1
				? StringUtils.ordinalIndexOf(message, " ", 2) : message.length() - 1);
		UUID uuid = UserData.getUUID(recipient);

		if (list.contains(command) && uuid != null && !DetoxPlus.detoxVisible(uuid)) {
			event.getPlayer().sendMessage(DetoxPlus.baseColor()
					+ "You've been marked as toxic, and the player you're trying to message has toxic messages disabled.");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void clickDetoxList(InventoryClickEvent event) {
		if (event.getView().getTitle().equals("Detoxed Players")
				&& event.getView().getTopInventory().getHolder() == null) {
			event.setCancelled(true);

			if (event.getCurrentItem() == null)
				return;

			ItemStack item = event.getCurrentItem();
			Player player = (Player) event.getWhoClicked();

			if (NBTHandler.hasString(item, "page"))
				DetoxCommand.constructDetoxList(player, Integer.parseInt(NBTHandler.getString(item, "page")));
		}
	}

	@EventHandler
	public void clickDetoxMenu(InventoryClickEvent event) {
		if (event.getView().getTitle().equals("Detox Menu") && event.getView().getTopInventory().getHolder() == null) {
			event.setCancelled(true);

			if (event.getCurrentItem() == null)
				return;

			ItemStack item = event.getCurrentItem();
			Player player = (Player) event.getWhoClicked();

			if (NBTHandler.hasString(item, "detoxVisibility")) {
				DetoxPlus.setDetoxVisibile(player, !DetoxPlus.detoxVisible(player));
				DetoxCommand.constructDetoxGUI(player);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			}

			else if (NBTHandler.hasString(item, "talkingInDetox")) {
				DetoxPlus.setInDetoxChat(player, !DetoxPlus.getInDetoxChat(player));
				DetoxCommand.constructDetoxGUI(player);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			}
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		UserData.updateUser(event.getPlayer(), true, false);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		UserData.updateUser(event.getPlayer(), false, true);
	}

	@EventHandler
	public void playerTalk(AsyncPlayerChatEvent event) {
		if (DetoxPlus.isDetoxed(event.getPlayer())) {
			event.getRecipients().retainAll(DetoxPlus.getPlayersDetoxVisible());
			event.setFormat(ChatColor.DARK_GREEN + "[Detoxed] " + ChatColor.RESET + event.getFormat());
		}

		else if (DetoxPlus.getInDetoxChat(event.getPlayer())) {
			Set<Player> set = Sets.newHashSet(event.getPlayer());
			set.addAll(DetoxPlus.getPlayersDetoxVisible());

			event.getRecipients().retainAll(set);
			event.setFormat(ChatColor.DARK_GREEN + "[Unmoderated] " + ChatColor.RESET + event.getFormat());
		}

		if (plugin.getConfig().getBoolean("detoxStar"))
			if (!DetoxPlus.isDetoxed(event.getPlayer()) && DetoxPlus.detoxVisible(event.getPlayer()))
				event.setFormat(event.getFormat().replace("%1$s", "%1$s" + ChatColor.GREEN + "*" + ChatColor.RESET));
	}
}
