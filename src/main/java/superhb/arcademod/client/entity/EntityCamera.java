package superhb.arcademod.client.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class EntityCamera extends Entity {
    public EntityCamera(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityCamera(Level level, double x, double y, double z) {
        this(superhb.arcademod.init.ModRegistries.CAMERA.get(), level);
        this.setPos(x + 0.5D, y + 2.0D, z - 0.5D);
        this.setXRot(45);
        this.setYRot(0);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
