package org.spacehq.mc.protocol1_8.data.message;

public enum ChatFormat {

	BOLD,
	UNDERLINED,
	STRIKETHROUGH,
	ITALIC,
	OBFUSCATED;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}

	public static ChatFormat byName(String name) {
		name = name.toLowerCase();
		for(ChatFormat format : values()) {
			if(format.toString().equals(name)) {
				return format;
			}
		}

		return null;
	}

}