package com.hiipower.openforbusiness;

import com.hiipower.openforbusiness.entity.CustomerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OpenForBusiness.MODID);

    public static final RegistryObject<EntityType<CustomerEntity>> CUSTOMER =
        ENTITIES.register("customer", () ->
                EntityType.Builder.of(CustomerEntity::new, MobCategory.MONSTER)
                        .sized(0.6f, 1.95f)
                        .build("customer")
        );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
