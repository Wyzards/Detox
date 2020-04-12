package com.Theeef.me;

import java.util.Date;
import java.util.UUID;

import org.bukkit.entity.Player;

public class UserData {

	public static DetoxPlus plugin = DetoxPlus.getPlugin(DetoxPlus.class);

	public static void updateUser(Player player, boolean updateLogin, boolean updateLogout) {
		plugin.getConfig().set("users." + player.getUniqueId() + ".name", player.getName());

		if (updateLogin)
			plugin.getConfig().set("users." + player.getUniqueId() + ".lastLogin", (new Date()).toString());
		if (updateLogout)
			plugin.getConfig().set("users." + player.getUniqueId() + ".lastLogout", (new Date()).toString());

		plugin.saveConfig();
	}

	public static UUID getUUID(String name) {
		for (String uuid : plugin.getConfig().getConfigurationSection("users").getKeys(false))
			if (plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName().equalsIgnoreCase(name))
				return UUID.fromString(uuid);
		return null;
	}
}
