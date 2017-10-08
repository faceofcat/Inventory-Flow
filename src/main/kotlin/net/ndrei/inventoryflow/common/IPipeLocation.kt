package net.ndrei.inventoryflow.common

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

interface IPipeLocation : INBTSerializable<NBTTagCompound> {
    val dimensionId: Int
    val position: BlockPos
    val side: ConnectorSide
}
