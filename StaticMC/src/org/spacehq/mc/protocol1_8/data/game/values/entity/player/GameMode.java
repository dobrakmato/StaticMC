package org.spacehq.mc.protocol1_8.data.game.values.entity.player;

import org.spacehq.mc.protocol1_8.data.game.values.world.notify.ClientNotificationValue;

public enum GameMode implements ClientNotificationValue {

	SURVIVAL,
	CREATIVE,
	ADVENTURE,
	SPECTATOR;

}
