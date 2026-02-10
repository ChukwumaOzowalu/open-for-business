package com.hiipower.openforbusiness.block;

import com.hiipower.openforbusiness.ModBlocks;
import com.hiipower.openforbusiness.block.entity.DisplayShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;


public class DisplayShelfBlock extends BaseEntityBlock {

    public DisplayShelfBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisplayShelfBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DisplayShelfBlockEntity shelfBE)) return InteractionResult.PASS;

        // SHIFT-RIGHT-CLICK with Ledger block item to bind to nearest Ledger
        if (player.isShiftKeyDown()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(ModBlocks.LEDGER.get().asItem())) {
                BlockPos ledgerPos = findNearestLedger(level, pos, 6);
                if (ledgerPos != null) {
                    shelfBE.bindLedger(ledgerPos);
                    player.sendSystemMessage(Component.literal("Shelf bound to Ledger at " + ledgerPos.toShortString()));
                } else {
                    player.sendSystemMessage(Component.literal("No Ledger found nearby to bind."));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Normal right-click: open shelf UI
        if (be instanceof MenuProvider provider) {
            NetworkHooks.openScreen((ServerPlayer) player, provider, pos);
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
