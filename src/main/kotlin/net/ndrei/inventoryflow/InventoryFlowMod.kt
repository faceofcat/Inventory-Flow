package net.ndrei.inventoryflow

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLConstructionEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.ndrei.teslacorelib.BaseProxy
import net.ndrei.teslacorelib.config.ModConfigHandler
import net.ndrei.teslacorelib.config.TeslaCoreLibConfig
import net.ndrei.teslacorelib.items.gears.CoreGearType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(modid = MOD_ID, version = MOD_VERSION, name = MOD_NAME,
        acceptedMinecraftVersions = MOD_MC_VERSION,
        dependencies = MOD_DEPENDENCIES,
        useMetadata = true, modLanguage = "kotlin", modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter")
object InventoryFlowMod {
    @SidedProxy(clientSide = "net.ndrei.inventoryflow.ClientProxy", serverSide = "net.ndrei.inventoryflow.ServerProxy")
    lateinit var proxy: BaseProxy
    lateinit var logger: Logger

    lateinit var config: ModConfigHandler

    val creativeTab: CreativeTabs by lazy {
        object : CreativeTabs(MOD_NAME) {
            override fun getTabIconItem() = ItemStack(Blocks.HOPPER)
        }
    }

    @Mod.EventHandler
    fun construct(event: FMLConstructionEvent) {
        arrayOf(
            TeslaCoreLibConfig.REGISTER_GEARS,
            "${TeslaCoreLibConfig.REGISTER_GEAR_TYPES}#${CoreGearType.WOOD.material}",
            "${TeslaCoreLibConfig.REGISTER_GEAR_TYPES}#${CoreGearType.STONE.material}",
            "${TeslaCoreLibConfig.REGISTER_GEAR_TYPES}#${CoreGearType.IRON.material}",
            "${TeslaCoreLibConfig.REGISTER_GEAR_TYPES}#${CoreGearType.GOLD.material}",
            "${TeslaCoreLibConfig.REGISTER_GEAR_TYPES}#${CoreGearType.DIAMOND.material}",
            TeslaCoreLibConfig.REGISTER_BATTERY,
            TeslaCoreLibConfig.REGISTER_MACHINE_CASE
        ).forEach {
            TeslaCoreLibConfig.setDefaultFlag(it, true)
        }

        this.logger = LogManager.getLogger(Loader.instance().activeModContainer()!!.modId)
        this.proxy.construction(event)
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        InventoryFlowMod.config = ModConfigHandler(MOD_ID, this.javaClass, this.logger, event.modConfigurationDirectory)
        this.proxy.preInit(event)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        this.proxy.init(event)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        this.proxy.postInit(event)
    }
}
