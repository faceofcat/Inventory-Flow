package net.ndrei.inventoryflow.blocks

import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.property.ExtendedBlockState
import net.minecraftforge.common.property.IExtendedBlockState
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.registries.IForgeRegistry
import net.ndrei.inventoryflow.InventoryFlowMod
import net.ndrei.inventoryflow.MOD_ID
import net.ndrei.teslacorelib.blocks.AxisAlignedBlock
import net.ndrei.teslacorelib.blocks.MultiPartBlock

abstract class BaseTileEntityBlock<T: TileEntity>(registryName: String, private val teClass: Class<T>)
    : MultiPartBlock(MOD_ID, InventoryFlowMod.creativeTab, registryName, Material.ROCK), ITileEntityProvider {

    //#region BLOCK STATE & TILE ENTITY

    override fun createBlockState(): BlockStateContainer {
        return ExtendedBlockState(this, arrayOf(AxisAlignedBlock.FACING), arrayOf(CONTAINED_TE))
    }

    override fun getExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState {
        if (state is IExtendedBlockState) {
            val te = world.getTileEntity(pos)
            if ((te != null) && this.teClass.isInstance(te)) {
                return state.withProperty(CONTAINED_TE, this.teClass.cast(te))
            }
        }
        return super.getExtendedState(state, world, pos)
    }

    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity? = this.teClass.newInstance()

    override fun registerBlock(registry: IForgeRegistry<Block>) {
        super.registerBlock(registry)
        GameRegistry.registerTileEntity(this.teClass, this.registryName!!.toString() + "_tile")
    }

    //#endregion

    companion object {
        val CONTAINED_TE = ContainedTileEntityProperty("tile_entity")
    }
}
