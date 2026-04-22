package com.max.magnetized.network;

import com.max.magnetized.compat.CuriosCompat;
import com.max.magnetized.component.ModDataComponents;
import com.max.magnetized.item.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.max.magnetized.Magnetized.MODID;

public record ToggleMagnetPacket() implements CustomPacketPayload {

    public static final Type<ToggleMagnetPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID, "toggle_magnet"));
    public static final StreamCodec<ByteBuf, ToggleMagnetPacket> STREAM_CODEC = StreamCodec.unit(new ToggleMagnetPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleMagnetPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            // Check hotbar first, then curios
            ItemStack magnetStack = findMagnetInHotbar(player);

            if (magnetStack.isEmpty()) {
                magnetStack = CuriosCompat.findMagnetInCurios(player);
            }

            if (!magnetStack.isEmpty()) {
                boolean isActive = magnetStack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
                boolean newState = !isActive;

                magnetStack.set(ModDataComponents.MAGNET_ACTIVE.get(), newState);

                player.level().playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.5f,
                        newState ? 1.2f : 0.8f
                );

                player.sendOverlayMessage(
                        Component.literal(newState ? "Magnet Activated" : "Magnet Deactivated")
                                .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                );
            } else {
                player.sendOverlayMessage(
                        Component.literal("No magnet found!")
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        });
    }

    private static ItemStack findMagnetInHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.MAGNET_ITEM.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}