package yarml.digicraft

import net.minecraft.util.math.Direction
import oshi.util.tuples.Quintet

fun orthogonal(direction: Direction, x: Double, y: Double, z: Double): Pair<Double, Double> {
    return when (direction) {
        Direction.UP -> Pair(x, z)
        Direction.DOWN -> Pair(x, z)
        Direction.NORTH -> Pair(x, y)
        Direction.SOUTH -> Pair(x, y)
        Direction.EAST -> Pair(z, y)
        Direction.WEST -> Pair(z, y)
    }
}

fun center(direction: Direction, min: Double, max: Double): Double {
    return when (direction) {
        Direction.UP -> 1 - max
        Direction.DOWN -> min
        Direction.NORTH -> 1 - max
        Direction.SOUTH -> min
        Direction.EAST -> 1 - max
        Direction.WEST -> min
    }
}

fun revQuadPos(
    direction: Direction,
    minX: Double,
    minY: Double,
    minZ: Double,
    maxX: Double,
    maxY: Double,
    maxZ: Double
): Quintet<Double, Double, Double, Double, Double> {
    return when (direction) {
        Direction.UP -> Quintet(minX, 1 - maxZ, maxX, 1 - minZ, 1 - maxY)
        Direction.DOWN -> Quintet(minX, minZ, maxX, maxZ, minY)
        Direction.NORTH -> Quintet(1 - maxX, minY, 1 - minX, maxY, minZ)
        Direction.SOUTH -> Quintet(minX, minY, maxX, maxY, 1 - maxZ)
        Direction.EAST -> Quintet(1 - maxZ, minY, 1 - minZ, maxY, 1 - maxX)
        Direction.WEST -> Quintet(minZ, minY, maxZ, maxY, minX)
    }
}
