package yarml.digicraft.item

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemUsageContext
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.world.event.GameEvent
import yarml.digicraft.block.DigiBlockEntities
import yarml.digicraft.block.DigiBlocks

class WireBlockItem : BlockItem(DigiBlocks.Wire, Settings()) {
    override fun useOnBlock(ctx: ItemUsageContext): ActionResult {
        val superResult = super.useOnBlock(ctx)
        if (superResult.isAccepted) {
            return superResult
        }
        val world = ctx.world
        val side = ctx.side
        val pos = ctx.blockPos
        val neighbourPos = pos.offset(side)
        val neighbourAtSide = world.getBlockState(neighbourPos)
        if (neighbourAtSide.block != DigiBlocks.Wire) {
            return ActionResult.PASS
        }
        val maybeNeighbourWire = world.getBlockEntity(neighbourPos, DigiBlockEntities.WireBlockEntity)
        if (maybeNeighbourWire.isEmpty) {
            return ActionResult.PASS
        }
        val neighbourWire = maybeNeighbourWire.get()
        if (!neighbourWire.addBaseToSide(side.opposite)) {
            return ActionResult.PASS
        }
        val soundGroup = DigiBlocks.Wire.defaultState.soundGroup
        world.playSound(
            ctx.player,
            pos,
            this.getPlaceSound(DigiBlocks.Wire.defaultState),
            SoundCategory.BLOCKS,
            (soundGroup.getVolume() + 1.0f) / 2.0f,
            soundGroup.getPitch() * 0.8f
        )
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter(ctx.player, neighbourAtSide))
        world.updateListeners(neighbourPos, neighbourAtSide, neighbourAtSide, Block.NOTIFY_LISTENERS)
        ctx.stack.decrementUnlessCreative(1, ctx.player)
        return ActionResult.SUCCESS
    }
}