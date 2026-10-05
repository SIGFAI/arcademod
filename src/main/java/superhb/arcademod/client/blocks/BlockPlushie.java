package superhb.arcademod.client.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import superhb.arcademod.client.audio.ArcadeSounds;
import superhb.arcademod.client.tileentity.BlockEntityPlushie;
import superhb.arcademod.init.ModRegistries;
import superhb.arcademod.util.EnumMob;

import javax.annotation.Nullable;

public class BlockPlushie extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<EnumMob> MOB = EnumProperty.create("mob", EnumMob.class);

    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.0D, 12.0D);

    public BlockPlushie(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MOB, EnumMob.CREEPER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MOB);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityPlushie(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        net.minecraft.sounds.SoundEvent sound = state.getValue(MOB) == EnumMob.PIG ? net.minecraft.sounds.SoundEvents.PIG_AMBIENT : net.minecraft.sounds.SoundEvents.CREEPER_HURT;
        if (!level.isClientSide) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        
        if (level.getBlockEntity(pos) instanceof BlockEntityPlushie plushie) {
            plushie.setMobID(state.getValue(MOB).getId());
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        ItemStack stack = context.getItemInHand();
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            int mobId = 0;
            if (tag.contains("Mob")) {
                mobId = tag.getInt("Mob");
            } else if (tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Mob")) {
                mobId = tag.getCompound("BlockEntityTag").getInt("Mob");
            }
            state = state.setValue(MOB, EnumMob.values()[mobId]);
        }
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityPlushie plushie) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Mob", plushie.getMobID());
            stack.setTag(tag);
        }
        return stack;
    }
}
