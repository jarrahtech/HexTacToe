package hextactoe

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import org.scalajs.dom.{Event, Image, SVGImageElement, XMLSerializer}
import org.scalajs.dom.window
import typings.babylonjs.BABYLON.Material
import org.scalajs.dom.SVGImageElement
import concurrent.ExecutionContext.Implicits.global

@main
def HexTacToe(): Unit = {
  BABYLON.Effect.ShadersStore.addOne(("basicVertexShader", "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}"))
  BABYLON.Effect.ShadersStore.addOne(("unlitFragmentShader", "precision highp float;varying vec2 vUV;uniform sampler2D textureSampler;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=texture2D(textureSampler,vUV)*vec4(color,opacity);}"))

  renderOnDomContentLoaded(dom.document.getElementById("app"), Main.appElement())
  renderOnDomContentLoaded(dom.document.getElementById("test"), counterButton())
  createBabylon()
}

object Main {
  def appElement(): Element = {
    div(
      a(href := "https://vitejs.dev", target := "_blank",
        img(src := "/vite.svg", className := "logo", alt := "Vite logo"),
      ),
      img(src := "/binoculars_fill.svg", className := "logo", alt := "binoculars"),
      h1("Hello Laminar & Scala.js & Babylon & ScalablyTyped!"),
      div(idAttr := "test",
        className := "card",
      ),
      p(className := "read-the-docs",
        "Click on the Vite logo to learn more",
      ),
    )
  }
}

def counterButton(): Element = {
    val counter = Var(0)
    button(
      tpe := "button",
      "count is ",
      child.text <-- counter,
      onClick --> { event => counter.update(c => c + 1) },
    )
  }

import typings.babylonjs.HTMLCanvasElement
import typings.babylonjs.global.*

@js.native @JSImport("/binoculars_fill.svg", JSImport.Default)
val binocularsSvg: String = js.native

def createBabylon() = {
  val canvas = dom.document.getElementById("renderCanvas").asInstanceOf[typings.babylonjs.HTMLCanvasElement]
  val engine = new BABYLON.Engine(canvas, true) // Generate the BABYLON 3D engine
  val scene = new BABYLON.Scene(engine)
  val camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 5, -10), scene)
  camera.setTarget(BABYLON.Vector3.Zero())
  val light = new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene)
  light.intensity = 0.7
  val planeOptions = typings.babylonjs.anon.SourcePlane()
  val plane = BABYLON.MeshBuilder.CreatePlane("plane", planeOptions, scene).asInstanceOf[BABYLON.Mesh]
  drawSvgToMesh(plane, scene, binocularsSvg)
  engine.runRenderLoop(()=>{ scene.render() })  

  window.addEventListener("resize", _ => engine.resize())
  window.addEventListener("load", _ => engine.resize())
}

import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes

def drawSvgToMesh(mesh: BABYLON.Mesh, scene: BABYLON.Scene, svgSrc: String) = {
    val texture = BABYLON.DynamicTexture("svgTexture", 256, scene) 
    /* ALT: try AdvancedDynamicTexture: https://doc.babylonjs.com/typedoc/classes/BABYLON.GUI.AdvancedDynamicTexture - doesn't fix color, but may detect clicks? 
    var texture = typings.babylonjsGui.global.BABYLON.GUI.AdvancedDynamicTexture.CreateForMesh(mesh, 256, 256, {}, false, {}, {})
    */
    val textureContext = texture.getContext()
    val size = texture.getSize()
    
    //val mat = BABYLON.BackgroundMaterial("mat", scene)
    //mat.diffuseTexture = texture
    //mat.alpha = 0.7

    val opts = PartialIShaderMaterialOptAttributes.MutableBuilder(PartialIShaderMaterialOptAttributes())
      .setAttributesVarargs("position", "uv")
      .setUniformBuffersVarargs("worldViewProjection", "color", "opacity")
      .setSamplersVarargs("textureSampler")
    val mat = new BABYLON.ShaderMaterial("shader", scene, StringDictionary(("vertex", "basic"),("fragment", "unlit")), opts)
    mat.setTexture("textureSampler", texture)
    mat.setVector3("color", BABYLON.Vector3(0,1,0));
    mat.setFloat("opacity", 0.7);
    mat.alpha = 0 // need to set an alpha<1 so the transparent bg does not appear!

    texture.hasAlpha = true  
    mesh.material = mat

    val img = Image()
    /* ALT: modify by changing svg directly!
    dom.ext.Ajax.get(binocularsSvg).collect { _.responseText }.foreach(svg => {
      val svgModified = svg.replace("""<svg fill="#ffffff" """, s"""<svg fill="#ffff00" """)
      val svgEncoded = s"data:image/svg+xml,${scala.scalajs.js.URIUtils.encodeURIComponent(svgModified)}"
      img.src = svgEncoded 
      img.onload = { (e: Event) => {
        textureContext.drawImage(img, 0, 0)
        texture.update()
      }}
    })
    */
    img.src = "/binoculars_fill.svg"
    img.onload = { (e: Event) => {
      textureContext.drawImage(img, 0, 0)
      texture.update()
    }}

}
