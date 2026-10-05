package superhb.arcademod.util;

import net.minecraft.util.StringRepresentable;

public enum EnumMob implements StringRepresentable {
    /* Mob List
        - Bat
        - Blaze
        - Chicken
        - Cow
        - Donkey
        - Elder Guardian
        - Enderman
        - Endermite
        - Villager + Involker + Witch
        - Ghast
        - Guardian
        - Horse
        - Husk
        - Llama
        - Magma Cube
        - Mooshroom
        - Mule
        - Ocelot
        - Polar Bear
        - Rabbit
        - Sheep
        - Shulker
        - Silverfish
        - Skeleton
        - Skeleton Horse
        - Slime
        - Spider
        - Squid
        - Stray
        - Vex
        - Vindicator
        - Wither Skeleton
        - Wither
        - Wolf
        - Zombie
        - Zombie Horse
        - Zombie Pigman
        - Zombie Villager
     */
    CREEPER(0, "creeper"),
    PIG(1, "pig");


    private int id;
    private String name;

    EnumMob (int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getSerializedName () {
        return name;
    }

    public int getId () {
    	return id;
    }
    
    public static EnumMob getValue (int id) {
        return values()[id];
    }

    public static String getName (int id) {
        return values()[id].name;
    }
}
