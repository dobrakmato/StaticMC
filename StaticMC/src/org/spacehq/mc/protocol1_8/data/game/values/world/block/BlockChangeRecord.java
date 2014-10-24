package org.spacehq.mc.protocol1_8.data.game.values.world.block;

import org.spacehq.mc.protocol1_8.data.game.Position;

public class BlockChangeRecord {

	private Position position;
	private int block;

	public BlockChangeRecord(Position position, int block) {
		this.position = position;
		this.block = block;
	}

	public Position getPosition() {
		return this.position;
	}

	public int getBlock() {
		return this.block;
	}

}
