package net.ndrei.inventoryflow.blocks.connector

import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.common.property.IExtendedBlockState
import net.ndrei.inventoryflow.blocks.BaseTileEntityBlock
import net.ndrei.teslacorelib.annotations.AutoRegisterBlock
import net.ndrei.teslacorelib.render.selfrendering.IBakery
import net.ndrei.teslacorelib.render.selfrendering.ISelfRenderingBlock
import net.ndrei.teslacorelib.render.selfrendering.SelfRenderingBlock

@AutoRegisterBlock
@SelfRenderingBlock
object ConnectorBlock: BaseTileEntityBlock<ConnectorTile>("connector_host", ConnectorTile::class.java), ISelfRenderingBlock {
    override fun getBakeries(layer: BlockRenderLayer?, state: IBlockState?, stack: ItemStack?, side: EnumFacing?, rand: Long, transform: TRSRTransformation): List<IBakery> {
        val result = mutableListOf<IBakery>()

        if ((layer != null) && (state != null)) {
            // render as block
            if (state is IExtendedBlockState) {
                val te = state.getValue(BaseTileEntityBlock.CONTAINED_TE) as? ConnectorTile
                te?.getConnectorParts()?.mapTo(result) { it.getBakery() }
            }
        }
        else if (stack != null) {
            // render as item
        }

        if (result.size == 0) {
            // TODO: do a default thing
        }

        return result
    }

    override fun isTranslucent(state: IBlockState?) = true
    override fun getAmbientOcclusionLightValue(state: IBlockState?) = 1.0f

    override fun getBlockLayer() = BlockRenderLayer.TRANSLUCENT
}
