package net.ndrei.inventoryflow.connectors

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable
import net.ndrei.teslacorelib.blocks.multipart.BlockPart

abstract class ConnectorBlockPart : BlockPart(), IFlowConnectorPart, INBTSerializable<NBTTagCompound> {
    //#region serialization

    override fun serializeNBT(): NBTTagCompound {
        return NBTTagCompound()
    }

    override fun deserializeNBT(nbt: NBTTagCompound?) {
    }

    //#endregion
}
