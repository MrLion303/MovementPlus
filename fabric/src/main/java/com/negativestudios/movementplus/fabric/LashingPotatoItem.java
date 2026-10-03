package com.negativestudios.movementplus.fabric;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LashingPotatoItem extends Item {
    static final String STATE = "MovementPlusGrappleState";
    static final String X = "MovementPlusGrappleX", Y = "MovementPlusGrappleY", Z = "MovementPlusGrappleZ";
    static final String VX = "MovementPlusGrappleVX", VY = "MovementPlusGrappleVY", VZ = "MovementPlusGrappleVZ";
    static final String AGE = "MovementPlusGrappleAge";
    static final int FLYING = 1, ATTACHED = 2;
    private static final double RANGE = 48.0D;
    private static final double PROJECTILE_SPEED = 1.65D;
    private static final double MAX_PULL_SPEED = 1.15D;

    public LashingPotatoItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (state(stack) != 0) {
            clear(stack);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS, .8F, 1.2F);
            return TypedActionResult.success(stack, world.isClient);
        }
        if (!world.isClient) {
            Vec3d origin = player.getEyePos().add(player.getRotationVec(1.0F).multiply(.45D));
            Vec3d direction = player.getRotationVec(1.0F).normalize().multiply(PROJECTILE_SPEED);
            NbtCompound tag = stack.getOrCreateNbt();
            tag.putInt(STATE, FLYING);
            putVec(tag, origin, X, Y, Z);
            putVec(tag, direction, VX, VY, VZ);
            tag.putInt(AGE, 0);
            if (!player.getAbilities().creativeMode) stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.PLAYERS, .8F, 1.0F);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity player) || !selected || state(stack) == 0) return;
        NbtCompound tag = stack.getOrCreateNbt();
        if (state(stack) == FLYING) {
            if (world.isClient) return;
            Vec3d pos = readVec(tag, X, Y, Z);
            Vec3d velocity = readVec(tag, VX, VY, VZ);
            Vec3d next = pos.add(velocity);
            HitResult hit = world.raycast(new RaycastContext(pos, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
            int age = tag.getInt(AGE) + 1;
            tag.putInt(AGE, age);
            if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                putVec(tag, blockHit.getPos(), X, Y, Z);
                tag.putInt(STATE, ATTACHED);
                world.playSound(null, blockHit.getBlockPos(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.PLAYERS, .7F, 1.3F);
            } else if (age > (int)(RANGE / PROJECTILE_SPEED)) {
                clear(stack);
            } else {
                putVec(tag, next, X, Y, Z);
            }
            return;
        }

        Vec3d anchor = readVec(tag, X, Y, Z);
        Vec3d pull = anchor.subtract(player.getEyePos());
        double distance = pull.length();
        if (distance < 1.35D) {
            clear(stack);
            return;
        }
        Vec3d acceleration = pull.normalize().multiply(Math.min(.115D, .045D + distance * .002D));
        Vec3d velocity = player.getVelocity().multiply(.985D).add(acceleration);
        double speed = velocity.length();
        if (speed > MAX_PULL_SPEED) velocity = velocity.multiply(MAX_PULL_SPEED / speed);
        player.setVelocity(velocity);
        player.velocityModified = true;
        player.fallDistance = 0.0F;

        if (world.isClient && world.getTime() % 3L == 0L) {
            world.addParticle(ParticleTypes.CRIT, anchor.x, anchor.y, anchor.z, 0, 0, 0);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.movementplus.lashing_potato.tooltip"));
        if (state(stack) == ATTACHED) tooltip.add(Text.translatable("item.movementplus.lashing_potato.attached"));
    }

    static int state(ItemStack stack) {
        return stack.hasNbt() ? stack.getNbt().getInt(STATE) : 0;
    }

    static Vec3d position(ItemStack stack) {
        return stack.hasNbt() && state(stack) != 0 ? readVec(stack.getNbt(), X, Y, Z) : null;
    }

    private static Vec3d readVec(NbtCompound tag, String x, String y, String z) {
        return new Vec3d(tag.getDouble(x), tag.getDouble(y), tag.getDouble(z));
    }

    private static void putVec(NbtCompound tag, Vec3d v, String x, String y, String z) {
        tag.putDouble(x, v.x); tag.putDouble(y, v.y); tag.putDouble(z, v.z);
    }

    private static void clear(ItemStack stack) {
        if (!stack.hasNbt()) return;
        NbtCompound tag = stack.getNbt();
        tag.remove(STATE); tag.remove(X); tag.remove(Y); tag.remove(Z);
        tag.remove(VX); tag.remove(VY); tag.remove(VZ); tag.remove(AGE);
    }
}