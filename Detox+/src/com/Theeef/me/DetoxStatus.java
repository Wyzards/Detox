package com.Theeef.me;

import java.util.UUID;

public class DetoxStatus {

	public static DetoxPlus plugin = DetoxPlus.getPlugin(DetoxPlus.class);

	String date;
	UUID staff;
	UUID uuid;
	String reason;

	public DetoxStatus(UUID uuid) {
		this.uuid = uuid;
		this.staff = UUID.fromString(plugin.getConfig().getString("users." + uuid + ".detoxStatus.staff"));
		this.reason = plugin.getConfig().getString("users." + uuid + ".detoxStatus.reason");
		this.date = plugin.getConfig().getString("users." + uuid + ".detoxStatus.date");
	}
}
