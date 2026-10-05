package superhb.arcademod.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import superhb.arcademod.Reference;

public class ModRegistries {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MODID);

    // Sound Events
    public static final RegistryObject<SoundEvent> INSERT_COIN = SOUND_EVENTS.register("effect.insert", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.insert")));
    public static final RegistryObject<SoundEvent> TETROMINOES = SOUND_EVENTS.register("theme.tetris", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "theme.tetris")));
    public static final RegistryObject<SoundEvent> PACMAN_WAKA_1 = SOUND_EVENTS.register("effect.pacman_waka_1", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_waka_1")));
    public static final RegistryObject<SoundEvent> PACMAN_WAKA_2 = SOUND_EVENTS.register("effect.pacman_waka_2", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_waka_2")));
    public static final RegistryObject<SoundEvent> PACMAN_WAKA_3 = SOUND_EVENTS.register("effect.pacman_waka_3", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_waka_3")));
    public static final RegistryObject<SoundEvent> PACMAN_WAKA_4 = SOUND_EVENTS.register("effect.pacman_waka_4", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_waka_4")));
    public static final RegistryObject<SoundEvent> PACMAN_WAKA_5 = SOUND_EVENTS.register("effect.pacman_waka_5", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_waka_5")));
    public static final RegistryObject<SoundEvent> PACMAN_WAKA_6 = SOUND_EVENTS.register("effect.pacman_waka_6", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_waka_6")));
    public static final RegistryObject<SoundEvent> PACMAN_DEATH = SOUND_EVENTS.register("effect.pacman_death", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_death")));
    public static final RegistryObject<SoundEvent> PACMAN_FRUIT = SOUND_EVENTS.register("effect.pacman_fruit", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_fruit")));
    public static final RegistryObject<SoundEvent> PACMAN_INTRO = SOUND_EVENTS.register("theme.pacman", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "theme.pacman")));
    public static final RegistryObject<SoundEvent> PACMAN_GHOST = SOUND_EVENTS.register("effect.pacman_ghost", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_ghost")));
    public static final RegistryObject<SoundEvent> PACMAN_SIREN = SOUND_EVENTS.register("theme.pacman_siren", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "theme.pacman_siren")));
    public static final RegistryObject<SoundEvent> PACMAN_FRIGHT = SOUND_EVENTS.register("theme.pacman_fright", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "theme.pacman_fright")));
    public static final RegistryObject<SoundEvent> PACMAN_LIFE = SOUND_EVENTS.register("effect.pacman_life", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pacman_life")));
    public static final RegistryObject<SoundEvent> PACMAN_EAT = SOUND_EVENTS.register("theme.pacman_eat", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "theme.pacman_eat")));
    public static final RegistryObject<SoundEvent> SPACEINVADERS = SOUND_EVENTS.register("theme.spaceinvaders", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "theme.spaceinvaders")));
    public static final RegistryObject<SoundEvent> SPACEINVADERS_SHOOT = SOUND_EVENTS.register("effect.spaceinvaders_shoot", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.spaceinvaders_shoot")));
    public static final RegistryObject<SoundEvent> SPACEINVADERS_EXPLODE = SOUND_EVENTS.register("effect.spaceinvaders_explode", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.spaceinvaders_explode")));
    public static final RegistryObject<SoundEvent> SPACEINVADERS_DESTROYED = SOUND_EVENTS.register("effect.spaceinvaders_destroyed", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.spaceinvaders_destroyed")));
    public static final RegistryObject<SoundEvent> PONG_HIT = SOUND_EVENTS.register("effect.pong_hit", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pong_hit")));
    public static final RegistryObject<SoundEvent> PONG_MISS = SOUND_EVENTS.register("effect.pong_miss", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pong_miss")));
    public static final RegistryObject<SoundEvent> PONG_WALL = SOUND_EVENTS.register("effect.pong_wall", () -> SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(Reference.MODID, "effect.pong_wall")));

    // Items
    public static final RegistryObject<Item> COIN = ITEMS.register("coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TICKET = ITEMS.register("ticket", () -> new Item(new Item.Properties()));

    // Blocks
    public static final RegistryObject<Block> ARCADE_MACHINE = BLOCKS.register("arcade_machine", () -> new superhb.arcademod.client.blocks.BlockArcade(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)));
    public static final RegistryObject<Block> BLOCK_INVISIBLE = BLOCKS.register("invisible", () -> new superhb.arcademod.client.blocks.BlockInvisible(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.BARRIER)));
    public static final RegistryObject<Block> COIN_PUSHER = BLOCKS.register("coin_pusher", () -> new superhb.arcademod.client.blocks.BlockPusher(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)));
    public static final RegistryObject<Block> PLUSHIE = BLOCKS.register("plushie", () -> new superhb.arcademod.client.blocks.BlockPlushie(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.WHITE_WOOL)));
    public static final RegistryObject<Block> PRIZE_BOX = BLOCKS.register("prize_box", () -> new superhb.arcademod.client.blocks.BlockPrize(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)));

    public static final RegistryObject<Item> ARCADE_MACHINE_ITEM = ITEMS.register("arcade_machine", () -> new superhb.arcademod.client.items.ItemBlockArcade(ARCADE_MACHINE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COIN_PUSHER_ITEM = ITEMS.register("coin_pusher", () -> new BlockItem(COIN_PUSHER.get(), new Item.Properties()));
    public static final RegistryObject<Item> PLUSHIE_ITEM = ITEMS.register("plushie", () -> new superhb.arcademod.client.items.ItemBlockPlushie(PLUSHIE.get(), new Item.Properties()));
    public static final RegistryObject<Item> PRIZE_BOX_ITEM = ITEMS.register("prize_box", () -> new BlockItem(PRIZE_BOX.get(), new Item.Properties()));

    // Block Entities
    public static final RegistryObject<BlockEntityType<superhb.arcademod.client.tileentity.BlockEntityArcade>> BLOCK_ENTITY_ARCADE = BLOCK_ENTITIES.register("tile_arcade", () -> BlockEntityType.Builder.of(superhb.arcademod.client.tileentity.BlockEntityArcade::new, ARCADE_MACHINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<superhb.arcademod.client.tileentity.BlockEntityPusher>> BLOCK_ENTITY_PUSHER = BLOCK_ENTITIES.register("tile_pusher", () -> BlockEntityType.Builder.of(superhb.arcademod.client.tileentity.BlockEntityPusher::new, COIN_PUSHER.get()).build(null));
    public static final RegistryObject<BlockEntityType<superhb.arcademod.client.tileentity.BlockEntityPlushie>> BLOCK_ENTITY_PLUSHIE = BLOCK_ENTITIES.register("tile_plushie", () -> BlockEntityType.Builder.of(superhb.arcademod.client.tileentity.BlockEntityPlushie::new, PLUSHIE.get()).build(null));
    public static final RegistryObject<BlockEntityType<superhb.arcademod.client.tileentity.BlockEntityPrize>> BLOCK_ENTITY_PRIZE = BLOCK_ENTITIES.register("tile_prize", () -> BlockEntityType.Builder.of(superhb.arcademod.client.tileentity.BlockEntityPrize::new, PRIZE_BOX.get()).build(null));

    // Entities
    public static final RegistryObject<EntityType<superhb.arcademod.client.entity.EntityCamera>> CAMERA = ENTITY_TYPES.register("camera", () -> EntityType.Builder.<superhb.arcademod.client.entity.EntityCamera>of(superhb.arcademod.client.entity.EntityCamera::new, MobCategory.MISC).sized(0.1f, 0.1f).build("camera"));
    public static final RegistryObject<EntityType<superhb.arcademod.client.entity.EntityClaw>> CLAW = ENTITY_TYPES.register("claw", () -> EntityType.Builder.<superhb.arcademod.client.entity.EntityClaw>of(superhb.arcademod.client.entity.EntityClaw::new, MobCategory.MISC).sized(0.5f, 0.5f).build("claw"));

    public static final RegistryObject<CreativeModeTab> ARCADE_TAB = CREATIVE_MODE_TABS.register("arcade_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.arcademod"))
            .icon(() -> new ItemStack(COIN.get()))
            .displayItems((parameters, output) -> {
                for (superhb.arcademod.util.EnumGame game : superhb.arcademod.util.EnumGame.values()) {
                    ItemStack stack = new ItemStack(ARCADE_MACHINE_ITEM.get());
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("Game", game.getId());
                    CompoundTag beTag = new CompoundTag();
                    beTag.putInt("Game", game.getId());
                    tag.put("BlockEntityTag", beTag);
                    output.accept(stack);
                }
                // coin pusher intentionally omitted from the creative tab
                // output.accept(COIN_PUSHER_ITEM.get());
                for (superhb.arcademod.util.EnumMob mob : superhb.arcademod.util.EnumMob.values()) {
                    ItemStack stack = new ItemStack(PLUSHIE_ITEM.get());
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("Mob", mob.getId());
                    CompoundTag beTag = new CompoundTag();
                    beTag.putInt("Mob", mob.getId());
                    tag.put("BlockEntityTag", beTag);
                    output.accept(stack);
                }
                output.accept(PRIZE_BOX_ITEM.get());
                output.accept(COIN.get());
                output.accept(TICKET.get());
            }).build());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENU_TYPES.register(eventBus);
        SOUND_EVENTS.register(eventBus);
        ENTITY_TYPES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
