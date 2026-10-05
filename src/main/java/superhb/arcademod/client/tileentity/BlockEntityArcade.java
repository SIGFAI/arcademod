package superhb.arcademod.client.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import superhb.arcademod.init.ModRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class BlockEntityArcade extends BlockEntity {
    private int game = 0;
    
    // Leaderboard
    private ArcadeLeaderboard[] leaderboard;
    
    // Energy
    private final EnergyStorage storage = new EnergyStorage(5000, 1000, 0);
    private final LazyOptional<EnergyStorage> energyOptional = LazyOptional.of(() -> storage);
    
    // Multiplayer
    private final ArrayList<String> playerList = new ArrayList<>();
    
    public BlockEntityArcade(BlockPos pos, BlockState state) {
        super(ModRegistries.BLOCK_ENTITY_ARCADE.get(), pos, state);
        if (state.hasProperty(superhb.arcademod.client.blocks.BlockArcade.GAME)) {
            this.game = state.getValue(superhb.arcademod.client.blocks.BlockArcade.GAME).getId();
        }
        leaderboard = new ArcadeLeaderboard[10];
    }
    
    public int getGameID() {
        return game;
    }
    
    public void setGameID(int id) {
        this.game = id;
        setChanged();
    }
    
    public void saveLeaderboard(ArcadeLeaderboard[] newLeaderboard) {
        this.leaderboard = newLeaderboard;
        setChanged();
    }
    
    public ArcadeLeaderboard[] getLeaderboard() {
        return leaderboard;
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityArcade entity) {
        if (!level.isClientSide) {
            // Power System Logic Here
        }
    }
    
    public void addPlayer(String name) {
        playerList.add(name);
    }
    
    public void removePlayer(String name) {
        playerList.remove(name);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Game", game);
        tag.putInt("Energy", storage.getEnergyStored());
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        game = tag.getInt("Game");
        storage.receiveEnergy(tag.getInt("Energy"), false);
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
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        energyOptional.invalidate();
    }
}
