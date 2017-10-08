package net.ndrei.inventoryflow.common

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

interface IPipePackage: INBTSerializable<NBTTagCompound> {
    val deliveryInfo: IPipePackageDelivery
    val currentPosition: IPipePackagePosition

    val information: Array<String>
}
