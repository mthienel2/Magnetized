package com.max.magnetized.item;

import com.max.magnetized.block.MagnetNullifierBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class MagnetNullifierBlockItem extends BlockItem {

    public static final Component TOOLTIP = Component.translatable("magnet_nullifier.magnetized.tooltip")
            .withStyle(ChatFormatting.GRAY);

    public MagnetNullifierBlockItem(MagnetNullifierBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(TOOLTIP);
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}