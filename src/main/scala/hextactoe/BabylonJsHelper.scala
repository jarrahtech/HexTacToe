package hextactoe

import org.scalajs.dom.{Event, Image}
import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.global.*
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes

final case class Dimensions(val width: Int, val height: Int)
object Dimensions {
  def square(length: Int) = Dimensions(length, length)
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
  
  def drawSvg(scene: BABYLON.Scene, svg: String, dimensions: Dimensions): BABYLON.Mesh = {
    val plane = BABYLON.MeshBuilder.CreatePlane("plane", nullPlaneOptions, scene).asInstanceOf[BABYLON.Mesh]
    val texture = BABYLON.DynamicTexture("svgTexture", 256, scene)
    texture.hasAlpha = true 

    val mat = new BABYLON.ShaderMaterial("shader", scene, unlitTransparentShaderPath, unlitTransparentShaderOpts)
    mat.setTexture("textureSampler", texture)
    mat.setVector3("color", BABYLON.Vector3(1,1,1));
    mat.setFloat("opacity", 1);
    mat.alpha = 0.9 
    plane.material = mat

    val img = Image()
    val svgModified = svg.replace("__WIDTH__", dimensions.width.toString).replace("__HEIGHT__", dimensions.height.toString)
    img.src = s"data:image/svg+xml,${scala.scalajs.js.URIUtils.encodeURIComponent(svgModified)}" 
    img.onload = { (e: Event) => {
        texture.getContext().drawImage(img, 0, 0)
        texture.update()
    }}
    plane
  }
}
