package yarml.digicraft.rendering.wire

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import yarml.digicraft.block.DigiBlockEntities
import java.util.function.Supplier

class BakedWireModel(private val particleSprite: Sprite, private val meshes: Map<Pair<Direction, Direction>, Mesh>) : BakedModel,
    FabricBakedModel {
    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> {
        return mutableListOf()
    }

    override fun useAmbientOcclusion() = true
    override fun hasDepth() = false
    override fun isSideLit() = false
    override fun isBuiltin() = false
    override fun getParticleSprite() = particleSprite
    override fun getTransformation() = null
    override fun getOverrides() = null

    override fun isVanillaAdapter() = false
    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        val maybeWire = world.getBlockEntity(pos, DigiBlockEntities.WireBlockEntity)
        if (maybeWire.isEmpty) {
            return
        }
        val wire = maybeWire.get()
        val emitter = context.emitter

        for (direction in Direction.entries) {
            val side = wire.getSide(direction)
            if (side.base) {
                meshes[Pair(direction, direction)]!!.outputTo(emitter)
            }
            for ((connected, neighbor) in wire.getSideNeighbours(direction)) {
                if (connected) {
                    meshes[Pair(direction, neighbor)]!!.outputTo(emitter)
                }
            }
        }
    }
}