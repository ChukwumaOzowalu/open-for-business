package com.hiipower.openforbusiness.block.entity;

import com.hiipower.openforbusiness.ModBlockEntities;
import com.hiipower.openforbusiness.menu.LedgerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LedgerBlockEntity extends BlockEntity implements MenuProvider {

    // Slot 0 = the item you are currently pricing
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<ItemStackHandler> itemCap = LazyOptional.empty();

    // item registry name -> price (currency per item)
    private final Map<String, Integer> priceMap = new HashMap<>();

    public LedgerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEDGER_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Ledger");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new LedgerMenu(containerId, playerInv, this);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getPriceFor(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return priceMap.getOrDefault(key.toString(), 0);
    }

    public void setPriceFor(ItemStack stack, int newPrice) {
        if (stack.isEmpty()) return;
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        priceMap.put(key.toString(), Math.max(0, newPrice));
        setChanged();

        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
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

        CompoundTag pricesTag = new CompoundTag();
        for (var e : priceMap.entrySet()) {
            pricesTag.putInt(e.getKey(), e.getValue());
        }
        tag.put("prices", pricesTag);

        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inv")) {
            items.deserializeNBT(tag.getCompound("inv"));
        }
        priceMap.clear();
        if (tag.contains("prices")) {
            CompoundTag pricesTag = tag.getCompound("prices");
            for (String key : pricesTag.getAllKeys()) {
                priceMap.put(key, pricesTag.getInt(key));
            }
        }
    }

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
