package com.max.magnetized.block.entity;

import com.max.magnetized.block.ElectromagnetBlock;
import com.max.magnetized.menu.ElectromagnetMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ElectromagnetBlockEntity extends BlockEntity implements MenuProvider {

    private int range = 5;
    private int width = 1;
    private boolean requiresRedstone = true;

    public ElectromagnetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROMAGNET_BLOCK_ENTITY.get(), pos, state);
    }

    public int getRange() { return range; }
    public void setRange(int range) {
        this.range = Math.max(3, Math.min(9, range));
        setChanged();
        syncToClient();
    }

    public int getWidth() { return width; }
    public void setWidth(int width) {
        this.width = Math.max(1, Math.min(9, width));
        setChanged();
        syncToClient();
    }

    public boolean isRequiresRedstone() { return requiresRedstone; }
    public void setRequiresRedstone(boolean requiresRedstone) {
        this.requiresRedstone = requiresRedstone;
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.range = input.getIntOr("range", 5);
        this.width = input.getIntOr("width", 1);
        this.requiresRedstone = input.getBooleanOr("requiresRedstone", true);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("range", this.range);
        output.putInt("width", this.width);
        output.putBoolean("requiresRedstone", this.requiresRedstone);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.getBlockPos());
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectromagnetMenu(containerId, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectromagnetBlockEntity be) {
        if (!state.getValue(ElectromagnetBlock.ACTIVE)) return;

        boolean pushing = state.getValue(ElectromagnetBlock.PUSHING);
        Direction facing = state.getValue(ElectromagnetBlock.FACING);
        int range = be.getRange();
        double halfWidth = be.getWidth() / 2.0;

        int effectiveRange = 0;
        for (int i = 1; i <= range; i++) {
            BlockPos checkPos = pos.relative(facing, i);
            BlockState checkState = level.getBlockState(checkPos);
            if (Block.isShapeFullBlock(checkState.getCollisionShape(level, checkPos))) {
                break;
            }
            effectiveRange = i;
        }

        if (effectiveRange == 0) return;

        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.5;
        double bz = pos.getZ() + 0.5;

        double minX, maxX, minY, maxY, minZ, maxZ;

        switch (facing) {
            case NORTH -> {
                minX = bx - halfWidth; maxX = bx + halfWidth;
                minY = by - halfWidth; maxY = by + halfWidth;
                minZ = bz - effectiveRange; maxZ = bz;
            }
            case SOUTH -> {
                minX = bx - halfWidth; maxX = bx + halfWidth;
                minY = by - halfWidth; maxY = by + halfWidth;
                minZ = bz; maxZ = bz + effectiveRange;
            }
            case EAST -> {
                minX = bx; maxX = bx + effectiveRange;
                minY = by - halfWidth; maxY = by + halfWidth;
                minZ = bz - halfWidth; maxZ = bz + halfWidth;
            }
            case WEST -> {
                minX = bx - effectiveRange; maxX = bx;
                minY = by - halfWidth; maxY = by + halfWidth;
                minZ = bz - halfWidth; maxZ = bz + halfWidth;
            }
            default -> { return; }
        }

        AABB area = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        List<Entity> entities = level.getEntities((Entity) null, area, e -> true);

        for (Entity entity : entities) {
            double dx = facing.getStepX();
            double dz = facing.getStepZ();

            double distanceAlongBeam;
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                distanceAlongBeam = Math.abs(entity.getZ() - bz) / effectiveRange;
            } else {
                distanceAlongBeam = Math.abs(entity.getX() - bx) / effectiveRange;
            }

            double maxSpeed = 0.4;
            double speed;
            if (pushing) {
                speed = maxSpeed * (1.0 - distanceAlongBeam);
                entity.addDeltaMovement(new Vec3(dx * speed, 0, dz * speed));
            } else {
                speed = maxSpeed * distanceAlongBeam;
                entity.addDeltaMovement(new Vec3(-dx * speed, 0, -dz * speed));
            }

            Vec3 movement = entity.getDeltaMovement();
            double maxVelocity = 0.4;
            double horizontalSpeed = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
            if (horizontalSpeed > maxVelocity) {
                double scale = maxVelocity / horizontalSpeed;
                entity.setDeltaMovement(movement.x * scale, movement.y, movement.z * scale);
            }

            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }

            entity.hurtMarked = true;
        }
    }
}