package org.spacehq.mc.protocol1_8.data.game.values.world.effect;

public class BreakPotionEffectData implements WorldEffectData {

	private int potionId;

	public BreakPotionEffectData(int potionId) {
		this.potionId = potionId;
	}

	public int getPotionId() {
		return this.potionId;
	}

}
