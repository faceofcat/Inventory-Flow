package net.ndrei.inventoryflow.connectors

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.model.TRSRTransformation
import net.ndrei.inventoryflow.client.Textures
import net.ndrei.teslacorelib.blocks.multipart.BlockPartHitBox
import net.ndrei.teslacorelib.render.selfrendering.*

class FluidConnector(private val facing: EnumFacing): ConnectorBlockPart() {
    init {
        this.boxes.add(BlockPartHitBox(this.facing.getAxisAlignedAABB32(10.0, 2.0)))
        this.boxes.add(BlockPartHitBox(this.facing.getAxisAlignedAABB32(14.0, 4.0, 2.0)))
    }

    override fun getBakery() = object: IBakery {
        override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
            val quads = mutableListOf<BakedQuad>()

            val big1 = this@FluidConnector.facing.getAxisAlignedAABB32(10.0, 1.0)
            RawCube(big1.min.scale(32.0), big1.max.scale(32.0), Textures.FLUID_CONNECTOR.getSprite()).autoUV()
                .addMissingFaces()
                .bake(quads, vertexFormat, transform)

            val big2 = this@FluidConnector.facing.getAxisAlignedAABB32(11.0, 1.0, 1.0)
            RawCube(big2.min.scale(32.0), big2.max.scale(32.0), Textures.FLUID_CONNECTOR.getSprite()).autoUV()
                .addMissingFaces()
                .bake(quads, vertexFormat, transform)

            val small = this@FluidConnector.facing.getAxisAlignedAABB32(14.0, 4.0, 2.0)
            RawCube(small.min.scale(32.0), small.max.scale(32.0), Textures.FLUID_CONNECTOR.getSprite()).autoUV()
                .addMissingFaces()
                .bake(quads, vertexFormat, transform)

            return quads
        }
    }
}
