package superhb.arcademod.client.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import superhb.arcademod.client.tileentity.BlockEntityArcade;
import superhb.arcademod.init.ModRegistries;
import superhb.arcademod.util.EnumGame;

import javax.annotation.Nullable;

public class BlockArcade extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<EnumGame> GAME = EnumProperty.create("game", EnumGame.class);

    private static final VoxelShape SHAPE_NORTH = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_WEST = Block.box(0.0D, 0.0D, 1.0D, 14.0D, 16.0D, 15.0D);
    private static final VoxelShape SHAPE_EAST = Block.box(2.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D);

    public BlockArcade(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(GAME, EnumGame.SNAKE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, GAME);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityArcade(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH: return SHAPE_NORTH;
            case SOUTH: return SHAPE_SOUTH;
            case WEST: return SHAPE_WEST;
            case EAST: return SHAPE_EAST;
            default: return SHAPE_NORTH;
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        
        if (level.getBlockEntity(pos) instanceof BlockEntityArcade arcade) {
            arcade.setGameID(state.getValue(GAME).getId());
        }

        BlockPos above = pos.above();
        if (level.getBlockState(above).canBeReplaced()) {
            level.setBlock(above, ModRegistries.BLOCK_INVISIBLE.get().defaultBlockState().setValue(BlockInvisible.FACING, state.getValue(FACING)), 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        BlockPos above = pos.above();
        if (level.getBlockState(above).getBlock() instanceof BlockInvisible) {
            level.destroyBlock(above, false);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Prevent double-opening (main-hand + off-hand) which can look like the GUI instantly closes.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityArcade) {
            if (level.isClientSide) {
                // If an arcade GUI is already open, don't reopen it on repeated interactions.
                if (!(net.minecraft.client.Minecraft.getInstance().screen instanceof superhb.arcademod.api.gui.GuiArcade)) {
                    superhb.arcademod.client.ClientAccess.openArcadeGui(level, (BlockEntityArcade)tile, player);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModRegistries.BLOCK_ENTITY_ARCADE.get(), BlockEntityArcade::tick);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        ItemStack stack = context.getItemInHand();
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            int gameId = 0;
            if (tag.contains("Game")) {
                gameId = tag.getInt("Game");
            } else if (tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Game")) {
                gameId = tag.getCompound("BlockEntityTag").getInt("Game");
            }
            state = state.setValue(GAME, EnumGame.values()[gameId]);
        }
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityArcade arcade) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Game", arcade.getGameID());
            stack.setTag(tag);
        }
        return stack;
    }
}
