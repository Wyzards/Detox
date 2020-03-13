package com.Theeef.me;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sun.istack.internal.NotNull;

import net.md_5.bungee.api.ChatColor;

public class DetoxCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command arg1, @NotNull String arg2,
			@NotNull String[] args) {

		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Detox.getColor(1) + "Only players can toggle detox mode!");
				return true;
			}

			Player player = (Player) sender;
			Detox.detox(player, !Detox.isDetoxed(player));

			if (Detox.isDetoxed(player))
				player.sendMessage(Detox.getColor(1) + "You can no longer see the messages of "
						+ Detox.getOnlineToxic().size() + " toxic players online.");
			else
				player.sendMessage(Detox.getColor(1) + "You can now see the messages of "
						+ Detox.getOnlineToxic().size() + " toxic players online.");
			return true;
		}

		if (!sender.hasPermission("detox.admin")) {
			sender.sendMessage(ChatColor.RED + "Insufficient Permissions");
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&r---------- " + Detox.cc(1) + "Detox Commands &r----------"));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&r- /" + Detox.cc(2) + "detox &7&oToggle visiblity of toxic players' messages."));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&r- /" + Detox.cc(2)
					+ "detox <player> <reason> &7&oMark a player as toxic for a period of time. Only a name is required. If already marked, this will unmark them."));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&r- /" + Detox.cc(2) + "detox list &7&oView a list of players marked as toxic."));
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			if (sender instanceof Player)
				Detox.constructToxicList((Player) sender, 1);
			else
				sender.sendMessage(ChatColor.RED + "Only players an use that command!");
			return true;
		}

		String reason = args.length == 1 ? null : "";
		UUID uuid = Detox.getUUID(args[0]);

		if (uuid == null) {
			sender.sendMessage(Detox.getColor(1) + args[0] + " has never played on the server before!");
			return true;
		}

		if (sender instanceof Player && ((Player) sender).getUniqueId().equals(uuid)
				&& !Detox.isToxic((Player) sender)) {
			// Z don't remove this or I'll cry.
			sender.sendMessage(Detox.getColor(1) + "You can't mark yourself, stupid...");
			return true;
		}

		for (int i = 1; i < args.length; i++)
			reason += " " + args[i];
		if (reason != null)
			reason = reason.trim();

		Detox.markAsToxic(uuid, sender, reason);

		if (Detox.isToxic(uuid)) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					Detox.cc(2) + Detox.getName(uuid) + Detox.cc(1) + " has been marked as toxic"
							+ (reason == null ? "." : " for '" + Detox.cc(2) + reason + Detox.cc(1) + "'.")
							+ " Other players have been informed."));

			for (Player player : Bukkit.getOnlinePlayers())
				if (!(sender instanceof Player) || (!((Player) sender).getUniqueId().equals(player.getUniqueId())
						&& !player.getUniqueId().equals(uuid)))
					player.sendMessage(Detox.getColor(2) + Detox.getName(uuid) + Detox.getColor(1)
							+ " has been marked as toxic. Their messages are now hidden from chat. To see their and other toxic player's messages, use /detox.");
		} else
			sender.sendMessage(
					Detox.getColor(2) + Detox.getName(uuid) + Detox.getColor(1) + " is no longer marked as toxic.");
		return true;
	}
}
