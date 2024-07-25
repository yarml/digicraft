package yarml.digicraft

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import yarml.digicraft.block.DigiBlockEntities
import yarml.digicraft.block.DigiBlocks

object DigiCraft : ModInitializer {
	const val MOD_ID = "digicraft"
	val Logger = LoggerFactory.getLogger(MOD_ID)
	override fun onInitialize() {
		DigiBlocks.init()
		DigiBlockEntities.init()
	}
}