package net.ndrei.inventoryflow.common

import net.minecraft.util.EnumFacing

enum class ConnectorSide(val facing: EnumFacing?) {
    NORTH(EnumFacing.NORTH),
    EAST(EnumFacing.EAST),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    UP(EnumFacing.UP),
    DOWN(EnumFacing.DOWN),
    CENTER(null);

    companion object {
        fun fromFacing(facing: EnumFacing?) =
            when (facing) {
                EnumFacing.NORTH -> NORTH
                EnumFacing.EAST -> EAST
                EnumFacing.SOUTH -> SOUTH
                EnumFacing.WEST -> WEST
                EnumFacing.UP -> UP
                EnumFacing.DOWN -> DOWN
                else -> CENTER
            }
    }
}
