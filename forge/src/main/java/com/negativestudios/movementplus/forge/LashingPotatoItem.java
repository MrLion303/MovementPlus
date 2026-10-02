package com.negativestudios.movementplus.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LashingPotatoItem extends Item {
    private static final String ATTACHED="MovementPlusAttached", X="MovementPlusAnchorX", Y="MovementPlusAnchorY", Z="MovementPlusAnchorZ";
    private static final double RANGE=64.0D;

    public LashingPotatoItem(Properties properties){super(properties);}

    @Override
    public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack stack=player.getItemInHand(hand);
        if(isAttached(stack)){
            detach(stack);
            level.playSound(null,player.blockPosition(),SoundEvents.CHAIN_BREAK,SoundSource.PLAYERS,.8F,1.2F);
            return InteractionResultHolder.success(stack);
        }
        HitResult hit=player.pick(RANGE,0.0F,false);
        if(!(hit instanceof BlockHitResult blockHit))return InteractionResultHolder.pass(stack);
        if(!level.isClientSide){
            setAnchor(stack,blockHit.getBlockPos());
            if(!player.getAbilities().instabuild)stack.hurtAndBreak(1,player,p->p.broadcastBreakEvent(hand));
        }
        level.playSound(null,player.blockPosition(),SoundEvents.CHAIN_PLACE,SoundSource.PLAYERS,.8F,1.0F);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){
        if(!(entity instanceof Player player)||!selected||!isAttached(stack))return;
        Vec3 anchor=getAnchor(stack);
        if(anchor==null){detach(stack);return;}
        Vec3 center=anchor.add(.5,.5,.5);
        Vec3 delta=center.subtract(player.position());
        double distance=delta.length();

        if(distance<1.35D)player.setDeltaMovement(Vec3.ZERO);
        else{
            player.setDeltaMovement(delta.normalize().scale(Math.min(1.35D,.28D+distance*.055D)));
            player.hurtMarked=true;
        }
        player.fallDistance=0.0F;

        if(level.isClientSide && level.getGameTime()%2L==0L){
            Vec3 start=player.position().add(0,player.getEyeHeight()*.75,0);
            Vec3 line=center.subtract(start);
            double length=line.length();
            if(length>0.01){
                Vec3 step=line.normalize().scale(.55);
                for(double d=0;d<length;d+=.55){
                    Vec3 p=start.add(step.scale(d));
                    level.addParticle(ParticleTypes.COMPOSTER,p.x,p.y,p.z,0,.01,0);
                }
            }
        }
    }

    private static boolean isAttached(ItemStack s){return s.hasTag()&&s.getTag().getBoolean(ATTACHED);}
    private static void setAnchor(ItemStack s,net.minecraft.core.BlockPos p){
        CompoundTag n=s.getOrCreateTag();n.putBoolean(ATTACHED,true);n.putInt(X,p.getX());n.putInt(Y,p.getY());n.putInt(Z,p.getZ());
    }
    private static void detach(ItemStack s){
        if(!s.hasTag())return;CompoundTag n=s.getTag();n.remove(ATTACHED);n.remove(X);n.remove(Y);n.remove(Z);
    }
    private static Vec3 getAnchor(ItemStack s){
        if(!isAttached(s))return null;CompoundTag n=s.getTag();return new Vec3(n.getInt(X),n.getInt(Y),n.getInt(Z));
    }
}
