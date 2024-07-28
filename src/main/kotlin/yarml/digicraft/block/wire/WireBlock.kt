package yarml.digicraft.block.wire

import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import yarml.digicraft.block.DigiBlockEntities

class WireBlock : Block(Settings.copy(Blocks.REDSTONE_WIRE)), BlockEntityProvider {
    init {
        defaultState = stateManager.defaultState.with(Properties.FACING, Direction.DOWN)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(Properties.FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(Properties.FACING, ctx.side.opposite)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return WireBlockEntity(pos, state)
    }

    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?): BlockState {
        return super.onBreak(world, pos, state, player)
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos, DigiBlockEntities.WireBlockEntity)
            if (blockEntity.isPresent) {
                blockEntity.get().removeAll()
            }
        }
        // This will remove the block entity
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        val blockEntity = world.getBlockEntity(pos, DigiBlockEntities.WireBlockEntity)
        return if (blockEntity.isPresent) {
            blockEntity.get().getOutlineShape()
        } else {
            VoxelShapes.fullCube()
        }
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        val maybeBlockEntity = world.getBlockEntity(pos, DigiBlockEntities.WireBlockEntity)
        if (maybeBlockEntity.isEmpty) {
            return state
        }
        val blockEntity = maybeBlockEntity.get()
        return blockEntity.updateForNeighbourUpdate(state, direction, neighborState, world, pos, neighborPos)
    }
}