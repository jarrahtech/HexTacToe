package hextactoe

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import org.scalajs.dom.{Event, Image, SVGImageElement, XMLSerializer}
import org.scalajs.dom.window
import scalajs.js.Thenable.Implicits.thenable2future
import typings.babylonjs.BABYLON.Material
import org.scalajs.dom.SVGImageElement
import concurrent.ExecutionContext.Implicits.global
import typings.babylonjs.HTMLCanvasElement
import typings.babylonjs.global.*
import BabylonJsHelper._
import typings.babylonjs.anon.Diameter

@main
def HexTacToe(): Unit = {
  renderOnDomContentLoaded(dom.document.getElementById("app"), Main.appElement())
  renderOnDomContentLoaded(dom.document.getElementById("test"), counterButton())

  val scene = createScene()
  loadUnlitTransparentShader
  
  /*val hex = for {
    response <- dom.fetch("/hexagon.svg")
    svg <- response.text()
  } yield {
    val resolution = Dimensions.square(256)
    //drawSvg(scene, svgWithDimensions(svg, resolution), Dimensions.unitSquare, resolution)
    
  }*/
  //drawHex(scene, 1024)
  BabylonGrid.build(scene, Dimensions.square(3), 1)
}

object Main {
  def appElement(): Element = {
    div(
      a(href := "https://vitejs.dev", target := "_blank",
        img(src := "/vite.svg", className := "logo", alt := "Vite logo"),
      ),
      img(src := "/hexagon_fill.svg", className := "logo", alt := "binoculars"),
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



def createScene() = {
  val canvas = dom.document.getElementById("renderCanvas").asInstanceOf[typings.babylonjs.HTMLCanvasElement]
  val engine = new BABYLON.Engine(canvas, true) // Generate the BABYLON 3D engine
  val scene = new BABYLON.Scene(engine)
  
  val origin = BABYLON.MeshBuilder.CreateSphere("sphere", typings.babylonjs.anon.DiameterZ.MutableBuilder(typings.babylonjs.anon.DiameterZ()).setDiameter(0.2), scene)

  val camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 0, -10), scene)
  camera.setTarget(BABYLON.Vector3.Zero())
  camera.attachControl(canvas, true)
  val light = new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene)
  light.intensity = 1

  engine.runRenderLoop(()=>{ scene.render() })  
  window.addEventListener("resize", _ => engine.resize())
  window.addEventListener("load", _ => engine.resize())
  scene
}

/*
import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes

def drawSvgToMesh(mesh: BABYLON.Mesh, scene: BABYLON.Scene, svgSrc: String) = {
  val sphere = BABYLON.Mesh.CreateSphere("sphere1", 16, 2, scene)
  sphere.position.y = 5
  //val ground = BABYLON.Mesh.CreateGround("ground1", 6.0, 6.0, 2, scene)
    val texture = BABYLON.DynamicTexture("svgTexture", 512, scene) 
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
    //val mat = new BABYLON.StandardMaterial("myMaterial", scene)
    //mat.diffuseColor = new BABYLON.Color3(1, 0, 1)
    //println("1")
    //mat.diffuseTexture = new BABYLON.Texture("binoculars_fill.svg", scene, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    //println("2")
    mat.setTexture("textureSampler", texture)
    mat.setVector3("color", BABYLON.Vector3(1,1,1));
    mat.setFloat("opacity", 1);
    mat.alpha = 0.9 // need to set an alpha<1 so the transparent bg does not appear!

    texture.hasAlpha = true  
    mesh.material = mat
    println(svgSrc)
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
    img.src = svgSrc
    img.onload = { (e: Event) => {
      textureContext.drawImage(img, 0, 0)
      texture.update()
      println("done")
    }}
}
*/
