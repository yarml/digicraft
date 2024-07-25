package yarml.digicraft.block

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.BlockEntityType.BlockEntityFactory
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import yarml.digicraft.DigiCraft
import yarml.digicraft.block.wire.WireBlockEntity

object DigiBlockEntities {
    lateinit var WireBlockEntity: BlockEntityType<WireBlockEntity>

    private fun <T : BlockEntity> register(
        factory: BlockEntityFactory<T>,
        name: String,
        vararg blocks: Block
    ): BlockEntityType<T> {
        val id = Identifier.of(DigiCraft.MOD_ID, name)
        val blockEntityType = BlockEntityType.Builder.create(factory, *blocks).build()
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, blockEntityType)
    }

    fun init() {
        WireBlockEntity = register(::WireBlockEntity, "wire", DigiBlocks.Wire)
    }
}