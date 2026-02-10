package com.hiipower.openforbusiness.menu;

import com.hiipower.openforbusiness.ModMenus;
import com.hiipower.openforbusiness.block.entity.DisplayShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.SlotItemHandler;

public class DisplayShelfMenu extends AbstractContainerMenu {

    private final DisplayShelfBlockEntity be;
    private final Level level;

    public DisplayShelfMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBE(playerInv, extraData));
    }

    public DisplayShelfMenu(int containerId, Inventory playerInv, DisplayShelfBlockEntity be) {
        super(ModMenus.DISPLAY_SHELF_MENU.get(), containerId);
        this.be = be;
        this.level = playerInv.player.level();

        // One shelf slot
        addSlot(new SlotItemHandler(be.getItems(), 0, 80, 20));

        // Player inventory
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

    private static DisplayShelfBlockEntity getBE(Inventory playerInv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInv.player.level();
        var be = level.getBlockEntity(pos);
        if (be instanceof DisplayShelfBlockEntity shelf) return shelf;
        throw new IllegalStateException("DisplayShelfBlockEntity not found at " + pos);
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

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        int containerSlots = 1;

        if (index < containerSlots) {
            if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) return empty;
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) return empty;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }
}
