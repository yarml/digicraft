package yarml.digicraft.item

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import yarml.digicraft.DigiCraft

object DigiItems {
    lateinit var Wire: WireBlockItem

    private fun <I: Item> register(item: I, name: String): I {
        val id = Identifier.of(DigiCraft.MOD_ID, name)
        return Registry.register(Registries.ITEM, id, item)
    }

    fun init() {
        Wire = register(WireBlockItem(), "wire")
    }
}