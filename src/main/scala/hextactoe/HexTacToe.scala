package hextactoe

import scala.scalajs.js._
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
import com.jarrahtechnology.hex.*

final case class Actor(val id: Int, val colour: BABYLON.Color3)
val player = Actor(0, BABYLON.Color3(0, 0.75, 1))
val opponent = Actor(1, BABYLON.Color3(1, 1, 0))

val yourTurn = s"""<span>It is </span><span style="whiteSpace: nowrap; color: ${player.colour.toHexString()}">your</span><span> turn, click the hex you want to claim.</span>"""
val opponentTurn = s"""<span>It is </span><span style="whiteSpace: nowrap; color: ${opponent.colour.toHexString()}">your opponent's</span><span> turn."""
val win = "Victory!!"
val lose = "Defeat!"
val draw = "Draw"

def renderTurn(who: String) = dom.document.getElementById("turn").innerHTML = who

var isPlayerTurn = true

@main
def HexTacToe(): Unit = {
  renderTurn(yourTurn)
  val (scene, camera) = createScene()
  loadUnlitTransparentShader

  val grid = BabylonGrid.build(scene, Dimensions.square(3), 0.75)
  scene.onPointerDown = (e, _, _) => if (e.detail.map(_<=1).getOrElse(true)) then {
    val ray = scene.createPickingRay(scene.pointerX, scene.pointerY, BABYLON.Matrix.Identity(), camera, false)
    scene.pickWithRay(ray).pickedPoint match {
      case pt: BABYLON.Vector3 => click(grid.fromPixel(pt), grid) 
      case _ => {}
    }
  } 
}

def colorToVector3(c: BABYLON.Color3) = BABYLON.Vector3(c.r, c.g, c.b)

def click[C <: CoordSystem](h: Option[(HexModel, Coord)], grid: BabylonGrid[C]) = h match {
  case Some(None, c) if isPlayerTurn => doPlayerTurn(c, grid) // real hex and not claimed by anyone
  case _ => {} // do nothing
}

def doPlayerTurn[C <: CoordSystem](c: Coord, grid: BabylonGrid[C]) = {
  isPlayerTurn = false
  grid.claim(player, c)
  isFinished(grid, () => {
    renderTurn(opponentTurn)
    doOpponentTurn(grid)
  })
} 

def isFinished[C <: CoordSystem](grid: BabylonGrid[C], nextTurn: () => Unit) = grid.winner match {
  case Some(player.id) => { isPlayerTurn = false; renderTurn(win) }
  case Some(opponent.id) => { isPlayerTurn = false; renderTurn(lose) }
  case None if grid.isDraw => { isPlayerTurn = false; renderTurn(draw) }
  case _ => nextTurn()
}

def doOpponentTurn[C <: CoordSystem](grid: BabylonGrid[C]) = {
  grid.display.grid.find(_._2.isEmpty).foreach((c, h) => {
    grid.claim(opponent, c)
    isFinished(grid, () => {
      renderTurn(yourTurn)
      isPlayerTurn = true
    })   
  })
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
