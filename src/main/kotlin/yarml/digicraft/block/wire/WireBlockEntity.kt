package yarml.digicraft.block.wire

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import yarml.digicraft.DigiCraft
import yarml.digicraft.block.DigiBlockEntities
import yarml.digicraft.block.DigiBlocks
import java.util.Optional

class WireBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(DigiBlockEntities.WireBlockEntity, pos, state) {
    companion object {
        private fun nbtWriteSide(side: String, wireSide: WireSide, nbt: NbtCompound) {
            WireSide.CODEC.encodeStart(NbtOps.INSTANCE, wireSide).result().ifPresent {
                nbt.put(side, it)
            }
        }

        private fun nbtReadSide(side: String, nbt: NbtCompound): WireSide {
            val wireSide = WireSide.CODEC.parse(NbtOps.INSTANCE, nbt.get(side)).result().get()
            return wireSide
        }
    }

    private val sides = mutableMapOf(
        Direction.DOWN to WireSide(power = false, base = false),
        Direction.UP to WireSide(power = false, base = false),
        Direction.NORTH to WireSide(power = false, base = false),
        Direction.SOUTH to WireSide(power = false, base = false),
        Direction.EAST to WireSide(power = false, base = false),
        Direction.WEST to WireSide(power = false, base = false),
    )

    fun getOutlineShape(): VoxelShape {
        var shape = VoxelShapes.empty()
        for (direction in Direction.entries) {
            shape = VoxelShapes.union(shape, getSideOutlineShape(direction))
        }
        return shape
    }

    private fun getSideOutlineShape(direction: Direction): VoxelShape {
        val side = sides[direction]!!
        val base = if (side.base) {
            WireShapes.getBaseOutlineShape(direction)
        } else {
            VoxelShapes.empty()
        }

        val shape = getSideNeighbours(direction).map { (connected, neighbor) ->
            if (connected) {
                WireShapes.getConnectionSegmentOutlineShape(direction, neighbor)
            } else {
                VoxelShapes.empty()
            }
        }.stream().reduce(base, VoxelShapes::union)

        return shape
    }

    fun getSide(direction: Direction): WireSide {
        return sides[direction]!!
    }

    fun getSideNeighbours(direction: Direction): List<Pair<Boolean, Direction>> {
        val side = sides[direction]!!
        return WireShapes.clockwiseNeighbors(direction)
            .map { neighbourDirection -> Pair(side.connectionOf(neighbourDirection), neighbourDirection) }
    }

    fun updateForNeighbourUpdate(
        state: BlockState,
        direction: Direction,
        neighbourState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighbourPos: BlockPos
    ): BlockState {
        if (neighbourState.isAir) {
            removeBase(direction)
        }
        if (neighbourState.block == DigiBlocks.Wire) {
            val maybeNeighbourWire = world.getBlockEntity(neighbourPos, DigiBlockEntities.WireBlockEntity)
            if (maybeNeighbourWire.isPresent) {
                connectToNeighbour(maybeNeighbourWire.get(), direction)
            }
        }
        return if (sides.values.any { side -> side.exists() }) {
            state
        } else {
            markRemoved()
            Blocks.AIR.defaultState
        }
    }

    fun addBaseToSide(direction: Direction): Boolean {
        if (getSide(direction).base) {
            return false
        }
        val modifiedWires = mutableSetOf<WireBlockEntity>()
        sides[direction] = WireSide(power = false, base = true)
        for (rotationDirection in WireShapes.clockwiseNeighbors(direction)) {
            // Add connections to bases within the same wire block
            val neighbourSide = getSide(rotationDirection)
            if (neighbourSide.base) {
                neighbourSide.setConnectionOf(direction, true)
                getSide(direction).setConnectionOf(rotationDirection, true)
            }
            // Add connections to bases in neighbouring wire blocks that have bases on the added side
            val neighbourPos = pos.offset(rotationDirection)
            val neighbourWire = world!!.getBlockEntity(neighbourPos, DigiBlockEntities.WireBlockEntity)
            if (neighbourWire.isPresent) {
                val neighbourBlockSide = neighbourWire.get().getSide(direction)
                if (neighbourBlockSide.base) {
                    modifiedWires.add(neighbourWire.get())
                    neighbourBlockSide.setConnectionOf(rotationDirection.opposite, true)
                    getSide(direction).setConnectionOf(rotationDirection, true)
                }
            }

            // Add connection to the block that is neighbouring our neighbour in the direction we want to add a base on
            val neighbourNeighbourPos = neighbourPos.offset(direction)
            val neighbourNeighbourWire =
                world!!.getBlockEntity(neighbourNeighbourPos, DigiBlockEntities.WireBlockEntity)
            if (neighbourNeighbourWire.isPresent) {
                val neighbourNeighbourBlockSide = neighbourNeighbourWire.get().getSide(rotationDirection.opposite)
                if (neighbourNeighbourBlockSide.base) {
                    modifiedWires.add(neighbourNeighbourWire.get())
                    neighbourNeighbourBlockSide.setConnectionOf(direction.opposite, true)
                    getSide(direction).setConnectionOf(rotationDirection, true)
                }
            }
        }
        for (modifiedWire in modifiedWires) {
            modifiedWire.markDirty()
        }
        markDirty()
        return true
    }

