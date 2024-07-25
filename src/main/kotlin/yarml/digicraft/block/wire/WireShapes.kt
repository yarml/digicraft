package yarml.digicraft.block.wire

import net.minecraft.block.Block
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

object WireShapes {
    private val Bases = mapOf(
        Direction.DOWN to Box(7.0, 0.0, 7.0, 9.0, 2.0, 9.0),
        Direction.UP to Box(7.0, 14.0, 7.0, 9.0, 16.0, 9.0),
        Direction.NORTH to Box(7.0, 7.0, 0.0, 9.0, 9.0, 2.0),
        Direction.SOUTH to Box(7.0, 7.0, 14.0, 9.0, 9.0, 16.0),
        Direction.EAST to Box(14.0, 7.0, 7.0, 16.0, 9.0, 9.0),
        Direction.WEST to Box(0.0, 7.0, 7.0, 2.0, 9.0, 9.0),
    )
    private val ConnectionSegments = mapOf(
        Pair(Direction.DOWN, Direction.NORTH) to Box(7.0, 0.0, 0.0, 9.0, 2.0, 7.0),
        Pair(Direction.DOWN, Direction.SOUTH) to Box(7.0, 0.0, 9.0, 9.0, 2.0, 16.0),
        Pair(Direction.DOWN, Direction.EAST) to Box(9.0, 0.0, 7.0, 16.0, 2.0, 9.0),
        Pair(Direction.DOWN, Direction.WEST) to Box(0.0, 0.0, 7.0, 7.0, 2.0, 9.0),

        Pair(Direction.UP, Direction.NORTH) to Box(7.0, 14.0, 9.0, 9.0, 16.0, 16.0),
        Pair(Direction.UP, Direction.SOUTH) to Box(7.0, 14.0, 0.0, 9.0, 16.0, 7.0),
        Pair(Direction.UP, Direction.EAST) to Box(0.0, 14.0, 7.0, 7.0, 16.0, 9.0),
        Pair(Direction.UP, Direction.WEST) to Box(9.0, 14.0, 7.0, 16.0, 16.0, 9.0),

        Pair(Direction.NORTH, Direction.DOWN) to Box(7.0, 0.0, 0.0, 9.0, 7.0, 2.0),
        Pair(Direction.NORTH, Direction.UP) to Box(7.0, 9.0, 0.0, 9.0, 16.0, 2.0),
        Pair(Direction.NORTH, Direction.EAST) to Box(9.0, 7.0, 0.0, 16.0, 9.0, 2.0),
        Pair(Direction.NORTH, Direction.WEST) to Box(0.0, 7.0, 0.0, 7.0, 9.0, 2.0),

        Pair(Direction.SOUTH, Direction.DOWN) to Box(7.0, 0.0, 14.0, 9.0, 7.0, 16.0),
        Pair(Direction.SOUTH, Direction.UP) to Box(7.0, 9.0, 14.0, 9.0, 16.0, 16.0),
        Pair(Direction.SOUTH, Direction.EAST) to Box(9.0, 7.0, 14.0, 16.0, 9.0, 16.0),
        Pair(Direction.SOUTH, Direction.WEST) to Box(0.0, 7.0, 14.0, 7.0, 9.0, 16.0),

        Pair(Direction.EAST, Direction.DOWN) to Box(14.0, 0.0, 7.0, 16.0, 7.0, 9.0),
        Pair(Direction.EAST, Direction.UP) to Box(14.0, 9.0, 7.0, 16.0, 16.0, 9.0),
        Pair(Direction.EAST, Direction.NORTH) to Box(14.0, 7.0, 0.0, 16.0, 9.0, 7.0),
        Pair(Direction.EAST, Direction.SOUTH) to Box(14.0, 7.0, 9.0, 16.0, 9.0, 16.0),

        Pair(Direction.WEST, Direction.DOWN) to Box(0.0, 0.0, 7.0, 2.0, 7.0, 9.0),
        Pair(Direction.WEST, Direction.UP) to Box(0.0, 9.0, 7.0, 2.0, 16.0, 9.0),
        Pair(Direction.WEST, Direction.NORTH) to Box(0.0, 7.0, 0.0, 2.0, 9.0, 7.0),
        Pair(Direction.WEST, Direction.SOUTH) to Box(0.0, 7.0, 9.0, 2.0, 9.0, 16.0),
    )

    private val BaseOutlines: Map<Direction, VoxelShape> = Bases.mapValues { (_, value) ->
        Block.createCuboidShape(
            Math.clamp(value.minX - 0.5, 0.0, 16.0),
            Math.clamp(value.minY - 0.5, 0.0, 16.0),
            Math.clamp(value.minZ - 0.5, 0.0, 16.0),
            Math.clamp(value.maxX + 0.5, 0.0, 16.0),
            Math.clamp(value.maxY + 0.5, 0.0, 16.0),
            Math.clamp(value.maxZ + 0.5, 0.0, 16.0),
        ).simplify()
    }
    private val ConnectionSegmentOutlines = ConnectionSegments.mapValues { (_, value) ->
        Block.createCuboidShape(
            Math.clamp(value.minX - 0.5, 0.0, 16.0),
            Math.clamp(value.minY - 0.5, 0.0, 16.0),
            Math.clamp(value.minZ - 0.5, 0.0, 16.0),
            Math.clamp(value.maxX + 0.5, 0.0, 16.0),
            Math.clamp(value.maxY + 0.5, 0.0, 16.0),
            Math.clamp(value.maxZ + 0.5, 0.0, 16.0),
        ).simplify()
    }

    fun getBaseOutlineShape(side: Direction): VoxelShape {
        return BaseOutlines[side]!!
    }

    fun getConnectionSegmentOutlineShape(from: Direction, to: Direction): VoxelShape {
        return ConnectionSegmentOutlines[Pair(from, to)]!!
    }

    fun getBaseShape(side: Direction): Box {
        return Bases[side]!!
    }
    fun getConnectionSegmentShape(from: Direction, to: Direction): Box {
        return ConnectionSegments[Pair(from, to)]!!
    }

    private val clockwiseNeighbourMap = mapOf(
        Direction.DOWN to listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),
        Direction.UP to listOf(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST),
        Direction.NORTH to listOf(Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST),
        Direction.WEST to listOf(Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH),
        Direction.SOUTH to listOf(Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST),
        Direction.EAST to listOf(Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH),
    )

    fun clockwiseNeighbors(side: Direction): List<Direction> {
        return clockwiseNeighbourMap[side]!!
    }
}