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
import superhb.arcademod.client.tileentity.BlockEntityArcade;
import superhb.arcademod.util.EnumGame;

import javax.annotation.Nullable;

public class ItemBlockArcade extends BlockItem {
    public ItemBlockArcade(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            int id = -1;
            if (tag.contains("Game")) {
                id = tag.getInt("Game");
            } else if (tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Game")) {
                id = tag.getCompound("BlockEntityTag").getInt("Game");
            }

            if (id >= 0 && id < EnumGame.values().length) {
                return Component.translatable("block.arcademod.arcade_machine." + EnumGame.values()[id].getSerializedName());
            }
        }
        return super.getName(stack);
    }
}
