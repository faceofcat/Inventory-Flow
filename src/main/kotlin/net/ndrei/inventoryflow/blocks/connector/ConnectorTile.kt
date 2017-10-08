package net.ndrei.inventoryflow.blocks.connector

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BufferBuilder
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
import net.ndrei.inventoryflow.common.ConnectorSide
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

        parts.add(CenterPart(this))

        val padding = 8.0
        val size = 3.0

        EnumFacing.VALUES.forEach { facing ->
            val piece = ConnectorPiece(facing.getAxisAlignedAABB32(padding, size), facing, this)
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

    fun getConnectedSides() =
        EnumFacing.VALUES.filter {
            val target = this.pos.offset(it)
            (this.faces[it]?.hasConnection == true)
                || (this.world.isBlockLoaded(target) && (this.world.getBlockState(target).block == this.getBlockType()))
        }

    fun isConnectedPipe(side: EnumFacing) =
        this.pos.offset(side).let {
            (this.world.isBlockLoaded(it) && (this.world.getBlockState(it).block == this.getBlockType()))
        }

    class CenterPart(private val tile: ConnectorTile): ConnectorBlockPart(ConnectorSide.CENTER) {
        init {
            this.boxes.add(BlockPartHitBox.big16Sized(
                4.0, 4.0, 4.0,
                8.0, 8.0, 8.0
            ))
        }

        override val bakingKey: String
            get() = super.bakingKey + ":" + this.tile.getConnectedSides().joinToString(":") { it.ordinal.toString() }

        override fun getBakery(): IBakery = object: IBakery {
            override fun getQuads(state: IBlockState?, stack: ItemStack?, side: EnumFacing?, vertexFormat: VertexFormat, transform: TRSRTransformation): MutableList<BakedQuad> {
                val quads = mutableListOf<BakedQuad>()

                val from = this@CenterPart.boxes[0].aabb.min.scale(32.0)
                val to = this@CenterPart.boxes[0].aabb.max.scale(32.0)
                val chamfer = 3.0

                val connected = this@CenterPart.tile.getConnectedSides()

                // val connectedAxes = connected.groupBy { it.axis }
                fun hasConnection(vararg face: EnumFacing) =
                    face.all { connected.contains(it) }

                fun hasOnlyConnection(vararg face: EnumFacing) =
                    (face.size == connected.size) && hasConnection(*face)

                val hasOnlyOneConnection = connected.size == 1

                fun hasConnectionOrReverse(vararg face: EnumFacing) =
                    hasConnection(*face) || hasConnection(*face.map { it.opposite }.toTypedArray())

                // FLAT FACES
                var straightPipe: EnumFacing.Axis? = null
                EnumFacing.Axis.values().any {
                    val first = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, it)
                    val second = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, it)
                    val straight = hasOnlyConnection(first, second)
                        && this@CenterPart.tile.isConnectedPipe(first)
                        && this@CenterPart.tile.isConnectedPipe(second)
                    if (straight) {
                        straightPipe = it
                        return@any true
                    }
                    return@any false
                }
                if (straightPipe != null) {
//                    val cube = RawCube(
//                        when (straightPipe!!) {
//                            EnumFacing.Axis.X -> Vec3d(from.x, from.y + chamfer, from.z + chamfer)
//                            EnumFacing.Axis.Y -> Vec3d(from.x + chamfer, from.y, from.z + chamfer)
//                            EnumFacing.Axis.Z -> Vec3d(from.x + chamfer, from.y + chamfer, from.z)
//                        },
//                        when (straightPipe!!) {
//                            EnumFacing.Axis.X -> Vec3d(to.x, to.y - chamfer, to.z - chamfer)
//                            EnumFacing.Axis.Y -> Vec3d(to.x - chamfer, to.y, to.z - chamfer)
//                            EnumFacing.Axis.Z -> Vec3d(to.x - chamfer, to.y - chamfer, to.z)
//                        },
//                        Textures.PIPE.getSprite()
//                    ).autoUV().dualSide()
//
//                    val facing = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, straightPipe!!)
//                    val base = if ((facing == EnumFacing.UP) || (facing == EnumFacing.DOWN)) EnumFacing.NORTH else facing.rotateY()
//                    (0..3).fold(base) { face, _ ->
//                        cube.addFace(face)
//                        face.rotateAround(straightPipe!!)
//                    }
//
//                    cube.bake(quads, vertexFormat, transform)
                    when (straightPipe!!) {
                        EnumFacing.Axis.X ->
                            Vec3d(from.x, from.y + chamfer, from.z + chamfer) to Vec3d(to.x, to.y - chamfer, to.z - chamfer)
                        EnumFacing.Axis.Y ->
                            Vec3d(from.x + chamfer, from.y, from.z + chamfer) to Vec3d(to.x - chamfer, to.y, to.z - chamfer)
                        EnumFacing.Axis.Z ->
                            Vec3d(from.x + chamfer, from.y + chamfer, from.z) to Vec3d(to.x - chamfer, to.y - chamfer, to.z)
                    }
                        .chamfers(straightPipe!!, 1.5, .5f, Textures.PIPE_SOLID.getSprite())
                        .forEach { it.bake(quads, vertexFormat, transform) }
                }
                else {
                    // big ball thingy
                    EnumFacing.Axis.values().forEach {
                        val nOffset = 0.0
                        val pOffset = 0.0

                        val cube = RawCube(
                            when (it) {
                                EnumFacing.Axis.X -> Vec3d(from.x + nOffset, from.y + chamfer, from.z + chamfer)
                                EnumFacing.Axis.Y -> Vec3d(from.x + chamfer, from.y + nOffset, from.z + chamfer)
                                EnumFacing.Axis.Z -> Vec3d(from.x + chamfer, from.y + chamfer, from.z + nOffset)
                            },
                            when (it) {
                                EnumFacing.Axis.X -> Vec3d(to.x - pOffset, to.y - chamfer, to.z - chamfer)
                                EnumFacing.Axis.Y -> Vec3d(to.x - chamfer, to.y - pOffset, to.z - chamfer)
                                EnumFacing.Axis.Z -> Vec3d(to.x - chamfer, to.y - chamfer, to.z - pOffset)
                            },
                            Textures.PIPE.getSprite()
                        ).autoUV().dualSide()

                        EnumFacing.AxisDirection.values().forEach { direction ->
                            val facing = EnumFacing.getFacingFromAxis(direction, it)
                            if (!connected.contains(facing))
                                cube.addFace(facing)
                        }

                        cube.bake(quads, vertexFormat, transform)
                    }
                }

                // THE LUMP
                val lump = RawLump()

                straightPipe = EnumFacing.Axis.X // TODO: remove this to re-enable the lump

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.WEST, EnumFacing.UP) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Up / Front
                        Vec3d(from.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y, to.z - chamfer),
                        Vec3d(from.x, to.y - chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.WEST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.EAST, EnumFacing.UP) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Up / Back
                        Vec3d(to.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x, to.y - chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.EAST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.WEST, EnumFacing.DOWN) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Down / Front
                        Vec3d(from.x, from.y + chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, from.y, from.z + chamfer),
                        Vec3d(from.x + chamfer, from.y, to.z - chamfer),
                        Vec3d(from.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.WEST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.EAST, EnumFacing.DOWN) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Down / Back
                        Vec3d(to.x, from.y + chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.EAST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.SOUTH, EnumFacing.UP) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Up / Left
                        Vec3d(from.x + chamfer, to.y - chamfer, from.z),
                        Vec3d(from.x + chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, from.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.SOUTH, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.NORTH, EnumFacing.UP) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Up / Right
                        Vec3d(from.x + chamfer, to.y - chamfer, to.z),
                        Vec3d(from.x + chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, to.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.NORTH, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.SOUTH, EnumFacing.DOWN) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Down / Left
                        Vec3d(from.x + chamfer, from.y + chamfer, from.z),
                        Vec3d(from.x + chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y, from.z + chamfer),
                        Vec3d(to.x - chamfer, from.y + chamfer, from.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.SOUTH, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.NORTH, EnumFacing.DOWN) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Down / Right
                        Vec3d(from.x + chamfer, from.y + chamfer, to.z),
                        Vec3d(from.x + chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, from.y, to.z - chamfer),
                        Vec3d(to.x - chamfer, from.y + chamfer, to.z)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.NORTH, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.SOUTH, EnumFacing.WEST) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Front / Left
                        Vec3d(from.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(from.x + chamfer, to.y - chamfer, from.z),
                        Vec3d(from.x + chamfer, from.y + chamfer, from.z),
                        Vec3d(from.x, from.y + chamfer, from.z + chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.WEST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.NORTH, EnumFacing.WEST) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Front / Right
                        Vec3d(to.x, to.y - chamfer, from.z + chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, from.z),
                        Vec3d(to.x - chamfer, from.y + chamfer, from.z),
                        Vec3d(to.x, from.y + chamfer, from.z + chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.WEST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.SOUTH, EnumFacing.EAST) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Back / Left
                        Vec3d(from.x, to.y - chamfer, to.z - chamfer),
                        Vec3d(from.x + chamfer, to.y - chamfer, to.z),
                        Vec3d(from.x + chamfer, from.y + chamfer, to.z),
                        Vec3d(from.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.EAST, bothSides = true)
                }

                if (straightPipe == null) { // hasConnectionOrReverse(EnumFacing.NORTH, EnumFacing.EAST) || hasOnlyOneConnection) {
                    lump.addFace(arrayOf( // Back / Right
                        Vec3d(to.x, to.y - chamfer, to.z - chamfer),
                        Vec3d(to.x - chamfer, to.y - chamfer, to.z),
                        Vec3d(to.x - chamfer, from.y + chamfer, to.z),
                        Vec3d(to.x, from.y + chamfer, to.z - chamfer)
                    ), arrayOf(
                        Vec2f(5.5f, 6.0f),
                        Vec2f(5.5f, 4.0f),
                        Vec2f(10.5f, 4.0f),
                        Vec2f(10.5f, 6.0f)
                    ), Textures.PIPE.getSprite(), EnumFacing.EAST, bothSides = true)
                }

                if (straightPipe == null) {
                    lump.addFace(arrayOf( // Front / Left / Up
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

                    lump.addFace(arrayOf( // Front / Left / Down
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

                    lump.addFace(arrayOf( // Front / Right / Up
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

                    lump.addFace(arrayOf( // Front / Right / Down
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

                    lump.addFace(arrayOf( // Back / Left / Up
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

                    lump.addFace(arrayOf( // Back / Left / Down
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

                    lump.addFace(arrayOf( // Back / Right / Up
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

                    lump.addFace(arrayOf( // Back / Right / Down
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
                }

                lump.bake(quads, vertexFormat, transform)

                return quads
            }
        }.static()

        override fun draw(buffer: BufferBuilder) {
            val quads = mutableListOf<BakedQuad>()

            val from = this@CenterPart.boxes[0].aabb.min.scale(32.0)
            val to = this@CenterPart.boxes[0].aabb.max.scale(32.0)
            val chamfer = 3.5

            val connected = this@CenterPart.tile.getConnectedSides()

            fun hasConnection(vararg face: EnumFacing) =
                face.all { connected.contains(it) }

            fun hasOnlyConnection(vararg face: EnumFacing) =
                (face.size == connected.size) && hasConnection(*face)

            // FLAT FACES
            var straightPipe: EnumFacing.Axis? = null
            EnumFacing.Axis.values().any {
                val first = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, it)
                val second = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, it)
                val straight = hasOnlyConnection(first, second)
                    && this@CenterPart.tile.isConnectedPipe(first)
                    && this@CenterPart.tile.isConnectedPipe(second)
                if (straight) {
                    straightPipe = it
                    return@any true
                }
                return@any false
            }
            if (straightPipe != null) {
                val cube = RawCube(
                    when (straightPipe!!) {
                        EnumFacing.Axis.X -> Vec3d(from.x, from.y + chamfer, from.z + chamfer)
                        EnumFacing.Axis.Y -> Vec3d(from.x + chamfer, from.y, from.z + chamfer)
                        EnumFacing.Axis.Z -> Vec3d(from.x + chamfer, from.y + chamfer, from.z)
                    },
                    when (straightPipe!!) {
                        EnumFacing.Axis.X -> Vec3d(to.x, to.y - chamfer, to.z - chamfer)
                        EnumFacing.Axis.Y -> Vec3d(to.x - chamfer, to.y, to.z - chamfer)
                        EnumFacing.Axis.Z -> Vec3d(to.x - chamfer, to.y - chamfer, to.z)
                    },
                    Textures.PIPE_TRANSPARENT.getSprite()
                ).autoUV().dualSide()

                val facing = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, straightPipe!!)
                val base = if ((facing == EnumFacing.UP) || (facing == EnumFacing.DOWN)) EnumFacing.NORTH else facing.rotateY()
                (0..3).fold(base) { face, _ ->
                    cube.addFace(face)
                    face.rotateAround(straightPipe!!)
                }

                cube.draw(buffer)
            }
        }
    }
}
