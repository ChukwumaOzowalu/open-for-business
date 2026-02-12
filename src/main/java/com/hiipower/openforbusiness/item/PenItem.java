package com.hiipower.openforbusiness.item;

import com.hiipower.openforbusiness.block.entity.DisplayShelfBlockEntity;
import com.hiipower.openforbusiness.block.entity.LedgerBlockEntity;
import com.hiipower.openforbusiness.block.entity.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PenItem extends Item {

    private static final String TAG_LEDGER_POS = "ofb_selected_ledger";

    public PenItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;

        // We only do linking when sneaking; otherwise let normal interactions happen
        if (ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = ctx.getItemInHand();
        BlockPos clickedPos = ctx.getClickedPos();
        BlockEntity be = ctx.getLevel().getBlockEntity(clickedPos);

        // 1) Click Ledger -> select it
        if (be instanceof LedgerBlockEntity) {
            setSelectedLedger(stack, clickedPos);
            ctx.getPlayer().sendSystemMessage(Component.literal("Pen selected Ledger: " + clickedPos.toShortString()));
            return InteractionResult.SUCCESS;
        }

        // 2) Click other OFB blocks -> bind to selected ledger
        BlockPos selected = getSelectedLedger(stack);
        if (selected == null) {
            ctx.getPlayer().sendSystemMessage(Component.literal("No Ledger selected. Sneak-right-click a Ledger with the Pen first."));
            return InteractionResult.SUCCESS;
        }

        if (be instanceof RegisterBlockEntity reg) {
            reg.bindLedger(selected);
            ctx.getPlayer().sendSystemMessage(Component.literal("Register bound to Ledger: " + selected.toShortString()));
            return InteractionResult.SUCCESS;
        }

        if (be instanceof DisplayShelfBlockEntity shelf) {
            shelf.bindLedger(selected);
            ctx.getPlayer().sendSystemMessage(Component.literal("Shelf bound to Ledger: " + selected.toShortString()));
            return InteractionResult.SUCCESS;
        }

        // (Optional) Shop sign binding later when it becomes a block entity
        ctx.getPlayer().sendSystemMessage(Component.literal("That block can't be linked."));
        return InteractionResult.SUCCESS;
    }

    private static void setSelectedLedger(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_LEDGER_POS, pos.asLong());
    }

    private static BlockPos getSelectedLedger(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_LEDGER_POS)) return null;
        return BlockPos.of(tag.getLong(TAG_LEDGER_POS));
    }
}
