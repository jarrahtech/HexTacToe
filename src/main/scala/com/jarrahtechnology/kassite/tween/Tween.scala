package com.jarrahtechnology.kassite.tween

import com.jarrahtechnology.util.Math._
import typings.babylonjs.global.*

import scala.collection.mutable.HashSet
import scala.concurrent.duration._

trait TweenParameters[T <: TweenParameters[_]](val duration: Duration, action: Double => Unit, val loop: LoopType, val ease: EaseType, val delay: Duration, val onFinish: Option[T => Unit]) {
  require(duration>Duration.Zero, s"duration=${duration} !> 0")

  val updateTweenWith = loop.progress(duration) andThen clamp01 andThen ease.method andThen action
  val hasFinishedAfter = loop.hasFinished(duration)
  def fireFinished = onFinish.foreach(_(this.asInstanceOf[T]))
  def runOn(manager: TweenManager) = manager.run(this)
}

final class Tween(val params: TweenParameters[_], val manager: TweenManager) {
  private var runTime = -params.delay
  private var timeScale = 1d

  def update(delta: Duration) = {runTime = runTime + delta*timeScale; params.updateTweenWith(runTime); if (params.hasFinishedAfter(runTime)) stop }
  def progressTime = runTime
  def pause = timeScale = 0
  def unpause = timeScale = 1
  def isPaused = timeScale==0 || manager.isPaused
  def isStopped = !manager.manages(this)
  def stop = { params.fireFinished; manager.remove(this) }
  def restart = { runTime = -params.delay; if (!manager.manages(this)) manager.run(this); this } 
  def syncTo(other: Tween) = { 
    runTime = params.duration * LoopType.loopForwardFromStart(other.params.duration)(other.runTime)
    timeScale = other.timeScale
  }
}

final class TweenManager(scene: BABYLON.Scene) {
  private val tweens: HashSet[Tween] = HashSet.empty[Tween]
  private var timeScale = 1d

  scene.onBeforeRenderObservable.add((sc, ev) => {
    val delta = Duration((scene.getDeterministicFrameTime()*1000).toLong, MICROSECONDS)
    tweens.foreach(_.update(delta))
  })

  def run(t: Tween): Tween = { if (t.manager == this) tweens.add(t); t }
  def run(params: TweenParameters[_]): Tween = run(Tween(params, this))
  def remove(tween: Tween) = tweens.remove(tween)
  def manages(tween: Tween) = tweens.exists(_ == tween)

  def setTimeScale(s: Double) = { require(timeScale>=0, s"speed=${timeScale} !>= 0"); timeScale = s }
  def getTimeScale = timeScale
  def pause = timeScale = 0
  def unpause = timeScale = 1
  def isPaused = timeScale==0
}

// TODO: path move, scale, material color, rotation, composite, alpha, look to/face, shake, punch