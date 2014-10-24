package org.spacehq.mc.protocol1_8.data.game.values.entity;

public class ProjectileData implements ObjectData {

	private int ownerId;

	public ProjectileData(int ownerId) {
		this.ownerId = ownerId;
	}

	public int getOwnerId() {
		return this.ownerId;
	}

}
