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
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.WorldAccess
import yarml.digicraft.block.DigiBlockEntities

class WireBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(DigiBlockEntities.WireBlockEntity, pos, state) {
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
        Direction.DOWN to WireSide(false, true, false, false, false, false),
        Direction.UP to WireSide(false, false, false, false, false, false),
        Direction.NORTH to WireSide(false, false, false, false, false, false),
        Direction.SOUTH to WireSide(false, false, false, false, false, false),
        Direction.EAST to WireSide(false, false, false, false, false, false),
        Direction.WEST to WireSide(false, false, false, false, false, false),
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
        return side.clockwiseConnections().zip(WireShapes.clockwiseNeighbors(direction))
    }

    fun updateForNeighbourUpdate(state: BlockState, direction: Direction, neighbourState: BlockState, world: WorldAccess, pos: BlockPos, neighbourPos: BlockPos): BlockState {
        return state
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

class WireSide(
    var power: Boolean,
    var base: Boolean,
    var up: Boolean,
    var down: Boolean,
    var left: Boolean,
    var right: Boolean
) {
    companion object {
        val CODEC: Codec<WireSide> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("power").forGetter(WireSide::power),
                Codec.BOOL.fieldOf("base").forGetter(WireSide::base),
                Codec.BOOL.fieldOf("up").forGetter(WireSide::up),
                Codec.BOOL.fieldOf("down").forGetter(WireSide::down),
                Codec.BOOL.fieldOf("left").forGetter(WireSide::left),
                Codec.BOOL.fieldOf("right").forGetter(WireSide::right)
            ).apply(instance, ::WireSide)
        }
    }

    fun clockwiseConnections(): List<Boolean> {
        return listOf(up, right, down, left)
    }

    fun exists(): Boolean {
        return base || up || down || left || right
    }
}
