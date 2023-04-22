package hextactoe

import org.scalajs.dom.{Event, Image}
import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.global.*
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes
import com.jarrahtechnology.util.Vector2

final case class Dimensions(val width: Double, val height: Double) {
    def toOptions = StringDictionary(("width", width), ("height", height))
    def toPlane = typings.babylonjs.anon.SourcePlane.MutableBuilder(typings.babylonjs.anon.SourcePlane()).setWidth(width).setHeight(height)
    def toVector2 = Vector2(width, height)
}
object Dimensions {
  def from(v: Vector2) = Dimensions(v.x, v.y)
  def square(length: Double) = Dimensions(length, length)
  def unitSquare = square(1)
}

object BabylonJsHelper {

  def loadUnlitTransparentShader = {
    BABYLON.Effect.ShadersStore.addOne(("basicVertexShader", "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}"))
    BABYLON.Effect.ShadersStore.addOne(("unlitFragmentShader", "precision highp float;varying vec2 vUV;uniform sampler2D textureSampler;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=texture2D(textureSampler,vUV)*vec4(color,opacity);}"))
  }

  val unlitTransparentShaderPath = StringDictionary(("vertex", "basic"),("fragment", "unlit"))
  val unlitTransparentShaderOpts = PartialIShaderMaterialOptAttributes.MutableBuilder(PartialIShaderMaterialOptAttributes())
        .setAttributesVarargs("position", "uv")
        .setUniformBuffersVarargs("worldViewProjection", "color", "opacity")
        .setSamplersVarargs("textureSampler")

  val nullPlaneOptions = typings.babylonjs.anon.SourcePlane()

  def unlitTransparentMaterial(scene: BABYLON.Scene, texture: BABYLON.DynamicTexture) = {
    val mat = new BABYLON.ShaderMaterial("shader", scene, unlitTransparentShaderPath, unlitTransparentShaderOpts)
    mat.setTexture("textureSampler", texture)
    mat.setVector3("color", BABYLON.Vector3(1,1,1));
    mat.setFloat("opacity", 1);
    mat.alpha = 0.99 // <1 so bg disappears
    mat
  }

  import scalajs.js.Thenable.Implicits.thenable2future
  import concurrent.ExecutionContext.Implicits.global
  def load(path: String) = for {
        response <- org.scalajs.dom.fetch("/hexagon.svg")
        text <- response.text()
    } yield text

  def svgWithDimensions(svg: String, resolution: Dimensions) = {
    val svgModified = svg.replace("__WIDTH__", resolution.width.toString).replace("__HEIGHT__", resolution.height.toString)
    s"data:image/svg+xml,${scala.scalajs.js.URIUtils.encodeURIComponent(svgModified)}" 
  }
  
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

  // TODO: move to hex library?
  val root3 = math.sqrt(3)
  val flatTopHexPoints = List(Vector2(0, root3/4d), Vector2(0.25, root3/2d), Vector2(0.75, root3/2d), Vector2(1, root3/4d), Vector2(0.75, 0), Vector2(0.25, 0))

   def drawFlatTopHexTexture(scene: BABYLON.Scene, resolution: Int): BABYLON.DynamicTexture = {
    val texture = BABYLON.DynamicTexture("svgTexture", StringDictionary(("width", resolution), ("height", resolution*root3/2d)), scene, true)
    texture.hasAlpha = true 
    val ctx = texture.getContext()
    ctx.beginPath();
    ctx.lineWidth = resolution/32
    // TODO: Vector2.addToAll(Double) and use here
    val pts = flatTopHexPoints.map(_.multiply(resolution-ctx.lineWidth*2).add(Vector2(ctx.lineWidth, ctx.lineWidth))) 
    ctx.moveTo(pts.last.x, pts.last.y)
    pts.foreach(p => ctx.lineTo(p.x, p.y))
    val wrap = pts.last.add(pts.head.subtract(pts.last).multiply(0.1f))
    ctx.lineTo(wrap.x, wrap.y)    
    ctx.strokeStyle = "white"  
    ctx.stroke()
    texture.update()
    texture
   }

  def drawTexture(scene: BABYLON.Scene, dimensions: Dimensions, texture: BABYLON.DynamicTexture): BABYLON.Mesh = {
    val plane = BABYLON.MeshBuilder.CreatePlane("plane", dimensions.toPlane, scene).asInstanceOf[BABYLON.Mesh]
    val mat = new BABYLON.ShaderMaterial("shader", scene, unlitTransparentShaderPath, unlitTransparentShaderOpts)
    mat.setTexture("textureSampler", texture)
    mat.setVector3("color", BABYLON.Vector3(1,1,1));
    mat.setFloat("opacity", 0.9);
    mat.alpha = 0.9 
    plane.material = mat
    plane
  }
}
