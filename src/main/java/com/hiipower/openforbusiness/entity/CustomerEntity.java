package com.hiipower.openforbusiness.entity;

import com.hiipower.openforbusiness.block.entity.DisplayShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class CustomerEntity extends PathfinderMob {

    // --- Config (v1) ---
    private static final int MAX_LIFE_TICKS = 20 * 30; // 30s total lifetime
    private static final int SEARCH_RADIUS = 10;
    private static final double SPEED = 1.1;

    // Small “desired items” list for v1 (we’ll later make this dynamic)
    private static final List<Item> DESIRES = List.of(
            Items.BREAD, Items.APPLE, Items.COOKED_BEEF, Items.OAK_PLANKS, Items.TORCH
    );

    // --- Runtime state ---
    private int lifeTicks = MAX_LIFE_TICKS;

    private enum State { PICK_ITEM, GO_SHELF, GO_REGISTER, GO_SIGN, LEAVE }
    private State state = State.PICK_ITEM;

    private Item desiredItem = Items.BREAD;

    private @Nullable BlockPos shopSignPos = null;
    private @Nullable BlockPos ledgerPos = null;

    private @Nullable BlockPos targetShelfPos = null;
    private @Nullable BlockPos targetRegisterPos = null; // v1: just “nearest register” later; for now we can skip

    public CustomerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    // Called by ShopSignBlock after spawning
    public void setShopSignPos(BlockPos pos) { this.shopSignPos = pos.immutable(); }
    public void setLedgerPos(@Nullable BlockPos pos) { this.ledgerPos = (pos == null ? null : pos.immutable()); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0f));
        // Random strolling is fine once they’re done; during “work” we drive navigation directly.
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.8));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        lifeTicks--;
        if (lifeTicks <= 0) {
            this.discard();
            return;
        }

        switch (state) {
            case PICK_ITEM -> {
                desiredItem = DESIRES.get(this.random.nextInt(DESIRES.size()));

                // Find a shelf with the desired item
                targetShelfPos = findShelfWithItem(desiredItem);

                if (targetShelfPos != null) {
                    state = State.GO_SHELF;
                    this.getNavigation().moveTo(
                            targetShelfPos.getX() + 0.5, targetShelfPos.getY() + 0.5, targetShelfPos.getZ() + 0.5,
                            SPEED
                    );
                } else {
                    // No shelf has it: go “request” at register (v1: we’ll just message and leave)
                    state = State.GO_REGISTER;
                }
            }

            case GO_SHELF -> {
                if (targetShelfPos == null) {
                    state = State.GO_REGISTER;
                    return;
                }

                if (this.blockPosition().closerThan(targetShelfPos, 2.0)) {
                    // Attempt to take one item from shelf
                    var be = this.level().getBlockEntity(targetShelfPos);
                    if (be instanceof DisplayShelfBlockEntity shelf) {
                        boolean took = shelf.takeOne(desiredItem);
                        // Even if failed (someone emptied it), continue to register flow
                        state = State.GO_REGISTER;

                        // v1: walk to nearest register (later). For now: just pay near shelf if no register.
                        targetRegisterPos = findNearestRegisterPos(SEARCH_RADIUS);
                        if (targetRegisterPos != null) {
                            this.getNavigation().moveTo(
                                    targetRegisterPos.getX() + 0.5, targetRegisterPos.getY() + 0.5, targetRegisterPos.getZ() + 0.5,
                                    SPEED
                            );
                        } else {
                            // no register found; drop payment where you are
                            dropPayment(1);
                            state = State.GO_SIGN;
                        }
                    } else {
                        state = State.GO_REGISTER;
                    }
                }
            }

            case GO_REGISTER -> {
                // If we have a register target, pay when we arrive
                if (targetRegisterPos != null) {
                    if (this.blockPosition().closerThan(targetRegisterPos, 2.0)) {
                        // v1 payment: 1 emerald flat (we'll use Ledger price later)
                        dropPayment(1);
                        state = State.GO_SIGN;
                    }
                } else {
                    // No register found: just “request” and leave (v1)
                    sayRequest();
                    state = State.GO_SIGN;
                }

                // If we haven’t picked a register yet (because no shelf), try now
                if (targetRegisterPos == null) {
                    targetRegisterPos = findNearestRegisterPos(SEARCH_RADIUS);
                    if (targetRegisterPos != null) {
                        this.getNavigation().moveTo(
                                targetRegisterPos.getX() + 0.5, targetRegisterPos.getY() + 0.5, targetRegisterPos.getZ() + 0.5,
                                SPEED
                        );
                    }
                }
            }

            case GO_SIGN -> {
                if (shopSignPos != null) {
                    this.getNavigation().moveTo(
                            shopSignPos.getX() + 0.5, shopSignPos.getY() + 0.5, shopSignPos.getZ() + 0.5,
                            SPEED
                    );

                    if (this.blockPosition().closerThan(shopSignPos, 2.0)) {
                        state = State.LEAVE;
                    }
                } else {
                    state = State.LEAVE;
                }
            }

            case LEAVE -> this.discard();
        }
    }

    private void sayRequest() {
        // Sends to nearest player within 10 blocks; simple for v1
        Player p = this.level().getNearestPlayer(this, 10);
        if (p != null) {
            p.sendSystemMessage(Component.literal("Customer wants: " + desiredItem.getDescription().getString() + " (not found)"));
        }
    }

    private void dropPayment(int emeralds) {
        if (emeralds <= 0) return;
        ItemStack pay = new ItemStack(Items.EMERALD, Math.min(64, emeralds));
        ItemEntity ent = new ItemEntity(this.level(), this.getX(), this.getY() + 0.2, this.getZ(), pay);
        this.level().addFreshEntity(ent);
    }

    private @Nullable BlockPos findShelfWithItem(Item item) {
        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    var be = this.level().getBlockEntity(cursor);

                    if (be instanceof DisplayShelfBlockEntity shelf) {
                        // Optional: only use shelves bound to same ledger, if customer has a ledger set
                        if (ledgerPos != null) {
                            BlockPos shelfLedger = shelf.getBoundLedgerPos();
                            if (shelfLedger == null || !shelfLedger.equals(ledgerPos)) continue;
                        }

                        if (shelf.hasItem(item)) return cursor.immutable();
                    }
                }
            }
        }
        return null;
    }

    private @Nullable BlockPos findNearestRegisterPos(int radius) {
        // v1: just find any Register block entity in range, return its pos.
        // We’ll tighten this later (bound ledger match, queueing, etc).
        BlockPos origin = this.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    var be = this.level().getBlockEntity(p);
                    if (be instanceof com.hiipower.openforbusiness.block.entity.RegisterBlockEntity reg) {
                        // Optional: match ledger binding if both exist
                        if (ledgerPos != null) {
                            var regLedger = reg.getBoundLedgerPos();
                            if (regLedger == null || !regLedger.equals(ledgerPos)) continue;
                        }

                        double d = origin.distSqr(p);
                        if (d < bestDist) {
                            bestDist = d;
                            best = p;
                        }
                    }
                }
            }
        }
        return best == null ? null : best.immutable();
    }

    // --- Save/load so state survives chunk reloads (nice to have) ---
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("lifeTicks", lifeTicks);
        tag.putString("state", state.name());
        tag.putString("desired", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(desiredItem).toString());
        if (shopSignPos != null) tag.putLong("shopSignPos", shopSignPos.asLong());
        if (ledgerPos != null) tag.putLong("ledgerPos", ledgerPos.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifeTicks = tag.getInt("lifeTicks");
        try { state = State.valueOf(tag.getString("state")); } catch (Exception ignored) { state = State.PICK_ITEM; }
        var key = tag.getString("desired");
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.tryParse(key));
        if (item != null) desiredItem = item;

        if (tag.contains("shopSignPos")) shopSignPos = BlockPos.of(tag.getLong("shopSignPos"));
        if (tag.contains("ledgerPos")) ledgerPos = BlockPos.of(tag.getLong("ledgerPos"));
    }
}

