package net.ndrei.inventoryflow.common

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

interface IPipePackagePosition : INBTSerializable<NBTTagCompound> {
    val travelingFrom: ConnectorSide
    val travelingTo: ConnectorSide

    val relativePosition: Float
}
