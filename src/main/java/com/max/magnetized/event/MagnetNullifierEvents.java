package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = Magnetized.MODID, value = Dist.CLIENT)
public class MagnetNullifierEvents {

    private static final ContextKey<Set<BlockPos>> VISUALIZED_NULLIFIERS_KEY =
            new ContextKey<>(Identifier.fromNamespaceAndPath(Magnetized.MODID, "visualized_nullifiers"));

    private static final float R = 1.0f;
    private static final float G = 0.1f;
    private static final float B = 0.1f;
    private static final float A = 0.5f;

    private static final Set<BlockPos> visualizedNullifiers =
            Collections.synchronizedSet(new HashSet<>());

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();

        if (!level.isClientSide()) return;
        if (hand != InteractionHand.MAIN_HAND) return;
        if (!player.isShiftKeyDown()) return;

        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.MAGNET_NULLIFIER.get())) return;

        if (visualizedNullifiers.contains(pos)) {
            visualizedNullifiers.remove(pos);
        } else {
            visualizedNullifiers.add(pos.immutable());
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(VISUALIZED_NULLIFIERS_KEY, visualizedNullifiers);
    }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Set<BlockPos> positions = event.getLevelRenderState().getRenderData(VISUALIZED_NULLIFIERS_KEY);
        if (positions == null || positions.isEmpty()) return;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();

        for (BlockPos center : positions) {
            double cx = center.getX() + 0.5 - camera.position().x;
            double cy = center.getY() + 0.5 - camera.position().y;
            double cz = center.getZ() + 0.5 - camera.position().z;

            poseStack.pushPose();
            poseStack.translate(cx, cy, cz);

            PoseStack.Pose pose = poseStack.last();
            event.getSubmitNodeCollector().submitCustomGeometry(
                    poseStack,
                    RenderTypes.linesTranslucent(),
                    (p, consumer) -> drawOutline(pose.pose(), pose, consumer)
            );

            poseStack.popPose();
        }
    }

    private static void drawOutline(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer consumer) {
        float half = 3.5f;

        // Bottom face
        drawLine(consumer, matrix, pose, -half, -half, -half,  half, -half, -half);
        drawLine(consumer, matrix, pose,  half, -half, -half,  half, -half,  half);
        drawLine(consumer, matrix, pose,  half, -half,  half, -half, -half,  half);
        drawLine(consumer, matrix, pose, -half, -half,  half, -half, -half, -half);

        // Top face
        drawLine(consumer, matrix, pose, -half,  half, -half,  half,  half, -half);
        drawLine(consumer, matrix, pose,  half,  half, -half,  half,  half,  half);
        drawLine(consumer, matrix, pose,  half,  half,  half, -half,  half,  half);
        drawLine(consumer, matrix, pose, -half,  half,  half, -half,  half, -half);

        // Vertical edges
        drawLine(consumer, matrix, pose, -half, -half, -half, -half,  half, -half);
        drawLine(consumer, matrix, pose,  half, -half, -half,  half,  half, -half);
        drawLine(consumer, matrix, pose,  half, -half,  half,  half,  half,  half);
        drawLine(consumer, matrix, pose, -half, -half,  half, -half,  half,  half);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2) {
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len;
        ny /= len;
        nz /= len;

        consumer.addVertex(matrix, x1, y1, z1)
                .setColor(R, G, B, A)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(2.0f);

        consumer.addVertex(matrix, x2, y2, z2)
                .setColor(R, G, B, A)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(2.0f);
    }

    // Removes the outline as soon as it's left-clicked
    @SubscribeEvent
    public static void onBlockBreak(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (!level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(ModBlocks.MAGNET_NULLIFIER.get())) return;

        ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());
        // Assuming these are the only two ways this happens
        if (heldItem.is(ItemTags.PICKAXES) || event.getEntity().isCreative()) {
            visualizedNullifiers.remove(pos);
        }
    }
}