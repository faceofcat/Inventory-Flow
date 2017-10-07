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
import net.ndrei.inventoryflow.client.Textures
import net.ndrei.inventoryflow.connectors.ConnectorBlockPart
import net.ndrei.inventoryflow.connectors.ConnectorSide
import net.ndrei.inventoryflow.connectors.IFlowConnectorPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPartHitBox
import net.ndrei.teslacorelib.blocks.multipart.IBlockPartHitBox
import net.ndrei.teslacorelib.render.selfrendering.*

class ConnectorPiece(aabb: AxisAlignedBB, val facing: EnumFacing, private val tile: ConnectorTile)
    : ConnectorBlockPart(ConnectorSide.fromFacing(facing)) {
    private var storedConnector: IFlowConnectorPart? = null
    private val pipeBox = BlockPartHitBox(facing.getAxisAlignedAABB32(11.0, 8.0))

    init {
        super.boxes.add(BlockPartHitBox(aabb))
        // this.storedConnector = FluidConnector(this.facing)
    }

    fun setConnection(connection: IFlowConnectorPart) {
        this.storedConnector = if (this.storedConnector == null) connection else null
    }

    val hasConnection get() = this.storedConnector != null

    override fun canBeHitWith(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer?, stack: ItemStack): Boolean {
        if ((player == null) || stack.isEmpty) {
            return (this.storedConnector != null)
        }


        return true
    }

    override val bakingKey: String
        get() = super.bakingKey + "::${this.storedConnector?.bakingKey ?: this.tile.isConnectedPipe(this.facing).toString()}"

    override fun getBakery() = object: IBakery {
        override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
            return (this@ConnectorPiece.storedConnector?.getBakery() ?: this@ConnectorPiece.getPipeBakery())
                .getQuads(state, stack, side, vertexFormat, transform)
        }
    }

    private fun getPipeBakery() =
        if (!this.tile.isConnectedPipe(this.facing)) {
            EmptyBakery
        }
        else object: IBakery {
            override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
                val aabb = this@ConnectorPiece.pipeBox.aabb
                val cube = RawCube(aabb.min.scale(32.0), aabb.max.scale(32.0), Textures.PIPE.getSprite()).autoUV().dualSide()

                val facing = this@ConnectorPiece.facing
                val base = if ((facing == EnumFacing.UP) || (facing == EnumFacing.DOWN)) EnumFacing.NORTH else facing.rotateY()
                (0..3).fold(base) { face, _ ->
                    cube.addFace(face)
                    face.rotateAround(facing.axis)
                }

                return mutableListOf<BakedQuad>().also { cube.bake(it, vertexFormat, transform) }
            }
        }.static()

    override fun renderOutline(event: DrawBlockHighlightEvent) {
        if (this.storedConnector == null)
            super.renderOutline(event)
        else
            this.storedConnector!!.renderOutline(event)
    }

    override val hitBoxes: List<IBlockPartHitBox>
        get() = if (this.tile.isConnectedPipe(this.facing)) listOf(this.pipeBox) else super.hitBoxes
}
