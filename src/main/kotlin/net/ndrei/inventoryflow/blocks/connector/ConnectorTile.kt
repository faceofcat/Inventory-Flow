package net.ndrei.inventoryflow.blocks.connector

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.model.TRSRTransformation
import net.ndrei.inventoryflow.client.Textures
import net.ndrei.inventoryflow.connectors.ConnectorBlockPart
import net.ndrei.inventoryflow.connectors.FluidConnector
import net.ndrei.inventoryflow.connectors.IFlowConnectorPart
import net.ndrei.teslacorelib.blocks.multipart.BlockPartHitBox
import net.ndrei.teslacorelib.blocks.multipart.IBlockPart
import net.ndrei.teslacorelib.blocks.multipart.IBlockPartHitBox
import net.ndrei.teslacorelib.blocks.multipart.IBlockPartProvider
import net.ndrei.teslacorelib.render.selfrendering.*

class ConnectorTile : TileEntity(), IBlockPartProvider {
    private val parts: List<IFlowConnectorPart>
    private val faces: Map<EnumFacing, ConnectorPiece>

    init {
        val parts = mutableListOf<IFlowConnectorPart>()
        val faces = mutableMapOf<EnumFacing, ConnectorPiece>()

        parts.add(CenterPart())

        val padding = 8.0
        val size = 3.0

        EnumFacing.VALUES.forEach { facing ->
            val piece = ConnectorPiece(facing.getAxisAlignedAABB32(padding, size), facing)
            faces[facing] = piece
            parts.add(piece)
        }

        this.parts = parts.toList()
        this.faces = faces.toMap()
    }

