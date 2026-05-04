package com.max.magnetized.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class SuperchargedMagnetItem extends MagnetItem {

    public static final Component TOOLTIP = Component.translatable("magnet.magnetized.supercharged_magnet").withStyle(ChatFormatting.DARK_GRAY);

    public SuperchargedMagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getRadius() {
        return 9;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(TOOLTIP);
    }
}
