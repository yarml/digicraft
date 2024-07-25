package yarml.digicraft.block.wire

import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import yarml.digicraft.block.DigiBlockEntities

class WireBlock : Block(Settings.copy(Blocks.REDSTONE_WIRE)), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return WireBlockEntity(pos, state)
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

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        val blockEntity = world.getBlockEntity(pos, DigiBlockEntities.WireBlockEntity)
        if (blockEntity.isPresent) {
            return blockEntity.get().updateForNeighbourUpdate(state, direction, neighborState, world, pos, neighborPos)
        } else {
            return state
        }
    }
}