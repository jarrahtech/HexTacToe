package hextactoe

import facade.babylonjs.*
import facade.babylonjs.global.BABYLON as BABYLON_IMPL
import com.jarrahtechnology.kassite.tween.*
import com.jarrahtechnology.hex.*

final case class GameState(val scene: BABYLON.Scene,
                 val camera: BABYLON.Camera,
                 val grid: BabylonGrid[_]) {
  val tweenMgr = TweenManager.frameTime(scene)           
  
  // TODO: move this to a lib?
  def currentHex = {
    val ray = scene.createPickingRay(scene.pointerX, scene.pointerY, BABYLON_IMPL.Matrix.Identity(), camera, false)
    scene.pickWithRay(ray).pickedPoint match {
        case pt: BABYLON.Vector3 => grid.fromPixel(pt)
        case null => None
    }
  }

  var activeActor: GameActor = Player
  def nextActor = {
    activeActor match {
      case Player => activeActor = Opponent
      case Opponent => activeActor = Player
    }
    displayText(activeActor.turnMsg)
    activeActor.startTurn(this)
  }

  def endActor = activeActor = Opponent

  def claimHex[C <: CoordSystem](c: Coord) = {
    grid.claim(activeActor, c)
    activeActor.createActorMarker(this, grid.toPixel(c), _ => {
        checkFinished(() => {
          nextActor
        })
    })
  }

  def checkFinished[C <: CoordSystem](nextTurn: () => Unit) = grid.winner match {
    case Some(_) => { activeActor.endAction(this); endActor }
    case None if grid.isDraw => { displayText("Draw &#x1F611;"); explode(scene); endActor }
    case _ => nextTurn()
  }
}
