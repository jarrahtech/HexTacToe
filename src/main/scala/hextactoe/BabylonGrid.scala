package hextactoe

import scala.util.chaining.*
import com.jarrahtechnology.hex.*
import com.jarrahtechnology.hex.Direction.*
import BabylonJsHelper.*
import facade.babylonjs.*
import facade.babylonjs.global.BABYLON as BABYLON_IMPL
import org.scalajs.dom
import scalajs.js.Thenable.Implicits.thenable2future
import concurrent.ExecutionContext.Implicits.global
import com.jarrahtechnology.util.Vector2    
import com.jarrahtechnology.kassite.shader.*
import com.jarrahtechnology.kassite.util.VectorConvert.*

type HexModel = Option[Int]

val lines = List(DirectionPath(List(None, Some(North), Some(North))),
                DirectionPath(List(None, Some(NorthWest), Some(NorthWest))),
                DirectionPath(List(None, Some(SouthWest), Some(SouthWest))),
                DirectionPath(List(None, Some(South), Some(South))),
                DirectionPath(List(None, Some(SouthEast), Some(SouthEast))),
                DirectionPath(List(None, Some(NorthEast), Some(NorthEast))))

// TODO: should HexGridDisplay move in Babylon tools (from hex library)
final case class BabylonGrid[C <: CoordSystem](display: HexGridDisplay[HexModel, C], meshes: List[List[ParameterisedShaderMaterial]], val origin: Vector2) {

    def fromPixel(p: BABYLON.Vector3): Option[(HexModel, Coord)] = {
      val coord = display.fromPixel(toV2Flat(p) subtractPiecewise origin)
      display.grid.hexAt(coord).map((_, coord))
    }

    def toPixel(c: Coord): BABYLON.Vector3 = toV3Flat(display.toPixel(c) addPiecewise origin)

    // TODO: use generics so don't need to cast here!
    def claim(actor: GameActor, c: Coord) = {
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
        drawTexture(scene, dim, hexTexture).tap(_._1.position = toV3Flat(origin addPiecewise display.toPixel(Coord(c, r))))._2
      })), origin)
    }

    def calcRadiiOrigin(c: CoordSystem, sizeInHexes: Dimensions) = {
      def shift = {
        if (c.isHorizontal && c.isEven && sizeInHexes.height>1) {
          Vector2(c.hexRadiiWidth, 0)
        } else if (!c.isHorizontal && c.isEven && sizeInHexes.width>1) {
          Vector2(0, c.hexRadiiHeight)
        } else Vector2.zero
      }

      (shift addPiecewise c.hexRadiiDimensions subtractPiecewise c.rectangularGridRadiiDimensions(sizeInHexes.width.toInt, sizeInHexes.height.toInt)) divide 2f
    }
}

final case class RectangularHexGridDisplay[Model, View, C <: CoordSystem, G <: RectangularHexGrid[(Model, View), C]](val grid: G, val hexRadius: Double, position: Vector2) {
    
  def fromPixel(pos: Vector2) = grid.coords.fromRadii((pos subtractPiecewise pixelCenter).divide(hexRadius))
  def hexFromPixel(pos: Vector2) = {
    val coord = fromPixel(pos)
    (coord, grid.hexAt(coord))
  }
  def toPixel(coord: Coord) = grid.coords.toRadii(coord).multiply(hexRadius) addPiecewise pixelCenter

  val pixelCenter = (gridCentreToOriginInRadii multiply hexRadius) addPiecewise position
  def gridCentreToOriginInRadii = { // shift from the center of the grid to the center of the (0,0) hex
      def shift = 
        if (grid.coords.isHorizontal && grid.coords.isEven && grid.numRows>1) {
          Vector2(grid.coords.hexRadiiWidth, 0)
        } else if (!grid.coords.isHorizontal && grid.coords.isEven && grid.numColumns>1) {
          Vector2(0, grid.coords.hexRadiiHeight)
        } else Vector2.zero

      (shift addPiecewise grid.coords.hexRadiiDimensions subtractPiecewise grid.coords.rectangularGridRadiiDimensions(grid.numColumns, grid.numRows)) divide 2f
    }
}

object RectangularHexGridDisplay {

}