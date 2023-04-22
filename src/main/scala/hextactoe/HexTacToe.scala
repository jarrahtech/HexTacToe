package hextactoe

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import org.scalajs.dom
import org.scalajs.dom.{Event, Image, SVGImageElement, XMLSerializer}
import org.scalajs.dom.window
import typings.babylonjs.BABYLON.Material
import org.scalajs.dom.SVGImageElement
import typings.babylonjs.HTMLCanvasElement
import typings.babylonjs.global.*
import BabylonJsHelper._
import typings.babylonjs.anon.Diameter
import com.jarrahtechnology.hex.Coord

val playerColour = "#87CEEB"
val opponentColor = "#FFFF00"
val yourTurn = s"""<span>It is </span><span style="whiteSpace: nowrap; color: ${playerColour}">your</span><span> turn, click the hex you want to claim.</span>"""
val opponentTurn = s"""<span>It is </span><span style="whiteSpace: nowrap; color: ${opponentColor}">your opponent's</span><span> turn."""

def renderTurn(who: String) = dom.document.getElementById("turn").innerHTML = who

var isPlayerTurn = true

@main
def HexTacToe(): Unit = {
  renderTurn(yourTurn)
  val (scene, camera) = createScene()
  loadUnlitTransparentShader

  val grid = BabylonGrid.build(scene, Dimensions.square(3), 0.75)
  scene.onPointerDown = (_, _, _) => {
    val ray = scene.createPickingRay(scene.pointerX, scene.pointerY, BABYLON.Matrix.Identity(), camera, false)
    scene.pickWithRay(ray).pickedPoint match {
      case pt: BABYLON.Vector3 => click(grid.fromPixel(pt)) 
      case _ => {}
    }
  } 
}

def click(h: Option[(HexModel, Coord)]) = h match {
  case Some(None, c) if isPlayerTurn => {println(s"$c"); isPlayerTurn = false; renderTurn(opponentTurn)}
  case _ => {}
}

def createScene() = {
  val canvas = dom.document.getElementById("renderCanvas").asInstanceOf[typings.babylonjs.HTMLCanvasElement]
  val engine = new BABYLON.Engine(canvas, true) // Generate the BABYLON 3D engine
  val scene = new BABYLON.Scene(engine)
  scene.clearColor = BABYLON.Color4(0,0,0,1)
  //val origin = BABYLON.MeshBuilder.CreateSphere("sphere", typings.babylonjs.anon.DiameterZ.MutableBuilder(typings.babylonjs.anon.DiameterZ()).setDiameter(0.2), scene)
  val camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 0, -10), scene)
  camera.setTarget(BABYLON.Vector3.Zero())
  //camera.attachControl(canvas, true)
  val light = new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene)
  light.intensity = 1

  engine.runRenderLoop(()=>{ scene.render() })  
  window.addEventListener("resize", _ => engine.resize())
  window.addEventListener("load", _ => engine.resize()) 
  (scene, camera)
}