    override fun onPartActivated(player: EntityPlayer, hand: EnumHand, part: IBlockPart, hitBox: IBlockPartHitBox): Boolean {
        if (part is ConnectorPiece) {
            part.setConnection(FluidConnector(part.facing))
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos)
        }
        return true
    }

    override fun getParts(): List<IBlockPart> = this.parts.toList()

    fun getConnectorParts() = this.parts.toList()

    class CenterPart: ConnectorBlockPart() {
        init {
            this.boxes.add(BlockPartHitBox.big16Sized(
                4.0, 4.0, 4.0,
                8.0, 8.0, 8.0
            ))
        }

        override fun getBakery(): IBakery = object: IBakery {
            override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
                val quads = mutableListOf<BakedQuad>()

//                EnumFacing.VALUES.fold(RawCube(
//                    this@CenterPart.boxes[0].aabb.min.scale(32.0),
//                    this@CenterPart.boxes[0].aabb.max.scale(32.0),
//                    Textures.PIPE.getSprite())
//                    .autoUV()
//                ) { cube, it ->
//                    cube.addFace(it)
//                }.bake(quads, vertexFormat, transform)

                val from = this@CenterPart.boxes[0].aabb.min.scale(32.0)
                val to = this@CenterPart.boxes[0].aabb.max.scale(32.0)
                val chamfer = 3.0

                // X Axis
                RawCube(
                    Vec3d(from.x, from.y + chamfer, from.z + chamfer),
                    Vec3d(to.x, to.y - chamfer, to.z - chamfer),
                    Textures.PIPE.getSprite()
                ).autoUV().dualSide()
                    .addFace(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X))
                    .addFace(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X))
                    .bake(quads, vertexFormat, transform)

                // Z AXIS
                RawCube(
                    Vec3d(from.x + chamfer, from.y + chamfer, from.z),
                    Vec3d(to.x - chamfer, to.y - chamfer, to.z),
                    Textures.PIPE.getSprite()
                ).autoUV().dualSide()
                    .addFace(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z))
                    .addFace(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z))
                    .bake(quads, vertexFormat, transform)

                // Y AXIS
                RawCube(
                    Vec3d(from.x + chamfer, from.y, from.z + chamfer),
                    Vec3d(to.x - chamfer, to.y, to.z - chamfer),
                    Textures.PIPE.getSprite()
                ).autoUV().dualSide()
                    .addFace(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y))
                    .addFace(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y))
                    .bake(quads, vertexFormat, transform)

                // THE LUMP
                RawLump()
                    .addFace(arrayOf( // Up / Front
                        Vec3d(from.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y, to.z - chamfer),
                        Vec3d(from.x, to.y - chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Up / Back
                        Vec3d(to.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x, to.y - chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Down / Front
                        Vec3d(from.x, from.y + chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, from.y, from.z + chamfer),
                        Vec3d(from.x + chamfer, from.y, to.z - chamfer),
                        Vec3d(from.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Down / Back
                        Vec3d(to.x, from.y + chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Down / Left
                        Vec3d(from.x + chamfer, to.y - chamfer, from.z),
                        Vec3d(from.x + chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, from.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z),
                        bothSides = true)

                    .addFace(arrayOf( // Up / Right
                        Vec3d(from.x + chamfer, to.y - chamfer, to.z),
                        Vec3d(from.x + chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, to.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z),
                        bothSides = true)

                    .addFace(arrayOf( // Down / Left
                        Vec3d(from.x + chamfer, from.y + chamfer, from.z),
                        Vec3d(from.x + chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y + chamfer, from.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z),
                        bothSides = true)

                    .addFace(arrayOf( // Down / Right
                        Vec3d(from.x + chamfer, from.y + chamfer, to.z),
                        Vec3d(from.x + chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, from.y + chamfer, to.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z),
                        bothSides = true)

                    .addFace(arrayOf( // Front / Left
                        Vec3d(from.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y - chamfer, from.z),
                        Vec3d(from.x + chamfer, from.y + chamfer, from.z),
                        Vec3d(from.x, from.y + chamfer, from.z + chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Front / Right
                        Vec3d(to.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, from.z),
                        Vec3d(to.x - chamfer, from.y + chamfer, from.z),
                        Vec3d(to.x, from.y + chamfer, from.z + chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Back / Left
                        Vec3d(from.x, to.y - chamfer, to.z - chamfer),
                        Vec3d(from.x + chamfer, to.y - chamfer, to.z),
                        Vec3d(from.x + chamfer, from.y + chamfer, to.z),
                        Vec3d(from.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Back / Right
                        Vec3d(to.x, to.y - chamfer, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, to.z),
                        Vec3d(to.x - chamfer, from.y + chamfer, to.z),
                        Vec3d(to.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Front / Left / Up
                        Vec3d(from.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y - chamfer, from.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Front / Left / Down
                        Vec3d(from.x, from.y + chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, from.y, from.z + chamfer),
                        Vec3d(from.x + chamfer, from.y + chamfer, from.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Front / Right / Up
                        Vec3d(to.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, from.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Front / Right / Down
                        Vec3d(to.x, from.y + chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y + chamfer, from.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
                        bothSides = true)
                    
                    .addFace(arrayOf( // Back / Left / Up
                        Vec3d(from.x, to.y - chamfer, to.z - chamfer),
                        Vec3d(from.x + chamfer, to.y, to.z - chamfer),
                        Vec3d(from.x + chamfer, to.y - chamfer, to.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Back / Left / Down
                        Vec3d(from.x, from.y + chamfer, to.z - chamfer),
                        Vec3d(from.x + chamfer, from.y, to.z - chamfer),
                        Vec3d(from.x + chamfer, from.y + chamfer, to.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Back / Right / Up
                        Vec3d(to.x, to.y - chamfer, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, to.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .addFace(arrayOf( // Back / Right / Down
                        Vec3d(to.x, from.y + chamfer, to.z - chamfer),
                        Vec3d(to.x - chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, from.y + chamfer, to.z)
                    ), arrayOf(
                        Vec2f(4.0f, 5.5f),
                        Vec2f(4.0f, 4.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(5.5f, 5.5f)
                    ), Textures.PIPE.getSprite(),
                        EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X),
                        bothSides = true)

                    .bake(quads, vertexFormat, transform)

                return quads
            }
        }.static()
    }
}
