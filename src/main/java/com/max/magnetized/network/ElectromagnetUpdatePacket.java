package com.max.magnetized.network;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ElectromagnetBlock;
import com.max.magnetized.block.entity.ElectromagnetBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ElectromagnetUpdatePacket(BlockPos pos, int range, int width, boolean requiresRedstone, boolean pushing)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ElectromagnetUpdatePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Magnetized.MODID, "electromagnet_update"));

    public static final StreamCodec<ByteBuf, ElectromagnetUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ElectromagnetUpdatePacket::pos,
                    ByteBufCodecs.INT, ElectromagnetUpdatePacket::range,
                    ByteBufCodecs.INT, ElectromagnetUpdatePacket::width,
                    ByteBufCodecs.BOOL, ElectromagnetUpdatePacket::requiresRedstone,
                    ByteBufCodecs.BOOL, ElectromagnetUpdatePacket::pushing,
                    ElectromagnetUpdatePacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(BlockPos pos, int range, int width, boolean requiresRedstone, boolean pushing) {
        ClientPacketDistributor.sendToServer(new ElectromagnetUpdatePacket(pos, range, width, requiresRedstone, pushing));
    }

    public static void handle(ElectromagnetUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(packet.pos());
                if (be instanceof ElectromagnetBlockEntity electromagnet) {
                    electromagnet.setRange(packet.range());
                    electromagnet.setWidth(packet.width());
                    electromagnet.setRequiresRedstone(packet.requiresRedstone());

                    boolean hasRedstoneSignal = player.level().hasNeighborSignal(packet.pos());
                    boolean shouldBeActive = !packet.requiresRedstone() || hasRedstoneSignal;

                    BlockState oldState = electromagnet.getBlockState();
                    BlockState newState = oldState
                            .setValue(ElectromagnetBlock.PUSHING, packet.pushing())
                            .setValue(ElectromagnetBlock.ACTIVE, shouldBeActive);

                    electromagnet.getLevel().setBlock(packet.pos(), newState, 3);
                    electromagnet.getLevel().sendBlockUpdated(packet.pos(), oldState, newState, 3);
                }
            }
        });
    }
}