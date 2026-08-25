package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ElectromagnetBlock;
import com.max.magnetized.block.ModBlocks;
import com.max.magnetized.block.entity.ElectromagnetBlockEntity;
import com.max.magnetized.compat.CuriosCompat;
import com.max.magnetized.component.ModDataComponents;
import com.max.magnetized.item.MagnetItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Iterator;
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

        if (magnetStack.isEmpty() && ModList.get().isLoaded("curios")) {
            magnetStack = CuriosCompat.findMagnetInCurios(player);
        }

        if (!magnetStack.isEmpty()) {
            boolean active = magnetStack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
            if (active) {
                MagnetItem magnet = (MagnetItem) magnetStack.getItem();
                pullItemsToPlayer(player, level, magnet, magnet.getRadius());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) return;

        ItemStack magnetStack = findActiveMagnetInHotbar(player);
        if (magnetStack.isEmpty() && ModList.get().isLoaded("curios")) {
            magnetStack = CuriosCompat.findMagnetInCurios(player);
        }
        if (magnetStack.isEmpty()) return;

        boolean active = magnetStack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
        if (!active) return;

        MagnetItem magnet = (MagnetItem) magnetStack.getItem();
        if (!magnet.hasAutoPickup()) return;

        if (isNearNullifier(player, event.getLevel())) return;

        boolean pickedUpAny = false;
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next().getItem();

            if (player.getInventory().add(drop)) {
                pickedUpAny = true;
                if (drop.isEmpty()) {
                    iterator.remove();
                }
            }
        }

        if (pickedUpAny) {
            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.2F,
                    ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
            );
        }
    }

    private static ItemStack findActiveMagnetInHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof MagnetItem) {
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

    private static void tryAutoPickup(Player player, ItemEntity item) {
        ItemEntityPickupEvent.Pre pre = new ItemEntityPickupEvent.Pre(player, item);
        NeoForge.EVENT_BUS.post(pre);
        if (pre.canPickup() == TriState.FALSE) return;

        ItemStack original = item.getItem().copy();
        ItemStack remaining = item.getItem();
        int startCount = remaining.getCount();

        if (player.getInventory().add(remaining)) {
            player.take(item, startCount - remaining.getCount());

            if (remaining.isEmpty()) {
                item.discard();
            }

            NeoForge.EVENT_BUS.post(new ItemEntityPickupEvent.Post(player, item, original));
        }
    }

    private static void pullItemsToPlayer(Player player, Level level, MagnetItem magnet, int radius) {
        if (isNearNullifier(player, level)) return;

        AABB area = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

        for (ItemEntity item : items) {
            // Skip items that are inside a nullifier zone
            if (isNearNullifier(item, level)) continue;
            // Prevents crazy item whiplash when you throw a block onto the ground
            if (item.hasPickUpDelay()) continue;

            double dx = player.getX() - item.getX();
            double dy = (player.getY() + 0.3) - item.getY();
            double dz = player.getZ() - item.getZ();

            dy *= 0.25;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (magnet.hasAutoPickup() && distance <= 0.5) {
                tryAutoPickup(player, item);
                continue;
            }

            if (distance > 0.5) {
                double speed = 1.4;

                item.setDeltaMovement(
                        dx / distance * speed,
                        dy / distance * speed,
                        dz / distance * speed
                );
            }
        }

        List<ExperienceOrb> expOrbs = level.getEntitiesOfClass(ExperienceOrb.class, area);

        for (ExperienceOrb exp : expOrbs) {
            // Skip exp orbs that are inside a nullifier zone
            if (isNearNullifier(exp, level)) continue;

            double orb_dx = player.getX() - exp.getX();
            double orb_dy = (player.getY() + 0.3) - exp.getY();
            double orb_dz = player.getZ() - exp.getZ();

            orb_dy *= 0.25;

            double orb_distance = Math.sqrt(orb_dx * orb_dx + orb_dy * orb_dy + orb_dz * orb_dz);

            if (orb_distance > 0.5) {
                double orb_speed = 1.1;

                exp.setDeltaMovement(
                        orb_dx / orb_distance * orb_speed,
                        orb_dy / orb_distance * orb_speed,
                        orb_dz / orb_distance * orb_speed
                );
            }
        }
    }
}