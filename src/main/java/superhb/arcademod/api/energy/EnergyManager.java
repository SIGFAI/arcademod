package superhb.arcademod.api.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyManager extends EnergyStorage implements INBTSerializable<Tag> {
    public EnergyManager(int capacity) {
        super(capacity);
    }

    public EnergyManager(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public EnergyManager(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public EnergyManager(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Energy", this.energy);
        tag.putInt("Capacity", this.capacity);
        tag.putInt("MaxReceive", this.maxReceive);
        tag.putInt("MaxExtract", this.maxExtract);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            this.energy = tag.getInt("Energy");
            this.capacity = tag.getInt("Capacity");
            this.maxReceive = tag.getInt("MaxReceive");
            this.maxExtract = tag.getInt("MaxExtract");
        }
    }
}
