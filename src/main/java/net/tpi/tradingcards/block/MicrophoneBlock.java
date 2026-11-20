package net.tpi.tradingcards.block;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A standalone microphone-on-a-stand decoration. Orients to face away from
 * whatever surface it's placed against - floor, wall, or ceiling - the same
 * way a vanilla End Rod does. Mirrors EndRodBlock/RodBlock's real approach
 * (verified against the decompiled 26.3 sources): one FACING property, shape
 * auto-rotated per axis via Shapes.rotateAllAxis, and blockstate rotation
 * variants handle reorienting the model itself - no per-facing model needed.
 *
 * 26.3 removed block codecs (and the block_type registry) from vanilla, so
 * there is no CODEC / codec() override anymore.
 */
public class MicrophoneBlock extends DirectionalBlock {

	private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateAllAxis(Block.cube(4.0, 4.0, 11.0));

	public MicrophoneBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING).getAxis());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
