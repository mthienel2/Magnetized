package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ElectromagnetBlock;
import com.max.magnetized.block.ModBlocks;
import com.max.magnetized.block.entity.ElectromagnetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.ItemTags;
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
public class ElectromagnetVisualizationEvents {

    private static final ContextKey<Set<BlockPos>> VISUALIZED_ELECTROMAGNETS_KEY =
            new ContextKey<>(Identifier.fromNamespaceAndPath(Magnetized.MODID, "visualized_electromagnets"));

    private static final float R = 1.0f;
    private static final float G = 0.1f;
    private static final float B = 0.1f;
    private static final float A = 0.5f;

    private static final Set<BlockPos> visualizedElectromagnets =
            Collections.synchronizedSet(new HashSet<>());

    public static void toggle(BlockPos pos) {
        if (visualizedElectromagnets.contains(pos)) {
            visualizedElectromagnets.remove(pos);
        } else {
            visualizedElectromagnets.add(pos.immutable());
        }
    }

    public static boolean isVisualized(BlockPos pos) {
        return visualizedElectromagnets.contains(pos);
    }

    @SubscribeEvent
    public static void onBlockBreak(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (!level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.ELECTROMAGNET_BLOCK.get())) return;

        ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());
        if (heldItem.is(ItemTags.PICKAXES) || event.getEntity().isCreative()) {
            visualizedElectromagnets.remove(pos);
        }
    }

    @SubscribeEvent
    public static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(VISUALIZED_ELECTROMAGNETS_KEY, visualizedElectromagnets);
    }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getGameTime() % 20 == 0) {
            visualizedElectromagnets.removeIf(pos ->
                    !mc.level.getBlockState(pos).is(ModBlocks.ELECTROMAGNET_BLOCK.get()));
        }

        Set<BlockPos> positions = event.getLevelRenderState().getRenderData(VISUALIZED_ELECTROMAGNETS_KEY);
        if (positions == null || positions.isEmpty()) return;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();

        for (BlockPos center : positions) {
            BlockState state = mc.level.getBlockState(center);
            if (!state.is(ModBlocks.ELECTROMAGNET_BLOCK.get())) continue;

            BlockEntity be = mc.level.getBlockEntity(center);
            if (!(be instanceof ElectromagnetBlockEntity electromagnet)) continue;

            Direction facing = state.getValue(ElectromagnetBlock.FACING);
            int range = electromagnet.getRange();
            double halfWidth = electromagnet.getWidth() / 2.0;

            double cx = center.getX() + 0.5 - camera.position().x;
            double cy = center.getY() + 0.5 - camera.position().y;
            double cz = center.getZ() + 0.5 - camera.position().z;

            poseStack.pushPose();
            poseStack.translate(cx, cy, cz);

            PoseStack.Pose pose = poseStack.last();
            final float fhw = (float) halfWidth;
            final int frange = range;
            final Direction ffacing = facing;

            event.getSubmitNodeCollector().submitCustomGeometry(
                    poseStack,
                    RenderTypes.linesTranslucent(),
                    (p, consumer) -> drawBeamOutline(p.pose(), p, consumer, ffacing, frange, fhw)
            );

            poseStack.popPose();
        }
    }

    private static void drawBeamOutline(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer consumer,
                                        Direction facing, int range, float hw) {
        float minX, maxX, minY, maxY, minZ, maxZ;

        switch (facing) {
            case NORTH -> {
                minX = -hw; maxX = hw;
                minY = -0.5f; maxY = 0.5f;
                minZ = -(range + 0.5f); maxZ = -0.5f;
            }
            case SOUTH -> {
                minX = -hw; maxX = hw;
                minY = -0.5f; maxY = 0.5f;
                minZ = 0.5f; maxZ = range + 0.5f;
            }
            case EAST -> {
                minX = 0.5f; maxX = range + 0.5f;
                minY = -0.5f; maxY = 0.5f;
                minZ = -hw; maxZ = hw;
            }
            case WEST -> {
                minX = -(range + 0.5f); maxX = -0.5f;
                minY = -0.5f; maxY = 0.5f;
                minZ = -hw; maxZ = hw;
            }
            default -> { return; }
        }

        drawLine(consumer, matrix, pose, minX, minY, minZ, maxX, minY, minZ);
        drawLine(consumer, matrix, pose, maxX, minY, minZ, maxX, minY, maxZ);
        drawLine(consumer, matrix, pose, maxX, minY, maxZ, minX, minY, maxZ);
        drawLine(consumer, matrix, pose, minX, minY, maxZ, minX, minY, minZ);

        drawLine(consumer, matrix, pose, minX, maxY, minZ, maxX, maxY, minZ);
        drawLine(consumer, matrix, pose, maxX, maxY, minZ, maxX, maxY, maxZ);
        drawLine(consumer, matrix, pose, maxX, maxY, maxZ, minX, maxY, maxZ);
        drawLine(consumer, matrix, pose, minX, maxY, maxZ, minX, maxY, minZ);

        drawLine(consumer, matrix, pose, minX, minY, minZ, minX, maxY, minZ);
        drawLine(consumer, matrix, pose, maxX, minY, minZ, maxX, maxY, minZ);
        drawLine(consumer, matrix, pose, maxX, minY, maxZ, maxX, maxY, maxZ);
        drawLine(consumer, matrix, pose, minX, minY, maxZ, minX, maxY, maxZ);
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
}