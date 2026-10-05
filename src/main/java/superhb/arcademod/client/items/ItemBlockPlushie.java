package superhb.arcademod.client.items;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import superhb.arcademod.client.tileentity.BlockEntityPlushie;
import superhb.arcademod.util.EnumMob;

import javax.annotation.Nullable;

public class ItemBlockPlushie extends BlockItem {
    public ItemBlockPlushie(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            int id = -1;
            if (tag.contains("Mob")) {
                id = tag.getInt("Mob");
            } else if (tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Mob")) {
                id = tag.getCompound("BlockEntityTag").getInt("Mob");
            }

            if (id >= 0 && id < EnumMob.values().length) {
                return Component.translatable("block.arcademod.plushie." + EnumMob.values()[id].getSerializedName());
            }
        }
        return super.getName(stack);
    }
}
