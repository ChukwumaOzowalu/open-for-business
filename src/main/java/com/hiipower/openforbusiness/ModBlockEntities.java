package com.hiipower.openforbusiness;

import com.hiipower.openforbusiness.block.entity.RegisterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, OpenForBusiness.MODID);

    public static final RegistryObject<BlockEntityType<RegisterBlockEntity>> REGISTER_BE =
            BLOCK_ENTITIES.register("register",
                    () -> BlockEntityType.Builder.of(RegisterBlockEntity::new, ModBlocks.REGISTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<com.hiipower.openforbusiness.block.entity.LedgerBlockEntity>> LEDGER_BE =
            BLOCK_ENTITIES.register("ledger",
                    () -> BlockEntityType.Builder.of(com.hiipower.openforbusiness.block.entity.LedgerBlockEntity::new,
                            ModBlocks.LEDGER.get()).build(null));

        public static final RegistryObject<BlockEntityType<com.hiipower.openforbusiness.block.entity.DisplayShelfBlockEntity>> DISPLAY_SHELF_BE =
                BLOCK_ENTITIES.register("display_shelf",
                        () -> BlockEntityType.Builder.of(
                                com.hiipower.openforbusiness.block.entity.DisplayShelfBlockEntity::new,
                                ModBlocks.DISPLAY_SHELF.get()
                        ).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
