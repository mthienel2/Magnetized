package com.max.magnetized.screen;

import com.max.magnetized.block.ElectromagnetBlock;
import com.max.magnetized.menu.ElectromagnetMenu;
import com.max.magnetized.network.ElectromagnetUpdatePacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ElectromagnetScreen extends AbstractContainerScreen<ElectromagnetMenu> {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            "magnetized", "textures/gui/container/electromagnet.png");

    private int range;
    private int width;
    private boolean requiresRedstone;
    private boolean pushing;

    public ElectromagnetScreen(ElectromagnetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 200, 130);
        this.range = menu.getBlockEntity().getRange();
        this.width = menu.getBlockEntity().getWidth();
        this.requiresRedstone = menu.getBlockEntity().isRequiresRedstone();
        this.pushing = menu.getBlockEntity().getBlockState().getValue(ElectromagnetBlock.PUSHING);
    }

    @Override
    protected void init() {
        super.init();

        // Range buttons
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            if (range > 3) { range--; sendUpdate(); }
        }).bounds(leftPos + 20, topPos + 15, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            if (range < 9) { range++; sendUpdate(); }
        }).bounds(leftPos + 110, topPos + 15, 20, 20).build());

        // Width buttons
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            if (width > 1) { width--; sendUpdate(); }
        }).bounds(leftPos + 20, topPos + 43, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            if (width < 9) { width++; sendUpdate(); }
        }).bounds(leftPos + 110, topPos + 43, 20, 20).build());

        // Push/Pull toggle
        addRenderableWidget(Button.builder(
                Component.literal(pushing ? "Mode: Push" : "Mode: Pull"), btn -> {
                    pushing = !pushing;
                    btn.setMessage(Component.literal(pushing ? "Mode: Push" : "Mode: Pull"));
                    sendUpdate();
                }).bounds(leftPos + 20, topPos + 71, 156, 20).build());

        // Signal mode toggle
        addRenderableWidget(Button.builder(
                Component.literal(requiresRedstone ? "High Signal" : "Low Signal"), btn -> {
                    requiresRedstone = !requiresRedstone;
                    btn.setMessage(Component.literal(requiresRedstone ? "High Signal" : "Low Signal"));
                    sendUpdate();
                }).bounds(leftPos + 20, topPos + 96, 156, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        String rangeText = "Range: " + range;
        String widthText = "Width: " + width;

        int centerX = 75;
        graphics.text(font, Component.literal(rangeText), centerX - font.width(rangeText) / 2, 21, 0xFF404040, false);
        graphics.text(font, Component.literal(widthText), centerX - font.width(widthText) / 2, 49, 0xFF404040, false);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    private void sendUpdate() {
        ElectromagnetUpdatePacket.send(
                menu.getBlockEntity().getBlockPos(),
                range, width, requiresRedstone, pushing
        );
    }
}