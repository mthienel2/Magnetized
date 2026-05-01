package com.max.magnetized;

import com.max.magnetized.block.ModBlocks;
import com.max.magnetized.block.entity.ModBlockEntities;
import com.max.magnetized.component.ModDataComponents;
import com.max.magnetized.item.ModItems;
import com.max.magnetized.menu.ModMenuTypes;
import com.max.magnetized.network.ElectromagnetUpdatePacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.max.magnetized.item.ModItems.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Magnetized.MODID)
public class Magnetized {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "magnetized";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "magnetized" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab with the id "magnetized:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("magnetized", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.magnetized")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> MAGNET_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(MAGNET_ITEM.get());
                output.accept(MAGNET_NULLIFIER_ITEM.get());
                output.accept(LIGHTNING_BOTTLE_ITEM.get());
                output.accept(ELECTROMAGNET_BLOCK_ITEM.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Magnetized(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);

        ModMenuTypes.MENU_TYPES.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Magnetized) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Magnetized.MODID);
        registrar.playToServer(
                ElectromagnetUpdatePacket.TYPE,
                ElectromagnetUpdatePacket.STREAM_CODEC,
                ElectromagnetUpdatePacket::handle
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
