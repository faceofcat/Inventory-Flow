package net.ndrei.inventoryflow.blocks

import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityNote
import net.minecraftforge.common.property.IUnlistedProperty

class ContainedTileEntityProperty(private val propName: String) : IUnlistedProperty<TileEntity> {
    override fun getName() = this.propName
    override fun isValid(value: TileEntity) = true
    override fun valueToString(value: TileEntity) = value.javaClass.name!!
    override fun getType(): Class<TileEntity> = TileEntity::class.java
}
