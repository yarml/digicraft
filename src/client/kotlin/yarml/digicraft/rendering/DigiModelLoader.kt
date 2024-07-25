package yarml.digicraft.rendering

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import yarml.digicraft.DigiCraft
import yarml.digicraft.rendering.wire.WireModel

class DigiModelLoader : ModelLoadingPlugin {
    override fun onInitializeModelLoader(pluginContext: ModelLoadingPlugin.Context) {
        pluginContext.resolveModel().register { ctx ->
            if (ctx.id().namespace == DigiCraft.MOD_ID && ctx.id().path == "block/wire") {
                WireModel()
            } else {
                null
            }
        }
    }
}