    private fun removeBase(direction: Direction) {
        if (!getSide(direction).base) {
            return
        }
        val modifiedWires = mutableSetOf<WireBlockEntity>()
        sides[direction] = WireSide(power = false, base = false)
        for (rotationDirection in WireShapes.clockwiseNeighbors(direction)) {
            val neighbourSide = getSide(rotationDirection)
            if (neighbourSide.connectionOf(direction)) {
                neighbourSide.setConnectionOf(direction, false)
            }

            // Remove connections in neighbouring wire blocks that have bases on the removed side
            val neighbourPos = pos.offset(rotationDirection)
            val neighbourWire = world!!.getBlockEntity(neighbourPos, DigiBlockEntities.WireBlockEntity)
            if (neighbourWire.isPresent) {
                val neighbourBlockSide = neighbourWire.get().getSide(direction)
                if (neighbourBlockSide.base) {
                    modifiedWires.add(neighbourWire.get())
                    neighbourBlockSide.setConnectionOf(rotationDirection.opposite, false)
                    neighbourWire.get().markDirty()
                }
            }

            // Remove connection to the block that is neighbouring our neighbour in the direction we want to remove a base on
            val neighbourNeighbourPos = neighbourPos.offset(direction)
            val neighbourNeighbourWire =
                world!!.getBlockEntity(neighbourNeighbourPos, DigiBlockEntities.WireBlockEntity)
            if (neighbourNeighbourWire.isPresent) {
                val neighbourNeighbourBlockSide = neighbourNeighbourWire.get().getSide(rotationDirection.opposite)
                if (neighbourNeighbourBlockSide.base) {
                    modifiedWires.add(neighbourNeighbourWire.get())
                    neighbourNeighbourBlockSide.setConnectionOf(direction.opposite, false)
                    neighbourNeighbourWire.get().markDirty()
                }
            }
        }
        for (modifiedWire in modifiedWires) {
            modifiedWire.markDirty()
        }
        markDirty()
    }

    fun removeAll() {
        for (direction in Direction.entries) {
            removeBase(direction)
        }
    }

    private fun connectToNeighbour(neighbour: WireBlockEntity, neighbourDirection: Direction) {
        for (rotationDirection in WireShapes.clockwiseNeighbors(neighbourDirection)) {
            val neighbourSide = neighbour.getSide(rotationDirection)
            val side = getSide(rotationDirection)
            if (neighbourSide.base && side.base) {
                neighbourSide.setConnectionOf(neighbourDirection.opposite, true)
                side.setConnectionOf(neighbourDirection, true)
                markDirty()
                neighbour.markDirty()
            }
        }
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)

        for (direction in Direction.entries) {
            nbtWriteSide(direction.toString(), sides[direction]!!, nbt)
        }
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        for (direction in Direction.entries) {
            sides[direction] = nbtReadSide(direction.toString(), nbt)
        }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
        return createNbt(registryLookup)
    }
}

data class WireSide(
    var power: Boolean,
    val base: Boolean,
) {
    private val connections = mutableMapOf<Direction, Boolean>()

    companion object {
        val CODEC: Codec<WireSide> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("power").forGetter(WireSide::power),
                Codec.BOOL.fieldOf("base").forGetter(WireSide::base),
                Codec.BOOL.optionalFieldOf("up").forGetter { Optional.ofNullable(it.connections[Direction.UP]) },
                Codec.BOOL.optionalFieldOf("down").forGetter { Optional.ofNullable(it.connections[Direction.DOWN]) },
                Codec.BOOL.optionalFieldOf("north").forGetter { Optional.ofNullable(it.connections[Direction.NORTH]) },
                Codec.BOOL.optionalFieldOf("south").forGetter { Optional.ofNullable(it.connections[Direction.SOUTH]) },
                Codec.BOOL.optionalFieldOf("east").forGetter { Optional.ofNullable(it.connections[Direction.EAST]) },
                Codec.BOOL.optionalFieldOf("west").forGetter { Optional.ofNullable(it.connections[Direction.WEST]) },
            ).apply(instance, ::WireSide)
        }
    }

    constructor(
        power: Boolean,
        base: Boolean,
        up: Optional<Boolean>,
        down: Optional<Boolean>,
        north: Optional<Boolean>,
        south: Optional<Boolean>,
        east: Optional<Boolean>,
        west: Optional<Boolean>
    ) : this(
        power, base
    ) {
        up.orElse(null)?.let { connections[Direction.UP] = it }
        down.orElse(null)?.let { connections[Direction.DOWN] = it }
        north.orElse(null)?.let { connections[Direction.NORTH] = it }
        south.orElse(null)?.let { connections[Direction.SOUTH] = it }
        east.orElse(null)?.let { connections[Direction.EAST] = it }
        west.orElse(null)?.let { connections[Direction.WEST] = it }
    }

    fun connectionOf(direction: Direction): Boolean {
        return connections[direction] ?: false
    }

    fun setConnectionOf(direction: Direction, value: Boolean) {
        connections[direction] = value
    }

    fun exists(): Boolean {
        return base || connections.any { it.value }
    }
}
