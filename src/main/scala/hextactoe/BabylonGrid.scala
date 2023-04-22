package hextactoe

import scala.util.chaining._
import com.jarrahtechnology.hex.*
import BabylonJsHelper._
import typings.babylonjs.global.*
import org.scalajs.dom
import scalajs.js.Thenable.Implicits.thenable2future
import concurrent.ExecutionContext.Implicits.global

final case class BabylonGrid[C <: CoordSystem](display: HexGridDisplay[Option[Int], C], meshes: List[List[BABYLON.Mesh]]) {

}

object BabylonGrid {

    private val resolution = 512

    val svg = for {
        response <- dom.fetch("/hexagon.svg")
        svg <- response.text()
    } yield svg
  
    def build(scene: BABYLON.Scene, sizeInHexes: Dimensions, hexRadius: Double) = {
      // TODO: have builders that take width/height as one parameter
      val model = RectangularHexGrid.immutable(CoordSystem.evenVertical, sizeInHexes.width.toInt-1, sizeInHexes.height.toInt-1, (x,y) => Option.empty[Int])
      val display = HexGridDisplay(model, hexRadius)
      svg.map(s => 
        val dim = Dimensions.from(model.coords.hexRadiiDimensions.multiply(hexRadius))  
        // TODO: be able to map over hexes with Coords? and then replace below
        val hexTexture = drawFlatTopHexTexture(scene, resolution)
        BabylonGrid(display, (0 until sizeInHexes.width.toInt).toList.map(c => (0 until sizeInHexes.height.toInt).toList.map(r => {
            //drawSvg(scene, hexSvg, dim, res)
            // TODO: if use this then share the image
            drawTexture(scene, dim, hexTexture).tap(_.position = projectFlatToBabylon3D(display.toPixel(Coord(c, r))))
        })))
      )
    }

    // TODO: V2 & V3 can convert to typings.babylonjs.BABYLON.{V2, V3} and project
    def projectFlatToBabylon3D(v: com.jarrahtechnology.util.Vector2) = BABYLON.Vector3(v.x, v.y, 0)
}

/* TODO: tweens

scene.onBeforeRenderObservable(function () {
  //Your code here https://playground.babylonjs.com/#835Y0X
});
scene.getAnimationRatio
https://forum.babylonjs.com/t/how-to-use-delta-time/27501/2
*/
