package net.ndrei.inventoryflow.blocks.connector

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.ndrei.inventoryflow.connectors.IFlowConnectorPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPartHitBox
import net.ndrei.teslacorelib.render.selfrendering.EmptyBakery

class ConnectorPiece(aabb: AxisAlignedBB) : BlockPart(), IFlowConnectorPart {
    init {
        super.boxes.add(BlockPartHitBox(aabb))
    }

    override fun canBeHitWith(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer?, stack: ItemStack): Boolean {
        if ((player == null) || stack.isEmpty) {
            return false
        }

        return true
    }

    override fun getBakery() = EmptyBakery
}
