package com.hiipower.openforbusiness.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class CustomerEntity extends Zombie {

    private int lifeTicks = 20 * 20; // 20 seconds

    public CustomerEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }


    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            lifeTicks--;
            if (lifeTicks <= 0) {
                this.discard();
            }
        }
    }
}

