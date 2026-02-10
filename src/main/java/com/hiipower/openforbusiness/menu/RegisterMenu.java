package com.hiipower.openforbusiness.menu;

import com.hiipower.openforbusiness.ModMenus;
import com.hiipower.openforbusiness.block.entity.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.world.entity.player.Player;

public class RegisterMenu extends AbstractContainerMenu {

    private final RegisterBlockEntity be;
    private final Level level;

    // Client constructor (reads pos from packet)
    public RegisterMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBE(playerInv, extraData));
    }

    // Server constructor
    public RegisterMenu(int containerId, Inventory playerInv, RegisterBlockEntity be) {
        super(ModMenus.REGISTER_MENU.get(), containerId);
        this.be = be;
        this.level = playerInv.player.level();

        // Vanilla hopper GUI size is 176x133; we’ll position two slots inside it.
        // Slot 0 (input)
        addSlot(new SlotItemHandler(be.getItems(), 0, 44, 20));
        // Slot 1 (output) — for now, allow taking/placing; later we’ll restrict to take-only
        addSlot(new SlotItemHandler(be.getItems(), 1, 116, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // output is take-only
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                // When payout is taken, consume the same amount of items from input (slot 0)
                var handler = be.getItems();
                int toConsume = stack.getCount(); // 1 emerald per 1 item in v0
                handler.extractItem(0, toConsume, false);

                super.onTake(player, stack);
            }
        });

        // Player inventory (3 rows)
        int startX = 8;
        int startY = 51;
        int slotSize = 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * slotSize, startY + row * slotSize));
            }
        }

        // Hotbar
        int hotbarY = startY + 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, startX + col * slotSize, hotbarY));
        }
    }

    private static RegisterBlockEntity getBE(Inventory playerInv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInv.player.level();
        var be = level.getBlockEntity(pos);
        if (be instanceof RegisterBlockEntity r) return r;
        throw new IllegalStateException("RegisterBlockEntity not found at " + pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return be.getLevel() != null
                && be.getLevel().getBlockEntity(be.getBlockPos()) == be
                && player.distanceToSqr(be.getBlockPos().getX() + 0.5, be.getBlockPos().getY() + 0.5, be.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return empty;

        ItemStack stackInSlot = slot.getItem();
        ItemStack original = stackInSlot.copy();

        int containerSlots = 2; // BE slots: 0=input, 1=output

        if (index < containerSlots) {
            // Moving from BE -> player inventory
            if (!this.moveItemStackTo(stackInSlot, containerSlots, this.slots.size(), true)) {
                return empty;
            }

            // ✅ If this was the OUTPUT slot, consume matching input for the amount actually moved
            if (index == 1) {
                int movedCount = original.getCount() - stackInSlot.getCount(); // how many emeralds actually moved
                if (movedCount > 0) {
                    be.getItems().extractItem(0, movedCount, false); // 1 emerald per 1 item in v0/v1
                }
            }
        } else {
            // Moving from player -> BE (prefer input slot 0)
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                return empty;
            }
        }

        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return original;
    }

}
