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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class LashingPotatoItem extends Item {
    private static final String ATTACHED="MovementPlusAttached", X="MovementPlusAnchorX", Y="MovementPlusAnchorY", Z="MovementPlusAnchorZ";
    private static final double RANGE=64.0D;

    public LashingPotatoItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack=player.getStackInHand(hand);
        if (isAttached(stack)) {
            detach(stack);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS, .8F, 1.2F);
            return ActionResult.SUCCESS;
        }
        HitResult hit=player.raycast(RANGE,0.0F,false);
        if (!(hit instanceof BlockHitResult blockHit)) return ActionResult.PASS;
        if (!world.isClient) {
            setAnchor(stack, blockHit.getBlockPos());
            if (!player.getAbilities().creativeMode) stack.damage(1,player,p->p.sendToolBreakStatus(hand));
        }
        world.playSound(null,player.getBlockPos(),SoundEvents.BLOCK_CHAIN_PLACE,SoundCategory.PLAYERS,.8F,1.0F);
        return ActionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity player) || !selected || !isAttached(stack)) return;
        Vec3d anchor=getAnchor(stack);
        if (anchor==null) { detach(stack); return; }
        Vec3d center=anchor.add(.5,.5,.5);
        Vec3d delta=center.subtract(player.getPos());
        double distance=delta.length();
        if (distance<1.35D) {
            player.setVelocity(Vec3d.ZERO);
        } else {
            player.setVelocity(delta.normalize().multiply(Math.min(1.35D,.28D+distance*.055D)));
            player.velocityModified=true;
        }
        player.fallDistance=0.0F;
        if (world.isClient && world.getTime()%2L==0L) {
            Vec3d start=player.getPos().add(0,player.getStandingEyeHeight()*.75,0);
            Vec3d line=center.subtract(start);
            double length=line.length();
            if (length>0.01) {
                Vec3d step=line.normalize().multiply(.55);
                for(double d=0;d<length;d+=.55) {
                    Vec3d p=start.add(step.multiply(d));
                    world.addParticle(ParticleTypes.COMPOSTER,p.x,p.y,p.z,0,.01,0);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack,@Nullable World world,List<Text> tooltip,TooltipContext context) {
        tooltip.add(Text.translatable("item.movementplus.lashing_potato.tooltip"));
        if(isAttached(stack)) tooltip.add(Text.translatable("item.movementplus.lashing_potato.attached"));
    }

    private static boolean isAttached(ItemStack s){ return s.hasNbt() && s.getNbt().getBoolean(ATTACHED); }
    private static void setAnchor(ItemStack s,BlockPos p){
        NbtCompound n=s.getOrCreateNbt(); n.putBoolean(ATTACHED,true); n.putInt(X,p.getX()); n.putInt(Y,p.getY()); n.putInt(Z,p.getZ());
    }
    private static void detach(ItemStack s){
        if(!s.hasNbt())return; NbtCompound n=s.getNbt(); n.remove(ATTACHED); n.remove(X); n.remove(Y); n.remove(Z);
    }
    private static Vec3d getAnchor(ItemStack s){
        if(!isAttached(s))return null; NbtCompound n=s.getNbt(); return new Vec3d(n.getInt(X),n.getInt(Y),n.getInt(Z));
    }
}
