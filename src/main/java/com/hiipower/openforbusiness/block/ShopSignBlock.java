package com.hiipower.openforbusiness.block;

import com.hiipower.openforbusiness.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ShopSignBlock extends Block {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public ShopSignBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                Player player, InteractionHand hand, BlockHitResult hit) {
       if (level.isClientSide) return InteractionResult.SUCCESS;

        boolean nowOpen = !state.getValue(OPEN);
        level.setBlock(pos, state.setValue(OPEN, nowOpen), 3);
        player.sendSystemMessage(Component.literal("Shop is now " + (nowOpen ? "OPEN" : "CLOSED")));

        if (nowOpen && level instanceof ServerLevel serverLevel) {
        // Spawn one customer near the sign (v0)
            var customer = ModEntities.CUSTOMER.get().create(serverLevel);
            if (customer != null) {
                customer.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
                serverLevel.addFreshEntity(customer);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
