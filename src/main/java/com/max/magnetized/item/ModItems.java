package com.max.magnetized.item;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ModBlocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Magnetized.MODID);

    public static final DeferredItem<MagnetItem> MAGNET_ITEM =
            ITEMS.registerItem("magnet_item", MagnetItem::new, props -> props.stacksTo(1));

    public static final DeferredItem<LightningBottleItem> LIGHTNING_BOTTLE_ITEM =
            ITEMS.registerItem("lightning_bottle_item", LightningBottleItem::new, props -> props
                    .stacksTo(1)
                    .component(DataComponents.CONSUMABLE, Consumable.builder()
                            .consumeSeconds(1.6f)
                            .animation(ItemUseAnimation.DRINK)
                            .sound(SoundEvents.GENERIC_DRINK)
                            .hasConsumeParticles(false)
                            .build()
                    )
            );

    public static final DeferredItem<BlockItem> MAGNET_NULLIFIER_ITEM = ITEMS.registerItem(
            "magnet_nullifier",
            props -> new MagnetNullifierBlockItem(ModBlocks.MAGNET_NULLIFIER.get(), props)
    );

    public static final DeferredItem<BlockItem> ELECTROMAGNET_BLOCK_ITEM = ITEMS.registerItem(
            "electromagnet_block",
            props -> new ElectromagnetBlockItem(ModBlocks.ELECTROMAGNET_BLOCK.get(), props)
    );
}
