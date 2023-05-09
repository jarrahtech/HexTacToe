package hextactoe

import scala.concurrent.duration.*
import facade.babylonjs.*
import facade.babylonjs.global.BABYLON as BABYLON_IMPL
import com.jarrahtechnology.kassite.tween.*

trait GameActor {
  def id: Int
  def colour: BABYLON.Color3
  def meshName: String
  def meshFile: String
  def meshSetup: BABYLON.AbstractMesh => Unit
  def turnMsg: String
  def endAction: GameState => Unit
  def startTurn: GameState => Unit

  val meshesUrl = s"${BuildInfo.baseUrl}SpaceKit_Kenney/"
  def createActorMarker(state: GameState, target: BABYLON.Vector3, onFinished: ScaleTweenParameters => Unit) = {
    BABYLON_IMPL.SceneLoader.ImportMesh(state.activeActor.meshName, meshesUrl, state.activeActor.meshFile, state.scene, (newMeshes, _, _, _, _, _, _) => { 
        val parent = new BABYLON_IMPL.Mesh("", state.scene)
        meshSetup(newMeshes(0))
        parent.addChild(newMeshes(0)) 
        parent.position = target 
        parent.scaling = BABYLON_IMPL.Vector3(0,0,0)
        val t =RotationTween.rotateAround(Duration(4, SECONDS), parent, BABYLON_IMPL.Vector3(0,0,1), state.tweenMgr)
        t.start
        ScaleTween.scaleTo(Duration(300, MILLISECONDS), parent, BABYLON_IMPL.Vector3(1,1,1), Some(onFinished)).runOn(state.tweenMgr)
    }, {}, {}, {})
  }
}

object Player extends GameActor {
  val id = 0
  val colour = BABYLON_IMPL.Color3(0, 0.75, 1)
  val meshName = "satelliteDish_detailed"
  val meshFile = "satelliteDish.glb"
  val meshSetup = m => { m.rotate(BABYLON_IMPL.Vector3(1,0,0), math.Pi/6); m.scaling.scaleInPlace(0.12) }
  val turnMsg = s"""<span>It is </span><span style="whiteSpace: nowrap; color: ${colour.toHexString()}">your</span><span> turn, click the hex you want to claim.</span>"""
  val endAction = state => { displayText("Victory!! &#x1F600;"); Fireworks.fireworks(state, 20) }
  def startTurn = _ => {}
}

object Opponent extends GameActor {
  val id = 1
  val colour = BABYLON_IMPL.Color3(1, 1, 0)
  val meshName = "turret"
  val meshFile = "turret.glb"
  val meshSetup = _.rotate(BABYLON_IMPL.Vector3(1,0,0), math.Pi/4)
  val turnMsg = s"""<span>It is </span><span style="whiteSpace: nowrap; color: ${colour.toHexString()}">your opponent's</span><span> turn."""
  val endAction = state => { displayText("Defeat! &#x1F61E;"); explode(state.scene)}
  override def startTurn = state => state.grid.display.grid.find(_._2.isEmpty).foreach((c, _) => state.claimHex(c))
}
