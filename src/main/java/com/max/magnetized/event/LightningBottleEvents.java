package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Magnetized.MODID)
public class LightningBottleEvents {

    private static final DustColorTransitionOptions LIGHTNING_DUST =
            new DustColorTransitionOptions(0x3399FF, 0xFFFFFF, 1.0f);

    private static final Map<Level, Set<BlockPos>> struckRods =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<Level, Map<BlockPos, Integer>> struckRodTimers =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final int STRIKE_DURATION_TICKS = 20 * 60; // 1 minute

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent event) {
        BlockPos strikePos = event.getEntity().blockPosition();
        Level level = event.getEntity().level();

        for (BlockPos candidate : BlockPos.betweenClosed(
                strikePos.offset(-1, -1, -1),
                strikePos.offset(1, 1, 1))) {

            BlockState state = level.getBlockState(candidate);
            if (state.is(Blocks.LIGHTNING_ROD)) {
                BlockPos immutable = candidate.immutable();
                struckRods
                        .computeIfAbsent(level, k -> Collections.synchronizedSet(new HashSet<>()))
                        .add(immutable);
                struckRodTimers
                        .computeIfAbsent(level, k -> Collections.synchronizedMap(new HashMap<>()))
                        .put(immutable, STRIKE_DURATION_TICKS);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockStateChange(BlockEvent.NeighborNotifyEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (state.is(Blocks.LIGHTNING_ROD) &&
                state.getValue(LightningRodBlock.POWERED)) {

            BlockPos immutable = pos.immutable();
            Set<BlockPos> struck = struckRods
                    .computeIfAbsent(level, k -> Collections.synchronizedSet(new HashSet<>()));

            // Only register if not already tracking this rod
            if (!struck.contains(immutable)) {
                struck.add(immutable);
                struckRodTimers
                        .computeIfAbsent(level, k -> Collections.synchronizedMap(new HashMap<>()))
                        .put(immutable, STRIKE_DURATION_TICKS);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Map<BlockPos, Integer> timers = struckRodTimers.get(level);
        Set<BlockPos> struck = struckRods.get(level);
        if (timers == null || struck == null) return;

        boolean spawnParticles = (level.getGameTime() % 10) == 0;

        timers.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            int ticksLeft = entry.getValue() - 1;

            if (ticksLeft <= 0) {
                struck.remove(pos);
                return true;
            }

            entry.setValue(ticksLeft);

            if (spawnParticles) {
                ((ServerLevel) level).sendParticles(
                        LIGHTNING_DUST,
                        pos.getX() + 0.5,
                        pos.getY() + 1.0,
                        pos.getZ() + 0.5,
                        5,
                        0.1, 0.2, 0.1,
                        0.02
                );
            }

            return false;
        });
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();

        if (hand != InteractionHand.MAIN_HAND) return;

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.LIGHTNING_ROD)) return;

        Set<BlockPos> struck = struckRods.get(level);

        if (!level.isClientSide() && struck != null && struck.contains(pos)) {
            for (int i = 0; i < 8; i++) {
                ((ServerLevel) level).sendParticles(
                        LIGHTNING_DUST,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        8,
                        0.2, 0.3, 0.2,
                        0.05
                );
            }
        }

        if (struck == null || !struck.contains(pos)) return;

        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.GLASS_BOTTLE)) return;

        if (!level.isClientSide()) {
            heldItem.shrink(1);

            ItemStack reward = new ItemStack(ModItems.LIGHTNING_BOTTLE_ITEM.get());
            if (!player.getInventory().add(reward)) {
                player.drop(reward, false);
            }
            // TODO: Custom sound for pickup
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL,
                    SoundSource.BLOCKS, 0.8f, 1.2f);

            struck.remove(pos);

            Map<BlockPos, Integer> timers = struckRodTimers.get(level);
            if (timers != null) timers.remove(pos);
        }

        event.setCanceled(true);
    }
}