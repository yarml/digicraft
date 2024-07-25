package yarml.digicraft

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import yarml.digicraft.rendering.DigiModelLoader

object DigiCraftClient : ClientModInitializer {
    override fun onInitializeClient() {
        ModelLoadingPlugin.register(DigiModelLoader())
    }
}