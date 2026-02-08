package com.hiipower.openforbusiness;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RegisterBlock extends Block {
    public RegisterBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, net.minecraft.core.BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            String heldName = held.isEmpty() ? "nothing" : held.getHoverName().getString();
            player.sendSystemMessage(Component.literal("Register: Hello! You're holding " + heldName + "."));
        }
        return InteractionResult.SUCCESS;
    }
}
