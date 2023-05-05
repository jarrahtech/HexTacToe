package hextactoe

import scala.scalajs.*
import scala.scalajs.js.annotation.*
import org.scalajs.dom
import org.scalajs.dom.{Event, Image, SVGImageElement, XMLSerializer}
import org.scalajs.dom.window
import typings.babylonjs.BABYLON.Material
import org.scalajs.dom.SVGImageElement
import typings.babylonjs.HTMLCanvasElement
import typings.babylonjs.global.*
import BabylonJsHelper.*
import typings.babylonjs.anon.Diameter
import com.jarrahtechnology.hex.*
import typings.babylonjs.BABYLON.PointerInfo
import typings.babylonjs.BABYLON.AbstractMesh
import scala.concurrent.duration.*
import com.jarrahtechnology.kassite.tween.*
import com.jarrahtechnology.kassite.shader.*

// TODO: cleanup up imports - typings.babylonjs.BABYLON & typings.babylonjs.global.BABYLON

@main
def HexTacToe(): Unit = { 
  val (scene, camera) = createScene()
  val state = GameState(scene, camera, BabylonGrid.build(scene, Dimensions.square(3), 0.75))
  displayText(state.activeActor.turnMsg)

  var lastTween = Option.empty[(Coord, Tween)]
  def stopPulse = { lastTween.foreach(_._2.stop); lastTween = None }
  def pulse(c: Coord) = 
    lastTween match {
      case Some((cc, _)) if c==cc => None // already pulsing do nothing
      case Some((cc, t)) => { stopPulse; Some(c) }
      case None => Some(c) 
    } foreach(c => lastTween = state.grid.mesh(c).map(m => (c, MaterialTween.shaderColor3Parameter(Duration(300, MILLISECONDS), m, "color", Player.colour).runOn(state.tweenMgr))))
  
  scene.onPointerObservable.add((pi, es) => (state.activeActor, state.currentHex) match {
    case (Player, Some((None, c))) if (pi.event.button<0) => pulse(c) // real unclaimed hex and player hovering over it on their turn
    case (Player, Some((None, c))) => { stopPulse; state.claimHex(c)} // real unclaimed hex and player clicked in their turn
    case _ => stopPulse // everything else 
  })
}

def displayText(text: String) = dom.document.getElementById("turn").innerHTML = text

def explode(scene: typings.babylonjs.BABYLON.Scene) = BABYLON.ParticleHelper.CreateAsync("explosion", scene).`then`(particles => {
    particles.systems.foreach(_.disposeOnStop = true)
    particles.start();
  })

def createScene() = {
  val canvas = dom.document.getElementById("renderCanvas").asInstanceOf[typings.babylonjs.HTMLCanvasElement]
  val engine = new BABYLON.Engine(canvas, true) // Generate the BABYLON 3D engine
  val scene = new BABYLON.Scene(engine)
  scene.clearColor = BABYLON.Color4(0,0,0,1)
  val camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 0, -10), scene)
  camera.setTarget(BABYLON.Vector3.Zero())
  val light = new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene)
  light.intensity = 1

  engine.runRenderLoop(()=>{ scene.render() })  
  window.addEventListener("resize", _ => engine.resize())
  window.addEventListener("load", _ => engine.resize()) 
  (scene, camera)
}
