package org.spacehq.mc.protocol1_8.data.game.values.world;

public class CustomSound implements Sound {

	private String name;

	public CustomSound(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
