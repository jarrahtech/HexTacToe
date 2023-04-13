package hextactoe

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import org.scalajs.dom.window

@main
def HexTacToe(): Unit = {
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
def createBabylon() = {
  val canvas = dom.document.getElementById("renderCanvas").asInstanceOf[typings.babylonjs.HTMLCanvasElement]
  val engine = new BABYLON.Engine(canvas, true) // Generate the BABYLON 3D engine
  val scene = new BABYLON.Scene(engine)
  val camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 5, -10), scene)
  camera.setTarget(BABYLON.Vector3.Zero())
  camera.attachControl(canvas, true)
  val light = new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene)
  light.intensity = 0.7
  val sphere = BABYLON.Mesh.CreateSphere("sphere1", 16, 2, scene)
  sphere.position.y = 1
  val ground = BABYLON.Mesh.CreateGround("ground1", 6.0, 6.0, 2, scene)
  engine.runRenderLoop(()=>{ scene.render() })  

  window.addEventListener("resize", _ => engine.resize())
  window.addEventListener("load", _ => engine.resize())
}
