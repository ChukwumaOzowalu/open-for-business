package com.hiipower.openforbusiness.menu;

import com.hiipower.openforbusiness.ModMenus;
import com.hiipower.openforbusiness.block.entity.LedgerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.SlotItemHandler;

public class LedgerMenu extends AbstractContainerMenu {

    private final LedgerBlockEntity be;
    private final Level level;

    // Client constructor
    public LedgerMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBE(playerInv, extraData));
    }

    // Server constructor
    public LedgerMenu(int containerId, Inventory playerInv, LedgerBlockEntity be) {
        super(ModMenus.LEDGER_MENU.get(), containerId);
        this.be = be;
        this.level = playerInv.player.level();

        // One slot: the item you're pricing
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

    private static LedgerBlockEntity getBE(Inventory playerInv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInv.player.level();
        var be = level.getBlockEntity(pos);
        if (be instanceof LedgerBlockEntity l) return l;
        throw new IllegalStateException("LedgerBlockEntity not found at " + pos);
    }

    public int getSelectedItemPrice() {
        return be.getPriceFor(be.getItems().getStackInSlot(0));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (level.isClientSide) return true;

        ItemStack target = be.getItems().getStackInSlot(0);
        if (target.isEmpty()) return true;

        int current = be.getPriceFor(target);

        if (id == 0) be.setPriceFor(target, current - 1);
        if (id == 1) be.setPriceFor(target, current + 1);

        return true;
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

        int containerSlots = 1; // ledger has 1 slot

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
