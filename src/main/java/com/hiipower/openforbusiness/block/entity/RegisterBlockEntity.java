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
import com.hiipower.openforbusiness.block.entity.LedgerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class RegisterBlockEntity extends BlockEntity implements MenuProvider {

    private BlockPos boundLedgerPos = null;

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

    private LedgerBlockEntity findNearbyLedger(int radius) {
        if (level == null) return null;

        BlockPos origin = getBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    var be = level.getBlockEntity(cursor);
                    if (be instanceof LedgerBlockEntity ledger) return ledger;
                }
            }
        }
        return null;
    }

    public void bindLedger(BlockPos ledgerPos) {
        this.boundLedgerPos = ledgerPos.immutable();
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getBoundLedgerPos() {
        return boundLedgerPos;
    }

    private LedgerBlockEntity getBoundLedger() {
        if (level == null || boundLedgerPos == null) return null;
        BlockEntity be = level.getBlockEntity(boundLedgerPos);
        return (be instanceof LedgerBlockEntity ledger) ? ledger : null;
    }

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
        if (boundLedgerPos != null) {
        tag.put("boundLedgerPos", NbtUtils.writeBlockPos(boundLedgerPos));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inv")) {
            items.deserializeNBT(tag.getCompound("inv"));
        }
        if (tag.contains("boundLedgerPos")) {
            boundLedgerPos = NbtUtils.readBlockPos(tag.getCompound("boundLedgerPos"));
        } else {
            boundLedgerPos = null;
        }
    }

    private void recalcPayout() {
        ItemStack input = items.getStackInSlot(0);

        if (input.isEmpty()) {
            items.setStackInSlot(1, ItemStack.EMPTY);
            return;
        }

        int count = Math.min(input.getCount(), 64);

        int pricePerItem = 1; // fallback
        LedgerBlockEntity ledger = getBoundLedger();
        if (ledger == null) {
            ledger = findNearbyLedger(16); // keep as fallback for now
        }
        if (ledger != null) {
            pricePerItem = ledger.getPriceFor(input);
        }

        int total = Math.max(0, pricePerItem * count);

        // For now, cap output to one stack so we don't deal with multi-stack payouts yet
        total = Math.min(total, 64);

        ItemStack payout = new ItemStack(Items.EMERALD, total);
        items.setStackInSlot(1, payout);
    }

}
