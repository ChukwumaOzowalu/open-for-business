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
import com.hiipower.openforbusiness.entity.CustomerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
            // Spawn one customer near the sign (v1)
            CustomerEntity customer = ModEntities.CUSTOMER.get().create(serverLevel);
            if (customer != null) {
                customer.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);

                // Tell the customer where the sign is (so they can walk back and despawn)
                customer.setShopSignPos(pos);

                // Optional: bind customer to nearest ledger so they only use shelves/registers for that shop
                BlockPos ledgerPos = findNearestLedger(serverLevel, pos, 12);
                customer.setLedgerPos(ledgerPos);

                serverLevel.addFreshEntity(customer);
            }
        }
        return InteractionResult.SUCCESS;
    }
    private static BlockPos findNearestLedger(Level level, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    var be = level.getBlockEntity(p);
                    if (be instanceof com.hiipower.openforbusiness.block.entity.LedgerBlockEntity) {
                        double d = origin.distSqr(p);
                        if (d < bestDist) {
                            bestDist = d;
                            best = p.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }
}
