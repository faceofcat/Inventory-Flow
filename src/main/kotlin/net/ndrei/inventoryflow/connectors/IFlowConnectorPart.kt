package net.ndrei.inventoryflow.connectors

import net.ndrei.teslacorelib.blocks.multipart.IBlockPart
import net.ndrei.teslacorelib.render.selfrendering.IBakery

interface IFlowConnectorPart : IBlockPart {
    fun getBakery(): IBakery
}
