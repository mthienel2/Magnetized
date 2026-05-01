package com.max.magnetized.item;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class ElectromagnetBlockItem extends BlockItem {

    public static final Component TOOLTIP = Component.translatable("electromagnet_block.magnetized.tooltip")
            .withStyle(ChatFormatting.GRAY);

    public ElectromagnetBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(TOOLTIP);
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}