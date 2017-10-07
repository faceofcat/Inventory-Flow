package net.ndrei.inventoryflow.connectors

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
                EnumFacing.NORTH -> ConnectorSide.NORTH
                EnumFacing.EAST -> ConnectorSide.EAST
                EnumFacing.SOUTH -> ConnectorSide.SOUTH
                EnumFacing.WEST -> ConnectorSide.WEST
                EnumFacing.UP -> ConnectorSide.UP
                EnumFacing.DOWN -> ConnectorSide.DOWN
                else -> CENTER
            }
    }
}
