package net.ndrei.inventoryflow

import net.minecraftforge.fml.relauncher.Side
import net.ndrei.teslacorelib.BaseProxy

open class CommonProxy(side: Side) : BaseProxy(side)

@Suppress("unused")
class ClientProxy : CommonProxy(Side.CLIENT)

@Suppress("unused")
class ServerProxy : CommonProxy(Side.SERVER)
