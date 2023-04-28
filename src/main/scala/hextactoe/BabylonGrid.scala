package hextactoe

import scala.util.chaining._
import com.jarrahtechnology.hex.*
import com.jarrahtechnology.hex.Direction._
import BabylonJsHelper._
import typings.babylonjs.global.*
import org.scalajs.dom
import scalajs.js.Thenable.Implicits.thenable2future
import concurrent.ExecutionContext.Implicits.global
import com.jarrahtechnology.util.Vector2    
import com.jarrahtechnology.kassite.shader._

type HexModel = Option[Int]

val lines = List(DirectionPath(List(None, Some(North), Some(North))),
                DirectionPath(List(None, Some(NorthWest), Some(NorthWest))),
                DirectionPath(List(None, Some(SouthWest), Some(SouthWest))),
                DirectionPath(List(None, Some(South), Some(South))),
                DirectionPath(List(None, Some(SouthEast), Some(SouthEast))),
                DirectionPath(List(None, Some(NorthEast), Some(NorthEast))))

// TODO: should HexGridDisplay move in Babylon tools (from hex library)
final case class BabylonGrid[C <: CoordSystem](display: HexGridDisplay[HexModel, C], meshes: List[List[ParameterisedShaderMaterial]], val origin: Vector2) {
    def deriveBoundingBox = {
      val w = display.grid.coords.hexRadiiWidth*display.hexRadius/2d
      val h = display.grid.coords.hexRadiiHeight*display.hexRadius/2d
      (-w+display.grid.coords.rectangularGridRadiiWidth(meshes.size, meshes(0).size), -w, -h+display.grid.coords.rectangularGridRadiiHeight(meshes.size, meshes(0).size), -h);
    }

    // TODO: util method for V2 conversion
    def fromPixel(p: BABYLON.Vector3): Option[(HexModel, Coord)] = {
        val coord = display.fromPixel(Vector2(p.x, p.y) subtract origin)
        display.grid.hexAt(display.fromPixel(Vector2(p.x, p.y) subtract origin)).map((_, coord))
    }

    def toPixel(c: Coord): BABYLON.Vector3 = {
        val p = display.toPixel(c) add origin
        BABYLON.Vector3(p.x, p.y, 0)
    }

    // TODO: use generics so don't need to cast here!
    def claim(actor: Actor, c: Coord) = {
        display.grid.asInstanceOf[MutableRectangularHexGrid[HexModel, C]].set(c, Some(actor.id))
        meshes(c.column)(c.row).setColor3("color", actor.colour)
    }

    def isDraw = display.grid.filter(_._2.isEmpty).isEmpty
    val linePaths = lines.map(_.toPath(display.grid))
    def winner: Option[Int] = display.grid.find((c, h) => xInLine(c, h.getOrElse(-1), 3)).flatMap(_._2)
    def xInLine(c: Coord, id: Int, x: Int) = linePaths.map(_(c)).exists(_.map(_.filter(_._2.getOrElse(-1)==id).length).getOrElse(0)==x)

    def mesh(c: Coord) = meshes.lift(c.column).flatMap(_.lift(c.row))
}

object BabylonGrid {

    private val resolution = 512

    def build(scene: BABYLON.Scene, sizeInHexes: Dimensions, hexRadius: Double) = {
      // TODO: have builders that take width/height as one parameter?
      val coords = CoordSystem.evenVertical
      val model = RectangularHexGrid.mutable(coords, sizeInHexes.width.toInt-1, sizeInHexes.height.toInt-1, (x,y) => Option.empty[Int])
      val display = HexGridDisplay(model, hexRadius)
      val dim = Dimensions.from(model.coords.hexRadiiDimensions.multiply(hexRadius))  
      // TODO: be able to map over hexes with Coords? and then replace below
      val hexTexture = drawFlatTopHexTexture(scene, resolution)
      val origin = calcRadiiOrigin(coords, sizeInHexes) multiply hexRadius
      BabylonGrid(display, (0 until sizeInHexes.width.toInt).toList.map(c => (0 until sizeInHexes.height.toInt).toList.map(r => {
        drawTexture(scene, dim, hexTexture).tap(_._1.position = projectFlatToBabylon3D(origin add display.toPixel(Coord(c, r))))._2
      })), origin)
    }

    // TODO: V2 & V3 can convert to typings.babylonjs.BABYLON.{V2, V3} and project
    def projectFlatToBabylon3D(v: com.jarrahtechnology.util.Vector2) = BABYLON.Vector3(v.x, v.y, 0)

    def calcRadiiOrigin(c: CoordSystem, sizeInHexes: Dimensions) = {
      def shift = {
        if (c.isHorizontal && c.isEven && sizeInHexes.height>1) {
          Vector2(c.hexRadiiWidth, 0)
        } else if (!c.isHorizontal && c.isEven && sizeInHexes.width>1) {
          Vector2(0, c.hexRadiiHeight)
        } else Vector2.zero
      }
      // TODO: put operators back in V2/3 in util -> add goes to addPiecewise
      (shift add c.hexRadiiDimensions subtract c.rectangularGridRadiiDimensions(sizeInHexes.width.toInt, sizeInHexes.height.toInt)) divide 2f
    }
}
