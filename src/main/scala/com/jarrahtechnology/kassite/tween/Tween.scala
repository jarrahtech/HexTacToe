package com.jarrahtechnology.kassite.tween

import com.jarrahtechnology.util.Math._
import typings.babylonjs.global.*

import scala.collection.mutable.HashSet
import scala.concurrent.duration._

// TODO: think about delay end and  builder
trait TweenParameters[T <: TweenParameters[_]](val duration: Duration, action: Double => Unit, val loop: LoopType, val ease: EaseType, val delay: Duration, val onStart: Option[T => Unit], val onFinish: Option[T => Unit]) {
  require(duration>Duration.Zero, s"duration=${duration} !> 0")

  val updateTweenWith = loop.progress(duration) andThen clamp01 andThen ease.method andThen action
  val hasFinishedAfter = loop.hasFinished(duration)
  def fireFinished = onFinish.foreach(_(this.asInstanceOf[T]))
  def fireStarted = onStart.foreach(_(this.asInstanceOf[T]))
  def runOn(manager: TweenManager) = manager.run(this)
}

trait ProgrammaticAnimation {
  protected var timeScale = 1d

  def manager: TweenManager
  def update(delta: Duration): Unit

  def pause = timeScale = 0
  def unpause = timeScale = 1
  def isPaused = timeScale==0 || manager.isPaused 
  def isStopped = !manager.manages(this)
  def stop: Boolean = manager.remove(this)
  def setTimeScale(s: Double) = { require(s>=0, s"speed=${s} !>= 0"); timeScale = s }
  def getTimeScale = timeScale
}

// TODO: check traits do not use parameter lists, but defs instead!!!! check all libs
// TODO: can this be subsumed by Tween? If not generalise better (names/functions), have runOn
final case class DeltaProgrammaticAnimation(action: Duration => Unit, val manager: TweenManager) extends ProgrammaticAnimation {
  def update(delta: Duration) = action(delta*timeScale)
  def start = if (!manager.manages(this)) manager.run(this)
}
// TODO: set as private and force construction through object with runOn?
// TODO: Tween <- DeltaTween, InterpTween

final case class Tween(val params: TweenParameters[_], val manager: TweenManager) extends ProgrammaticAnimation {
  private var runTime = -params.delay

  def update(delta: Duration) = {
    val startTime = runTime
    runTime = runTime + delta*timeScale
    if (startTime.toNanos<=0 && runTime.toNanos>0) params.fireStarted
    params.updateTweenWith(runTime)
    if (params.hasFinishedAfter(runTime)) stop
  }
  def progressTime = runTime
  override def stop = { params.fireFinished; super.stop }
  def restart = { runTime = -params.delay; if (!manager.manages(this)) manager.run(this); this } 
  def syncTo(other: Tween) = { 
    runTime = params.duration * LoopType.loopForwardFromStart(other.params.duration)(other.runTime)
    timeScale = other.timeScale
  }
}

// TODO: don't take scene, take a fn to get time (eg TweenManager.frameTime => current)
final class TweenManager(scene: BABYLON.Scene) {
  private val tweens: HashSet[ProgrammaticAnimation] = HashSet.empty[ProgrammaticAnimation]
  private var timeScale = 1d

  scene.onBeforeRenderObservable.add((sc, ev) => {
    val delta = Duration((scene.getDeterministicFrameTime()*1000).toLong, MICROSECONDS)
    tweens.foreach(_.update(delta))
  })

  // Could do the next two lines in one with generics, but scala-js doesn't seem to like that at runtime :(
  def run(t: Tween): Tween = { if (t.manager == this) tweens.add(t); t }
  def run(t: DeltaProgrammaticAnimation): DeltaProgrammaticAnimation = { if (t.manager == this) tweens.add(t); t } 
  def run(params: TweenParameters[_]): Tween = {val t = Tween(params, this); run(t) }
  
  def remove(tween: ProgrammaticAnimation) = tweens.remove(tween)
  def manages(tween: ProgrammaticAnimation) = tweens.exists(_ == tween)

  def setTimeScale(s: Double) = { require(timeScale>=0, s"speed=${timeScale} !>= 0"); timeScale = s }
  def getTimeScale = timeScale
  def pause = timeScale = 0
  def unpause = timeScale = 1
  def isPaused = timeScale==0
}

// TODO: path move, composite, alpha, look to/face, shake, punch