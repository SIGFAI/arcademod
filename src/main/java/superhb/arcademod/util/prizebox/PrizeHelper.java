package superhb.arcademod.util.prizebox;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class PrizeHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ItemStack getItemStack(JsonObject object) {
        String resourceName = GsonHelper.getAsString(object, "item");
        ResourceLocation rl = new ResourceLocation(resourceName);
        Item item = ForgeRegistries.ITEMS.getValue(rl);

        if (item == null || item == Items.AIR) {
            throw new JsonSyntaxException(String.format("Unknown item '%s'", resourceName));
        }

        if (object.has("nbt")) {
            try {
                JsonElement element = object.get("nbt");
                CompoundTag nbt;

                if (element.isJsonObject()) {
                    nbt = TagParser.parseTag(GSON.toJson(element));
                } else {
                    nbt = TagParser.parseTag(element.getAsString());
                }

                ItemStack stack = new ItemStack(item, 1);
                stack.setTag(nbt);
                return stack;
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT Entry: " + e.toString());
            }
        }
        return new ItemStack(item, 1);
    }
}
