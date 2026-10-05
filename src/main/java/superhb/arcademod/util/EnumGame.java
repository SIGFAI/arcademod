package superhb.arcademod.util;

import net.minecraft.util.StringRepresentable;

public enum EnumGame implements StringRepresentable {
	SNAKE(0, "snake"),
	TETROMINOES(1, "tetrominoes"),
	PACMAN(2, "pacman"),
	PONG(3, "pong"),
	SPACEINVADERS(4, "spaceinvaders"),
	//DDR(4, "ddr");
	KONG(5, "kong");
	
	private int id;
	private String registryName;
	
	EnumGame (int id, String registryName) {
		this.id = id;
		this.registryName = registryName;
	}
	
	@Override
	public String getSerializedName () {
		return registryName;
	}
	
	public int getId () {
		return id;
	}
	
	public static EnumGame getValue (int id) {
		return values()[id];
	}
	
	public static String getRegistryName (int id) {
		return values()[id].registryName;
	}
}
