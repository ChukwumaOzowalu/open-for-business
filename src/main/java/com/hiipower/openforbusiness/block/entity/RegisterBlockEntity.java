package com.hiipower.openforbusiness.block.entity;

import com.hiipower.openforbusiness.ModBlockEntities;
import com.hiipower.openforbusiness.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


import javax.annotation.Nullable;

public class RegisterBlockEntity extends BlockEntity implements MenuProvider {

    // Slot 0 = input item, Slot 1 = currency output
    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            // Only recalc when input changes
            if (slot == 0) {
                Level level = getLevel();
                if (level != null && !level.isClientSide) {
                    recalcPayout();
                }
            }
        }
    };

    private LazyOptional<ItemStackHandler> itemCap = LazyOptional.empty();

    public RegisterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REGISTER_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Register");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new com.hiipower.openforbusiness.menu.RegisterMenu(containerId, playerInv, this);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        itemCap = LazyOptional.of(() -> items);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inv", items.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inv")) {
            items.deserializeNBT(tag.getCompound("inv"));
        }
    }

    private void recalcPayout() {
        ItemStack input = items.getStackInSlot(0);

        // No input => clear payout
        if (input.isEmpty()) {
            items.setStackInSlot(1, ItemStack.EMPTY);
            return;
        }

        // Simple v0 pricing: 1 emerald per item (up to 64)
        int count = Math.min(input.getCount(), 64);
        ItemStack payout = new ItemStack(Items.EMERALD, count);

        items.setStackInSlot(1, payout);
    }
}
