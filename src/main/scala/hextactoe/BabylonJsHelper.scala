package hextactoe

import org.scalajs.dom.{Event, Image}
import org.scalablytyped.runtime.StringDictionary
import facade.babylonjs.*
import facade.babylonjs.global.BABYLON as BABYLON_IMPL
import facade.babylonjs.anon.{PartialIShaderMaterialOptAttributes, SourcePlane}
import com.jarrahtechnology.util.Vector2
import com.jarrahtechnology.kassite.shader.*
import com.jarrahtechnology.hex.{root3, flatTopHexPixelPoints}

final case class Dimensions(val width: Double, val height: Double) {
    def toOptions = StringDictionary(("width", width), ("height", height))
    def toPlane = SourcePlane.MutableBuilder(SourcePlane()).setWidth(width).setHeight(height)
    def toJtV2 = Vector2(width, height)
    def toBabylonV2 = BABYLON_IMPL.Vector2(width, height)
}

object Dimensions {
  def from(v: Vector2) = Dimensions(v.x, v.y)
  def square(length: Double) = Dimensions(length, length)
  def unitSquare = square(1)
}

object BabylonJsHelper {

  import scalajs.js.Thenable.Implicits.thenable2future
  import concurrent.ExecutionContext.Implicits.global
  def load(path: String) = for {
        response <- org.scalajs.dom.fetch(path)
        text <- response.text()
    } yield text

  def svgWithDimensions(svg: String, resolution: Dimensions) = {
    val svgModified = svg.replace("__WIDTH__", resolution.width.toString).replace("__HEIGHT__", resolution.height.toString)
    s"data:image/svg+xml,${scala.scalajs.js.URIUtils.encodeURIComponent(svgModified)}" 
  }
/*  
  def drawSvg(scene: BABYLON.Scene, svg: String, dimensions: Dimensions, resolution: Dimensions): BABYLON.Mesh = {
    val plane = BABYLON.MeshBuilder.CreatePlane("plane", dimensions.toPlane, scene).asInstanceOf[BABYLON.Mesh]
    val texture = BABYLON.DynamicTexture("svgTexture", resolution.toOptions, scene, true)
    texture.hasAlpha = true 
    plane.material = unlitTransparentMaterial(scene, texture)

    val img = Image()
    img.src = svg
    img.onload = { (e: Event) => {
        texture.getContext().drawImage(img, 0, 0)
        texture.update()
    }}
    plane
  }
*/

  def drawFlatTopHexTexture(scene: BABYLON.Scene, resolution: Int): BABYLON.DynamicTexture = {
    val texture = BABYLON_IMPL.DynamicTexture("svgTexture", StringDictionary(("width", resolution), ("height", resolution*root3/2d)), scene, true)
    texture.hasAlpha = true 
    val ctx = texture.getContext()
    ctx.beginPath();
    ctx.lineWidth = resolution/32
    val pts = flatTopHexPixelPoints.map(_.multiply(resolution-ctx.lineWidth*2).addToAll(ctx.lineWidth)) 
    ctx.moveTo(pts.last.x, pts.last.y)
    pts.foreach(p => ctx.lineTo(p.x, p.y))
    val wrap = pts.last.addPiecewise(pts.head.subtractPiecewise(pts.last).multiply(0.1f))
    ctx.lineTo(wrap.x, wrap.y)    
    ctx.strokeStyle = "white"  
    ctx.stroke()
    texture.update()
    texture
  }

  def drawTexture(scene: BABYLON.Scene, dimensions: Dimensions, texture: BABYLON.DynamicTexture): (BABYLON.Mesh, ParameterisedShaderMaterial) = {
    val plane = BABYLON_IMPL.MeshBuilder.CreatePlane("plane", dimensions.toPlane, scene).asInstanceOf[BABYLON.Mesh]
    val mat = Shaders.unlitTransparent.toMaterial(scene)
    mat.setTexture("tex", texture)
    mat.setFloat("opacity", 0.9)
    mat.alpha = 0.9 
    plane.material = mat
    (plane, mat)
  }
}
