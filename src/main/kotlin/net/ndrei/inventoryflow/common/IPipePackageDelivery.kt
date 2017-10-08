package net.ndrei.inventoryflow.common

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

interface IPipePackageDelivery : INBTSerializable<NBTTagCompound> {
    val destination: IPipeLocation
    val source: IPipeLocation

    val computedPath: Array<IPipeLocation>

    val travelingSpeed: Float
}