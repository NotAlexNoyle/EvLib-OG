package plugin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProfile {

	private UUID uuid;
	private String name;
	private Map<String, String> properties = new HashMap<>();

	public PlayerProfile(UUID uuid, String name) {

		this.uuid = uuid;
		this.name = name;

	}

	public UUID getUuid() {

		return uuid;

	}

	public String getName() {

		return name;

	}

	public Map<String, String> getProperties() {

		return properties;

	}

	public void addProperty(String key, String value) {

		properties.put(key, value);

	}
}