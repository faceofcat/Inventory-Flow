package net.ndrei.inventoryflow.connectors

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable
import net.ndrei.inventoryflow.common.ConnectorSide
import net.ndrei.teslacorelib.blocks.multipart.BlockPart

abstract class ConnectorBlockPart(val side: ConnectorSide) : BlockPart(), IFlowConnectorPart, INBTSerializable<NBTTagCompound> {
    //#region serialization

    override fun serializeNBT(): NBTTagCompound {
        return NBTTagCompound()
    }

    override fun deserializeNBT(nbt: NBTTagCompound?) {
    }

    //#endregion

    override val bakingKey: String
        get() = "${this.side.name}::${this.javaClass.name}"
}
