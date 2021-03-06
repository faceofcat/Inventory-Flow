package net.ndrei.inventoryflow.blocks.connector

import com.google.common.cache.CacheBuilder
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.common.property.IExtendedBlockState
import net.ndrei.inventoryflow.InventoryFlowMod
import net.ndrei.inventoryflow.blocks.BaseTileEntityBlock
import net.ndrei.teslacorelib.annotations.AutoRegisterBlock
import net.ndrei.teslacorelib.render.selfrendering.IBakery
import net.ndrei.teslacorelib.render.selfrendering.ISelfRenderingBlock
import net.ndrei.teslacorelib.render.selfrendering.SelfRenderingBlock
import net.ndrei.teslacorelib.render.selfrendering.TESRProxy
import net.ndrei.teslapoweredthingies.render.bakery.SelfRenderingTESR
import org.lwjgl.opengl.GL11
import java.util.concurrent.TimeUnit

@AutoRegisterBlock
@SelfRenderingBlock
object ConnectorBlock: BaseTileEntityBlock<ConnectorTile>("connector_host", ConnectorTile::class.java), ISelfRenderingBlock {
    override fun getBakeries(layer: BlockRenderLayer?, state: IBlockState?, stack: ItemStack?, side: EnumFacing?, rand: Long, transform: TRSRTransformation): List<IBakery> {
        val result = mutableListOf<IBakery>()

        if ((layer != null) && (state != null)) {
            // render as block
            result.add(object: IBakery {
                private val cache = CacheBuilder.newBuilder().expireAfterAccess(42, TimeUnit.SECONDS).build<String, MutableList<BakedQuad>>()

                override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
                    val quads = mutableListOf<BakedQuad>()
                    if (state is IExtendedBlockState) {
                        val te = state.getValue(BaseTileEntityBlock.CONTAINED_TE) as? ConnectorTile
                        te?.getConnectorParts()?.forEach {
                            quads.addAll(this.cache.get(it.bakingKey, {
                                InventoryFlowMod.logger.info("Creating part model for '${it.bakingKey}'.")
                                it.getBakery().getQuads(state, stack, side, vertexFormat, transform)
                            }))
                        }
                    }
                    return quads
                }
            })
        }
        else if (stack != null) {
            // render as item
        }

        if (result.size == 0) {
            // TODO: do a default thing
        }

        return result
    }

    override fun renderTESR(proxy: TESRProxy, te: TileEntity, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        proxy.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
//        GlStateManager.disableCull()
        GlStateManager.depthMask(false)

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT)
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

        val tile = te as? ConnectorTile
        tile?.getConnectorParts()?.forEach {
            it.draw(buffer)
        }

        buffer.setTranslation(0.0, 0.0, 0.0)
        tessellator.draw()

        GlStateManager.depthMask(true)
        RenderHelper.enableStandardItemLighting()
    }

    override fun registerRenderer() {
        super.registerRenderer()
        SelfRenderingTESR.registerFor(this.teClass)
    }

    override fun isTranslucent(state: IBlockState?) = true
    override fun getAmbientOcclusionLightValue(state: IBlockState?) = 1.0f

//    override fun getUseNeighborBrightness(state: IBlockState?) = false

    override fun isFullCube(state: IBlockState?) = false
    override fun isFullBlock(state: IBlockState?) = false
    override fun isOpaqueCube(state: IBlockState?) = false

    override fun getLightValue(state: IBlockState?, world: IBlockAccess?, pos: BlockPos?) = 0

    override fun getBlockLayer() = BlockRenderLayer.SOLID
}
