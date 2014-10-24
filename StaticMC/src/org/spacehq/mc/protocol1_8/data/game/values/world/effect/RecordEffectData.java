package org.spacehq.mc.protocol1_8.data.game.values.world.effect;

public class RecordEffectData implements WorldEffectData {

	private int recordId;

	public RecordEffectData(int recordId) {
		this.recordId = recordId;
	}

	public int getRecordId() {
		return this.recordId;
	}

}
