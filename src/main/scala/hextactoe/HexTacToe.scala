package hextactoe

import org.scalajs.dom
import org.scalajs.dom.window
import facade.babylonjs.*
import facade.babylonjs.global.BABYLON as BABYLON_IMPL
import BabylonJsHelper.*
import com.jarrahtechnology.hex.*
import scala.concurrent.duration.*
import com.jarrahtechnology.kassite.tween.*

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
    } foreach(c => lastTween = state.grid.mesh(c).map(m => (c, {
      val originalColor = m.getColor3("color").getOrElse(BABYLON_IMPL.Color3(1,1,1))
      InterpTweenBuilder().withAction(InterpFunction.shaderParam(m, "color", originalColor, Player.colour))     
          .withDuration(Duration(300, MILLISECONDS))
          .withLoop(LoopType.PingPongForever)
          .withOnFinish(_ => m.setColor3("color", originalColor))
          .runOn(state.tweenMgr)
    })))
  
  scene.onPointerObservable.add((pi, es) => (state.activeActor, state.currentHex) match {
    case (Player, Some((None, c))) if (pi.event.button<0) => pulse(c) // real unclaimed hex and player hovering over it on their turn
    case (Player, Some((None, c))) => { stopPulse; state.claimHex(c)} // real unclaimed hex and player clicked in their turn
    case _ => stopPulse // everything else 
  })
}

def displayText(text: String) = dom.document.getElementById("turn").innerHTML = text

def explode(scene: BABYLON.Scene) = BABYLON_IMPL.ParticleHelper.CreateAsync("explosion", scene).`then`(particles => {
    particles.systems.foreach(_.disposeOnStop = true)
    particles.start();
  })

def createScene() = {
  val canvas = dom.document.getElementById("renderCanvas").asInstanceOf[HTMLCanvasElement]
  val engine = new BABYLON_IMPL.Engine(canvas, true) // Generate the BABYLON 3D engine
  val scene = new BABYLON_IMPL.Scene(engine)
  scene.clearColor = BABYLON_IMPL.Color4(0,0,0,1)
  val camera = new BABYLON_IMPL.FreeCamera("camera1", new BABYLON_IMPL.Vector3(0, 0, -10), scene)
  camera.setTarget(BABYLON_IMPL.Vector3.Zero())
  val light = new BABYLON_IMPL.HemisphericLight("light", new BABYLON_IMPL.Vector3(0, 1, 0), scene)
  light.intensity = 1

  engine.runRenderLoop(()=>{ scene.render() })  
  window.addEventListener("resize", _ => engine.resize())
  window.addEventListener("load", _ => engine.resize()) 
  (scene, camera)
}
