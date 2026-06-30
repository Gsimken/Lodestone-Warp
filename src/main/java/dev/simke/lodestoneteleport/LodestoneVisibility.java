package dev.simke.lodestoneteleport;

import java.util.Locale;

public enum LodestoneVisibility {
	PRIVATE("private"),
	DISCOVERABLE("discoverable"),
	GLOBAL("global");

	private final String id;

	LodestoneVisibility(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static LodestoneVisibility from(String value, LodestoneVisibility fallback) {
		if (value == null) {
			return fallback;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "private", "personal" -> PRIVATE;
			case "global" -> GLOBAL;
			case "discoverable", "public" -> DISCOVERABLE;
			default -> fallback;
		};
	}
}
