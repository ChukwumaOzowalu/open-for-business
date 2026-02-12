package com.hiipower.openforbusiness.client;

import com.hiipower.openforbusiness.ModEntities;
import com.hiipower.openforbusiness.OpenForBusiness;
import com.hiipower.openforbusiness.entity.CustomerEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = OpenForBusiness.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEntityRenderers {

    private static final ResourceLocation ZOMBIE_TEXTURE =
        new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.CUSTOMER.get(), ctx ->
                    new HumanoidMobRenderer<CustomerEntity, net.minecraft.client.model.HumanoidModel<CustomerEntity>>(
                            ctx,
                            new net.minecraft.client.model.HumanoidModel<CustomerEntity>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                            0.5f
                    ) {
                        @Override
                        public ResourceLocation getTextureLocation(CustomerEntity entity) {
                            return ZOMBIE_TEXTURE;
                        }
                    }
            );
        });
    }
}
