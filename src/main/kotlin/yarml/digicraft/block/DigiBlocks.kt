package yarml.digicraft.block

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import yarml.digicraft.DigiCraft
import yarml.digicraft.block.wire.WireBlock

object DigiBlocks {
    lateinit var Wire: WireBlock

    private fun <B: Block> register(block: B, name: String, itemSettings: Item.Settings?): B {
        val id = Identifier.of(DigiCraft.MOD_ID, name)
        if (itemSettings != null) {
            val blockItem = BlockItem(block, Item.Settings())
            Registry.register(Registries.ITEM, id, blockItem)
        }
        return Registry.register(Registries.BLOCK, id, block)
    }

    fun init() {
        Wire = register(WireBlock(), "wire", null)
    }
}