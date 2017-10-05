package net.ndrei.inventoryflow.blocks.connector

import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraftforge.common.model.TRSRTransformation
import net.ndrei.inventoryflow.connectors.ConnectorBlockPart
import net.ndrei.inventoryflow.connectors.FluidConnector
import net.ndrei.inventoryflow.connectors.IFlowConnectorPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPartHitBox
import net.ndrei.teslacorelib.blocks.multipart.IBlockPart
import net.ndrei.teslacorelib.blocks.multipart.IBlockPartHitBox
import net.ndrei.teslacorelib.blocks.multipart.IBlockPartProvider
import net.ndrei.teslacorelib.render.selfrendering.*

class ConnectorTile : TileEntity(), IBlockPartProvider {
    private val parts: List<IFlowConnectorPart>
    private val faces: Map<EnumFacing, ConnectorPiece>

    init {
        val parts = mutableListOf<IFlowConnectorPart>()
        val faces = mutableMapOf<EnumFacing, ConnectorPiece>()

        parts.add(CenterPart())

        val padding = 8.0
        val size = 3.0

        EnumFacing.VALUES.forEach { facing ->
            val piece = ConnectorPiece(facing.getAxisAlignedAABB32(padding, size), facing)
            faces[facing] = piece
            parts.add(piece)
        }

        this.parts = parts.toList()
        this.faces = faces.toMap()
    }

    override fun onPartActivated(player: EntityPlayer, hand: EnumHand, part: IBlockPart, hitBox: IBlockPartHitBox): Boolean {
        if (part is ConnectorPiece) {
            part.setConnection(FluidConnector(part.facing))
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos)
        }
        return true
    }

    override fun getParts(): List<IBlockPart> = this.parts.toList()

    fun getConnectorParts() = this.parts.toList()

    class CenterPart: ConnectorBlockPart() {
        init {
            this.boxes.add(BlockPartHitBox.big16Sized(
                6.0, 6.0, 6.0,
                4.0, 4.0, 4.0
            ))
        }

        override fun getBakery(): IBakery = object: IBakery {
            override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
                val quads = mutableListOf<BakedQuad>()

                EnumFacing.VALUES.fold(RawCube(
                    this@CenterPart.boxes[0].aabb.min.scale(32.0),
                    this@CenterPart.boxes[0].aabb.max.scale(32.0),
                    Minecraft.getMinecraft().textureMapBlocks.missingSprite)
                    .autoUV()
                ) { cube, it ->
                    cube.addFace(it)
                }.bake(quads, vertexFormat, transform)

                return quads
            }
        }.static()
    }
}
