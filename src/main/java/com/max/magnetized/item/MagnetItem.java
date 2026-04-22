package com.max.magnetized.item;

import com.max.magnetized.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.function.Consumer;

public class MagnetItem extends Item {

    public MagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {

            boolean isActive = stack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
            boolean newState = !isActive;

            stack.set(ModDataComponents.MAGNET_ACTIVE.get(), newState);
            player.sendOverlayMessage(
                    Component.literal(newState ? "Magnet Activated" : "Magnet Deactivated")
                            .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
            );
        }

        if (level.isClientSide()) {
            boolean isActive = stack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
            level.playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5f,
                    isActive ? 1.2f : 0.8f,
                    false
            );
        }

        return InteractionResult.CONSUME.withoutItem();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Give enchantment glow when magnet is active
        return stack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
    }

    public static final Component MAGNET_TOOLTIP = Component.translatable("magnet.magnetized.magnet").withStyle(ChatFormatting.GRAY);

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(MAGNET_TOOLTIP);
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
