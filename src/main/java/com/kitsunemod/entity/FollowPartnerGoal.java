package com.kitsunemod.entity;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** Sigue al jugador con el que tiene una cita, o al que le pidió que lo siguiera. */
public class FollowPartnerGoal extends Goal {
    private final KitsuneEntity kitsune;
    private final double speed;
    private Player target;
    private int recalc;

    public FollowPartnerGoal(KitsuneEntity kitsune, double speed) {
        this.kitsune = kitsune;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player p = kitsune.getFollowTarget();
        if (p == null || p.isSpectator() || kitsune.getTarget() != null) return false;
        if (kitsune.distanceToSqr(p) < 9.0D) return false;
        target = p;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && kitsune.getFollowTarget() == target
                && kitsune.getTarget() == null && kitsune.distanceToSqr(target) > 5.0D;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void stop() {
        target = null;
        kitsune.getNavigation().stop();
    }

    @Override
    public void tick() {
        kitsune.getLookControl().setLookAt(target, 10.0F, kitsune.getMaxHeadXRot());
        if (--recalc > 0) return;
        recalc = 10;
        if (kitsune.distanceToSqr(target) > 24 * 24) {
            // Teletransporte mágico de kitsune (fuego fatuo)
            kitsune.getNavigation().stop();
            kitsune.moveTo(target.getX() - target.getLookAngle().x * 1.5, target.getY(),
                    target.getZ() - target.getLookAngle().z * 1.5, kitsune.getYRot(), kitsune.getXRot());
        } else {
            kitsune.getNavigation().moveTo(target, speed);
        }
    }
}
