package com.negativestudios.movementplus.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MovementPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(MovementPlusFabric.LASHING_POTATO, new Identifier("minecraft", "lashing_potato_extended"),
            (stack, world, entity, seed) -> LashingPotatoItem.state(stack) != 0 ? 1.0F : 0.0F);
        WorldRenderEvents.AFTER_ENTITIES.register(MovementPlusFabricClient::renderRope);
    }

    private static void renderRope(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || context.matrixStack() == null || context.consumers() == null) return;
        ItemStack stack = player.getMainHandStack();
        if (LashingPotatoItem.state(stack) == 0) stack = player.getOffHandStack();
        Vec3d tip = LashingPotatoItem.position(stack);
        if (tip == null) return;
        Vec3d camera = context.camera().getPos();
        Vec3d start = player.getCameraPosVec(context.tickDelta()).add(0, -0.12D, 0).subtract(camera);
        Vec3d end = tip.subtract(camera);
        Vec3d direction = end.subtract(start).normalize();
        MatrixStack matrices = context.matrixStack();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getLines());
        vertices.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(92, 65, 42, 255)
            .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z).next();
        vertices.vertex(matrix, (float) end.x, (float) end.y, (float) end.z).color(145, 111, 72, 255)
            .normal(normal, (float) direction.x, (float) direction.y, (float) direction.z).next();
    }
}