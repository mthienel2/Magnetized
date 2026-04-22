package com.max.magnetized.item;

import com.max.magnetized.Magnetized;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Magnetized.MODID);

    public static final DeferredItem<MagnetItem> MAGNET_ITEM =
            ITEMS.registerItem("magnet_item", MagnetItem::new, props -> props.stacksTo(1));
}
