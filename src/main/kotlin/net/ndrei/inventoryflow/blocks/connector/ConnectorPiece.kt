package net.ndrei.inventoryflow.blocks.connector

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.common.model.TRSRTransformation
import net.ndrei.inventoryflow.connectors.IFlowConnectorPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPartHitBox
import net.ndrei.teslacorelib.render.selfrendering.EmptyBakery
import net.ndrei.teslacorelib.render.selfrendering.IBakery

class ConnectorPiece(aabb: AxisAlignedBB, val facing: EnumFacing) : BlockPart(), IFlowConnectorPart {
    private var storedConnector: IFlowConnectorPart? = null

    init {
        super.boxes.add(BlockPartHitBox(aabb))
        // this.storedConnector = FluidConnector(this.facing)
    }

    fun setConnection(connection: IFlowConnectorPart) {
        this.storedConnector = if (this.storedConnector == null) connection else null
    }

    override fun canBeHitWith(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer?, stack: ItemStack): Boolean {
        if ((player == null) || stack.isEmpty) {
            return (this.storedConnector != null)
        }

        return true
    }

    override fun getBakery() = object: IBakery {
        override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
            return (this@ConnectorPiece.storedConnector?.getBakery() ?: EmptyBakery)
                .getQuads(state, stack, side, vertexFormat, transform)
        }
    }

    override fun renderOutline(event: DrawBlockHighlightEvent) {
        if (this.storedConnector == null)
            super.renderOutline(event)
        else
            this.storedConnector!!.renderOutline(event)
    }
}
