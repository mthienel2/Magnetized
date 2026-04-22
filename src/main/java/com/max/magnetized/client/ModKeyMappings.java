package com.max.magnetized.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {

    public static final KeyMapping TOGGLE_MAGNET = new KeyMapping(
            "key.magnetized.toggle_magnet", // Translation key
            KeyConflictContext.IN_GAME, // Only works when not in a GUI
            InputConstants.Type.KEYSYM, // Keyboard input
            GLFW.GLFW_KEY_M, // Default key is M
            KeyMapping.Category.GAMEPLAY // Category in controls menu
    );
}