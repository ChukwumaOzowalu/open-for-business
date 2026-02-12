package com.hiipower.openforbusiness;

import net.minecraft.commands.Commands;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber(modid = OpenForBusiness.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ofb_spawn_customer")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            var level = ctx.getSource().getLevel();
                            var p = ctx.getSource().getPosition();

                            var customer = ModEntities.CUSTOMER.get().create(level);
                            if (customer != null) {
                                customer.moveTo(p.x, p.y, p.z, 0.0f, 0.0f);
                                level.addFreshEntity(customer);
                                ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Spawned customer."), true);
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Failed to create customer entity."));
                                return 0;
                            }
                        })
        );
    }
}
