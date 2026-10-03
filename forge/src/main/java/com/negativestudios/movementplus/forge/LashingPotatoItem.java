package com.negativestudios.movementplus.forge;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LashingPotatoItem extends Item {
    static final String STATE = "MovementPlusGrappleState";
    static final String X = "MovementPlusGrappleX", Y = "MovementPlusGrappleY", Z = "MovementPlusGrappleZ";
    static final String VX = "MovementPlusGrappleVX", VY = "MovementPlusGrappleVY", VZ = "MovementPlusGrappleVZ";
    static final String AGE = "MovementPlusGrappleAge";
    static final int FLYING = 1, ATTACHED = 2;
    private static final double RANGE = 48.0D, PROJECTILE_SPEED = 1.65D, MAX_PULL_SPEED = 1.15D;

    public LashingPotatoItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (state(stack) != 0) {
            clear(stack);
            level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, .8F, 1.2F);
            return InteractionResultHolder.success(stack);
        }
        if (!level.isClientSide) {
            Vec3 origin = player.getEyePosition().add(player.getLookAngle().scale(.45D));
            Vec3 direction = player.getLookAngle().normalize().scale(PROJECTILE_SPEED);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(STATE, FLYING);
            putVec(tag, origin, X, Y, Z);
            putVec(tag, direction, VX, VY, VZ);
            tag.putInt(AGE, 0);
            if (!player.getAbilities().instabuild) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, .8F, 1.0F);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || !selected || state(stack) == 0) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (state(stack) == FLYING) {
            if (level.isClientSide) return;
            Vec3 pos = readVec(tag, X, Y, Z);
            Vec3 velocity = readVec(tag, VX, VY, VZ);
            Vec3 next = pos.add(velocity);
            HitResult hit = level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            int age = tag.getInt(AGE) + 1;
            tag.putInt(AGE, age);
            if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                putVec(tag, blockHit.getLocation(), X, Y, Z);
                tag.putInt(STATE, ATTACHED);
                level.playSound(null, blockHit.getBlockPos(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, .7F, 1.3F);
            } else if (age > (int)(RANGE / PROJECTILE_SPEED)) {
                clear(stack);
            } else {
                putVec(tag, next, X, Y, Z);
            }
            return;
        }

        Vec3 anchor = readVec(tag, X, Y, Z);
        Vec3 pull = anchor.subtract(player.getEyePosition());
        double distance = pull.length();
        if (distance < 1.35D) { clear(stack); return; }
        Vec3 acceleration = pull.normalize().scale(Math.min(.115D, .045D + distance * .002D));
        Vec3 velocity = player.getDeltaMovement().scale(.985D).add(acceleration);
        double speed = velocity.length();
        if (speed > MAX_PULL_SPEED) velocity = velocity.scale(MAX_PULL_SPEED / speed);
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;

        if (level.isClientSide && level.getGameTime() % 3L == 0L) {
            level.addParticle(ParticleTypes.CRIT, anchor.x, anchor.y, anchor.z, 0, 0, 0);
        }
    }

    static int state(ItemStack stack) { return stack.hasTag() ? stack.getTag().getInt(STATE) : 0; }
    static Vec3 position(ItemStack stack) { return stack.hasTag() && state(stack) != 0 ? readVec(stack.getTag(), X, Y, Z) : null; }
    private static Vec3 readVec(CompoundTag tag, String x, String y, String z) {
        return new Vec3(tag.getDouble(x), tag.getDouble(y), tag.getDouble(z));
    }
    private static void putVec(CompoundTag tag, Vec3 v, String x, String y, String z) {
        tag.putDouble(x, v.x); tag.putDouble(y, v.y); tag.putDouble(z, v.z);
    }
    private static void clear(ItemStack stack) {
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        tag.remove(STATE); tag.remove(X); tag.remove(Y); tag.remove(Z);
        tag.remove(VX); tag.remove(VY); tag.remove(VZ); tag.remove(AGE);
    }
}