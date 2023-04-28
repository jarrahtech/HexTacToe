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
import typings.babylonjs.BABYLON.PointerInfo
import typings.babylonjs.BABYLON.AbstractMesh

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

def selectHex(scene: BABYLON.Scene, camera: BABYLON.FreeCamera, grid: BabylonGrid[_]) = {
  val ray = scene.createPickingRay(scene.pointerX, scene.pointerY, BABYLON.Matrix.Identity(), camera, false)
  scene.pickWithRay(ray).pickedPoint match {
    case pt: BABYLON.Vector3 => grid.fromPixel(pt)
    case _ => None
  }
}

@main
def HexTacToe(): Unit = {
  renderTurn(yourTurn)
  val (scene, camera) = createScene()

  val grid = BabylonGrid.build(scene, Dimensions.square(3), 0.75)

  import scala.concurrent.duration._
  import com.jarrahtechnology.kassite.tween._
  import com.jarrahtechnology.kassite.shader._
  val tweenMgr = TweenManager(scene)
  var lastTween = Option.empty[(Coord, Tween)]
  def stopPulse = { lastTween.foreach(_._2.stop); lastTween = None }
  def pulse(c: Coord) = 
    lastTween match {
      case Some((cc, _)) if c==cc => None // already pulsing do nothing
      case Some((cc, t)) => { stopPulse; Some(c) }
      case None => Some(c) 
    } foreach(c => lastTween = grid.mesh(c).map(m => (c, MaterialTween.shaderColor3Parameter(Duration(300, MILLISECONDS), m, "color", player.colour).runOn(tweenMgr))))
  
  scene.onPointerObservable.add((pi, es) => selectHex(scene, camera, grid) match {
    case Some((None, c)) if isPlayerTurn && (pi.event.button<0) => pulse(c) // real unclaimed hex and player hovering over it on their turn
    case Some((None, c)) if isPlayerTurn => { stopPulse; doPlayerTurn(c, grid)} // real unclaimed hex and player clicked in their turn
    case _ => stopPulse // everything else 
  })

  BABYLON.SceneLoader.ImportMesh("turret", "/SpaceKit_Kenney/", "turret.glb", scene, (newMeshes, _, _, _, _, _, _) => {
    newMeshes(0).rotation = BABYLON.Vector3(math.Pi/4,0,math.Pi)
    newMeshes(0).position = BABYLON.Vector3(1,0,0)
    //RotationTween.rotateAround(Duration(2, SECONDS), newMeshes(0), BABYLON.Vector3(0,0,1)).runOn(tweenMgr)
  }, {}, {}, {})

  BABYLON.SceneLoader.ImportMesh("satelliteDish_detailed", "/SpaceKit_Kenney/", "satelliteDish.glb", scene, (newMeshes, _, _, _, _, _, _) => {
    newMeshes(0).rotate(BABYLON.Vector3(1,0,0), math.Pi)
    newMeshes(0).scaling = BABYLON.Vector3(0.14,0.14,0.14)
  }, {}, {}, {})
  
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
