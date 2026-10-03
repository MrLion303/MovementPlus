package com.negativestudios.movementplus.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = MovementPlusForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LashingPotatoRopeRenderer {
    private LashingPotatoRopeRenderer() {}

    @SubscribeEvent
    public static void renderRope(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (LashingPotatoItem.state(stack) == 0) stack = player.getOffhandItem();
        Vec3 tip = LashingPotatoItem.position(stack);
        if (tip == null) return;
        Vec3 camera = event.getCamera().getPosition();
        Vec3 start = player.getEyePosition(event.getPartialTick()).add(0, -.12D, 0).subtract(camera);
        Vec3 end = tip.subtract(camera);
        Vec3 direction = end.subtract(start).normalize();
        PoseStack poseStack = event.getPoseStack();
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(2.0F);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(92, 65, 42, 255)
            .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z).endVertex();
        buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z).color(145, 111, 72, 255)
            .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.lineWidth(1.0F);
    }
}