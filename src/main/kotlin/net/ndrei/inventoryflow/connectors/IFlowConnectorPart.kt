package net.ndrei.inventoryflow.connectors

import net.minecraft.client.renderer.BufferBuilder
import net.ndrei.teslacorelib.blocks.multipart.IBlockPart
import net.ndrei.teslacorelib.render.selfrendering.EmptyBakery
import net.ndrei.teslacorelib.render.selfrendering.IBakery

interface IFlowConnectorPart : IBlockPart {
    fun getBakery(): IBakery = EmptyBakery
    fun draw(buffer: BufferBuilder) { }
    val bakingKey: String
}
