package com.hiipower.openforbusiness;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, OpenForBusiness.MODID);

    public static final RegistryObject<Block> REGISTER = BLOCKS.register("register",
            () -> new RegisterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
            )
    );

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
