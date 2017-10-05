package net.ndrei.inventoryflow.client

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.ndrei.inventoryflow.MOD_ID
import net.ndrei.teslacorelib.annotations.InitializeDuringConstruction

enum class Textures(path: String) {
    FLUID_CONNECTOR("blocks/fluid_connector");

    val resource = ResourceLocation(MOD_ID, path)

    fun getSprite() = Minecraft.getMinecraft().textureMapBlocks.getTextureExtry(this.resource.toString())
}

@InitializeDuringConstruction
object TexturesRegistry {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    fun stitchTextures(ev: TextureStitchEvent) {
        if (ev.map == Minecraft.getMinecraft().textureMapBlocks) {
            Textures.values().forEach { ev.map.registerSprite(it.resource) }
        }
    }
}
