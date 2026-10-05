package superhb.arcademod.client.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import superhb.arcademod.init.ModRegistries;

import javax.annotation.Nullable;

public class BlockEntityPlushie extends BlockEntity {
    private int mob = 0;
    private CompoundTag tagCompound;

    public BlockEntityPlushie(BlockPos pos, BlockState state) {
        super(ModRegistries.BLOCK_ENTITY_PLUSHIE.get(), pos, state);
        if (state.hasProperty(superhb.arcademod.client.blocks.BlockPlushie.MOB)) {
            this.mob = state.getValue(superhb.arcademod.client.blocks.BlockPlushie.MOB).getId();
        }
    }

    public int getMobID() {
        return mob;
    }

    public void setMobID(int id) {
        this.mob = id;
        setChanged();
    }
    
    public CompoundTag getTagCompound() {
        return tagCompound;
    }
    
    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("Mob", mob);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        tagCompound = compound;
        mob = compound.getInt("Mob");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
