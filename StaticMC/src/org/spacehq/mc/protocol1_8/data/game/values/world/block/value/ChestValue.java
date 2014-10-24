package org.spacehq.mc.protocol1_8.data.game.values.world.block.value;

public class ChestValue implements BlockValue {

	private int viewers;

	public ChestValue(int viewers) {
		this.viewers = viewers;
	}

	public int getViewers() {
		return this.viewers;
	}

}
