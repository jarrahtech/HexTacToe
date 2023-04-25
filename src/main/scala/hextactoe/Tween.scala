package hextactoe

import com.jarrahtechnology.util.Math._
import typings.babylonjs.global.*

import scala.collection.mutable.HashSet
import scala.concurrent.duration._

trait TweenParameters[T <: TweenParameters[_]](val duration: Duration, tween: Double => Unit, val loop: LoopType, val delay: Duration, val ease: EaseMethod, val onFinish: Option[T => Unit]) {
  require(duration>Duration.Zero, s"duration=${duration} !> 0")

  val updateTweenWith = loop.progress(duration) andThen clamp01 andThen ease.fn andThen tween
  val hasFinishedAfter = loop.hasFinished(duration)
  def fireFinished = onFinish.foreach(_(this.asInstanceOf[T]))
  def run(manager: TweenManager) = manager.run(this)
}

final class Tween(val params: TweenParameters[_], val manager: TweenManager) {
  private var runTime = -params.delay
  private var timeScale = 1d

  def update(delta: Duration) = {runTime = runTime + delta*timeScale; params.updateTweenWith(runTime); if (params.hasFinishedAfter(runTime)) stop }
  def progressTime = runTime
  def pause = timeScale = 0
  def unpause = timeScale = 1
  def isPaused = timeScale==0 || manager.isPaused
  def stop = { params.fireFinished; manager.remove(this) }
  def restart = { runTime = -params.delay; if (!manager.manages(this)) manager.run(this); this } 
}

final class TweenManager(scene: BABYLON.Scene) {
  private val tweens: HashSet[Tween] = HashSet.empty[Tween]
  private var timeScale = 1d

  scene.onBeforeRenderObservable.add((sc, ev) => {
    val delta = Duration(scene.deltaTime.toLong, MILLISECONDS)
    tweens.foreach(_.update(delta))
  })

  def run(t: Tween): Tween = { tweens.add(t); t }
  def run(params: TweenParameters[_]): Tween = run(Tween(params, this))
  def remove(tween: Tween) = tweens.remove(tween)
  def manages(tween: Tween) = tweens.exists(_ == tween)

  def setTimeScale(s: Double) = { require(timeScale>=0, s"speed=${timeScale} !>= 0"); timeScale = s }
  def getTimeScale = timeScale
  def pause = timeScale = 0
  def unpause = timeScale = 1
  def isPaused = timeScale==0
}

final case class LinearMoveTweenParameters(d: Duration, val mesh: BABYLON.Mesh, val dest: BABYLON.Vector3, val origin: BABYLON.Vector3) 
  extends TweenParameters[LinearMoveTweenParameters](d, v => mesh.position = v3lerp(v, origin, dest), LoopType.Once, Duration.Zero, EaseMethod.InBounce, None)

object MoveTween {
  def linear(duration: Duration, mesh: BABYLON.Mesh, dest: BABYLON.Vector3) = LinearMoveTweenParameters(duration, mesh, dest, mesh.position.asInstanceOf[BABYLON.Vector3])
}