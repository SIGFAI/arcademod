package superhb.arcademod.client.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import superhb.arcademod.init.ModRegistries;

public class BlockEntityPusher extends BlockEntity {
    public BlockEntityPusher(BlockPos pos, BlockState state) {
        super(ModRegistries.BLOCK_ENTITY_PUSHER.get(), pos, state);
    }
}
