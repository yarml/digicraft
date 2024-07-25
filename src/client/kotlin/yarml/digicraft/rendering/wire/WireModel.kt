package yarml.digicraft.rendering.wire

import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.Baker
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import yarml.digicraft.block.wire.WireShapes
import yarml.digicraft.center
import yarml.digicraft.orthogonal
import yarml.digicraft.revQuadPos
import java.util.function.Function

class WireModel : UnbakedModel {
    companion object {
        private val CopperSpriteId =
            SpriteIdentifier(
                PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                Identifier.of("minecraft:block/copper_block")
            )

    }

    private lateinit var copperSprite: Sprite

    override fun getModelDependencies(): MutableCollection<Identifier> {
        return mutableSetOf()
    }

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {
        // no-op
    }

    override fun bake(
        baker: Baker,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings
    ): BakedModel {
        copperSprite = textureGetter.apply(CopperSpriteId)

        val renderer = RendererAccess.INSTANCE.renderer!!
        val meshes = mutableMapOf<Pair<Direction, Direction>, Mesh>()

        for (direction in Direction.entries) {
            val baseMeshBuilder = renderer.meshBuilder()
            val baseEmitter = baseMeshBuilder.emitter!!
            val baseShape = WireShapes.getBaseShape(direction)

            val meshBuilders = mutableMapOf<Direction, Triple<MeshBuilder, QuadEmitter, Box>>()

            meshBuilders[direction] = Triple(baseMeshBuilder, baseEmitter, baseShape)
            for (neighbourDirection in WireShapes.clockwiseNeighbors(direction)) {
                val connectionMeshBuilder = renderer.meshBuilder()
                val connectionEmitter = connectionMeshBuilder.emitter!!
                val connectionShape = WireShapes.getConnectionSegmentShape(direction, neighbourDirection)
                meshBuilders[neighbourDirection] = Triple(connectionMeshBuilder, connectionEmitter, connectionShape)
            }

            for (cuboidFaceDirection in Direction.entries) {
                for ((_, builderData) in meshBuilders.entries) {
                    val (_, emitter, shape) = builderData
                    val params = revQuadPos(
                        cuboidFaceDirection,
                        shape.minX / 16.0,
                        shape.minY / 16.0,
                        shape.minZ / 16.0,
                        shape.maxX / 16.0,
                        shape.maxY / 16.0,
                        shape.maxZ / 16.0
                    )

                    emitter.square(
                        cuboidFaceDirection,
                        params.a.toFloat(),
                        params.b.toFloat(),
                        params.c.toFloat(),
                        params.d.toFloat(),
                        params.e.toFloat(),
                    )
                    emitter.spriteBake(copperSprite, MutableQuadView.BAKE_LOCK_UV)
                    emitter.color(-1, -1, -1, -1)
                    emitter.emit()
                }
            }

            for ((neighbourDirection, builderData) in meshBuilders.entries) {
                val (builder, _, _) = builderData
                meshes[Pair(direction, neighbourDirection)] = builder.build()
            }
        }
        return BakedWireModel(copperSprite, meshes)
    }
}