package com.hiipower.openforbusiness.block.entity;

import com.hiipower.openforbusiness.ModBlockEntities;
import com.hiipower.openforbusiness.menu.DisplayShelfMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


import javax.annotation.Nullable;

public class DisplayShelfBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<ItemStackHandler> itemCap = LazyOptional.empty();

    private BlockPos boundLedgerPos = null;

    public DisplayShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISPLAY_SHELF_BE.get(), pos, state);
    }

    public boolean hasItem(Item item) {
        ItemStack stack = items.getStackInSlot(0);
        return !stack.isEmpty() && stack.getItem() == item;
    }

    public boolean takeOne(Item item) {
        ItemStack stack = items.getStackInSlot(0);
        if (!stack.isEmpty() && stack.getItem() == item) {
            items.extractItem(0, 1, false);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Display Shelf");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new DisplayShelfMenu(containerId, playerInv, this);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public void bindLedger(BlockPos ledgerPos) {
        this.boundLedgerPos = ledgerPos.immutable();
        setChanged();

        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getBoundLedgerPos() {
        return boundLedgerPos;
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
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inv", items.serializeNBT());
        if (boundLedgerPos != null) {
            tag.put("boundLedgerPos", NbtUtils.writeBlockPos(boundLedgerPos));
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inv")) items.deserializeNBT(tag.getCompound("inv"));
        boundLedgerPos = tag.contains("boundLedgerPos") ? NbtUtils.readBlockPos(tag.getCompound("boundLedgerPos")) : null;
    }

    // Sync to client
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
