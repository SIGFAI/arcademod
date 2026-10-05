package superhb.arcademod.client.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import superhb.arcademod.init.ModRegistries;

import javax.annotation.Nullable;

public class BlockEntityPrize extends BlockEntity implements Nameable {
    private Component customName;

    public BlockEntityPrize(BlockPos pos, BlockState state) {
        super(ModRegistries.BLOCK_ENTITY_PRIZE.get(), pos, state);
    }

    public void setCustomName(Component name) {
        this.customName = name;
        setChanged();
    }

    @Override
    public Component getName() {
        return customName != null ? customName : Component.translatable("block.arcademod.prize_box");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.hasCustomName()) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
        }
    }
}
