package com.max.magnetized.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MagnetNullifierBlock extends Block {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public MagnetNullifierBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, clicked);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }


    private static final VoxelShape SHAPE = Shapes.box(0.25, 0.0, 0.25, 0.75, 0.5625, 0.75);

    private static final VoxelShape SHAPE_UP = Shapes.box(3.5/16, 0.0, 3.5/16, 12.5/16, 9.0/16, 12.5/16);
    private static final VoxelShape SHAPE_DOWN = Shapes.box(3.5/16, 7.0/16, 3.5/16, 12.5/16, 1.0, 12.5/16);
    private static final VoxelShape SHAPE_NORTH = Shapes.box(3.5/16, 3.5/16, 7.0/16, 12.5/16, 12.5/16, 1.0);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(3.5/16, 3.5/16, 0.0, 12.5/16, 12.5/16, 9.0/16);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0.0, 3.5/16, 3.5/16, 9.0/16, 12.5/16, 12.5/16);
    private static final VoxelShape SHAPE_WEST = Shapes.box(7.0/16, 3.5/16, 3.5/16, 1.0, 12.5/16, 12.5/16);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_UP;
        };
    }
}