package com.Theeef.me;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

public class Events implements Listener {

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

		if (!DetoxPlus.isDetoxed(event.getPlayer()) && DetoxPlus.detoxVisible(event.getPlayer()))
			event.setFormat(event.getFormat().replace("%1$s", "%1$s" + ChatColor.GREEN + "*" + ChatColor.RESET));
	}
}
