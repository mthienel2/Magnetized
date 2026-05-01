package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ElectromagnetBlock;
import com.max.magnetized.block.ModBlocks;
import com.max.magnetized.block.entity.ElectromagnetBlockEntity;
import com.max.magnetized.compat.CuriosCompat;
import com.max.magnetized.component.ModDataComponents;
import com.max.magnetized.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Magnetized.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();

        // Check all neighbors of the changed block for electromagnets
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!neighborState.is(ModBlocks.ELECTROMAGNET_BLOCK.get())) continue;

            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof ElectromagnetBlockEntity electromagnet) {
                boolean powered = level.hasNeighborSignal(neighborPos);
                boolean shouldBeActive = !electromagnet.isRequiresRedstone() || powered;
                boolean currentlyActive = neighborState.getValue(ElectromagnetBlock.ACTIVE);

                if (shouldBeActive != currentlyActive) {
                    BlockState newState = neighborState.setValue(ElectromagnetBlock.ACTIVE, shouldBeActive);
                    level.setBlock(neighborPos, newState, 3);
                    level.sendBlockUpdated(neighborPos, neighborState, newState, 3);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide()) {
            return;
        }

        ItemStack magnetStack = findActiveMagnetInHotbar(player);

        if (magnetStack.isEmpty()) {
            magnetStack = CuriosCompat.findMagnetInCurios(player);
        }

        if (!magnetStack.isEmpty()) {
            boolean active = magnetStack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
            if (active) {
                pullItemsToPlayer(player, level);
            }
        }
    }

    private static ItemStack findActiveMagnetInHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.MAGNET_ITEM.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isNearNullifier(Entity entity, Level level) {
        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();

        BlockPos center = entity.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-10, -10, -10),
                center.offset(10, 10, 10))) {

            if (level.getBlockState(pos).is(ModBlocks.MAGNET_NULLIFIER.get())) {
                double dx = Math.abs((pos.getX() + 0.5) - ex);
                double dy = Math.abs((pos.getY() + 0.5) - ey);
                double dz = Math.abs((pos.getZ() + 0.5) - ez);

                // Check if item is within the 7x7x7 cube (3.5 in each direction)
                if (dx <= 3.5 && dy <= 3.5 && dz <= 3.5) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void pullItemsToPlayer(Player player, Level level) {
        // If the player is in a nullifier zone, shut off the magnet entirely
        if (isNearNullifier(player, level)) return;

        int radius = 7;

        AABB area = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

        for (ItemEntity item : items) {
            // Skip items that are inside a nullifier zone
            if (isNearNullifier(item, level)) continue;

            double dx = player.getX() - item.getX();
            double dy = (player.getY() + 0.3) - item.getY();
            double dz = player.getZ() - item.getZ();

            dy *= 0.25;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 0.5) {
                double speed = 0.3;

                item.setDeltaMovement(
                        dx / distance * speed,
                        dy / distance * speed,
                        dz / distance * speed
                );
            }
        }
    }
}