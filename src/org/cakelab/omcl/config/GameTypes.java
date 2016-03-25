package org.cakelab.omcl.config;


public enum GameTypes {
	CLIENT("Client"),
	SERVER("Server");
	
	private String name;

	GameTypes(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public static GameTypes get(String name) {
		for (GameTypes v : GameTypes.values()) {
			if (name.equals(v.toString())) return v;
		}
		throw new IllegalArgumentException("No enum constant found for '" + name +'"');
	}

}
