package hextactoe

import scala.concurrent.duration._
import math.Integral.Implicits.infixIntegralOps

enum LoopType(val progress: Duration => Duration => Double, val hasFinished: Duration => Duration => Boolean) {
  case Once extends LoopType(duration => runTime => runTime/duration, duration => runTime => runTime>=duration)
  // TODO: reverse?
  case Cycle extends LoopType(duration => runTime => durationMod(runTime, duration)/duration, _ => _ => false)
  case PingPong extends LoopType(duration => runTime => {
    val runPercent = durationMod(runTime, duration)/duration
    if (isEven(runTime/duration)) runPercent else 1-runPercent // backward
  }, _ => _ => false)
}

enum EaseMethod(val fn: Interpolator) {
  case Linear extends EaseMethod(identity)
  case Spring extends EaseMethod(springInterp)
  case Sigmoid extends EaseMethod(eInterp(0.2))
  case Punch extends EaseMethod(eInterp(0.1))

  case InQuad extends EaseMethod(powerInterp(2))
  case OutQuad extends EaseMethod(reverseInterp(InQuad.fn))

  case InCubic extends EaseMethod(powerInterp(3))
  case OutCubic extends EaseMethod(reverseInterp(InCubic.fn))

  case InQuart extends EaseMethod(powerInterp(4))
  case OutQuart extends EaseMethod(reverseInterp(InQuart.fn))

  case InSin extends EaseMethod(sinInterp)
  case OutSin extends EaseMethod(reverseInterp(InSin.fn))

  case InBounce extends EaseMethod(bounceInterp)
  case OutBounce extends EaseMethod(reverseInterp(InBounce.fn))

  // TODO: ease in/out, interp curves
}

import com.jarrahtechnology.util.Math._

final class Tween(val duration: Duration, val loop: LoopType, val delay: Duration, ease: EaseMethod, tween: Double => Unit, onFinish: Tween => Unit, val manager: TweenManager) {
  require(duration>Duration.Zero, s"duration=${duration} !> 0")

  private var runTime = -delay
  private var mult = 1d
  val updateTweenWith = loop.progress(duration) andThen clamp01 andThen ease.fn andThen tween
  val hasFinishedAfter = loop.hasFinished(duration)

  def update(delta: Duration) = {runTime = runTime + delta*mult; updateTweenWith(runTime); if (hasFinishedAfter(runTime)) stop }
  def progressTime = runTime
  def pause = mult = 0
  def unpause = mult = 1
  def isPaused = mult==0 || manager.isPaused
  def stop = { onFinish(this); manager.remove(this) }
  def restart = ??? // set runTime to -delay and add back to mgr if removed
}

import typings.babylonjs.global.*
import scala.collection.mutable.HashSet

final class TweenManager(scene: BABYLON.Scene) {
  private val tweens: HashSet[Tween] = HashSet.empty[Tween]
  private var mult = 1d

  scene.onBeforeRenderObservable.add((sc, ev) => {
    val delta = Duration(scene.deltaTime.toLong, MILLISECONDS)
    tweens.foreach(_.update(delta))
  })

  private def add(tween: Tween) = if (!manages(tween) && tween.manager==this) tweens.add(tween)
  def remove(tween: Tween) = tweens.remove(tween)
  def manages(tween: Tween) = tweens.exists(_ == tween)
  def sync = ???

  def setSpeed(s: Double) = { require(mult>=0, s"speed=${mult} !>= 0"); mult = s }
  def getSpeed = mult
  def pause = mult = 0
  def unpause = mult = 1
  def isPaused = mult==0

  def move(d: Duration, mesh: BABYLON.Mesh, origin: BABYLON.Vector3, dest: BABYLON.Vector3) = 
    add(Tween(d, LoopType.Once, Duration.Zero, EaseMethod.InBounce, v => mesh.position = v3lerp(v, origin, dest), _ => println("done move"), this))
}

type Interpolator = Double => Double

// TODO: here down should be in util (and used in InterpGraph too)
def interp(interpFn: Interpolator)(value: Double, lower: Double, upper: Double) = (upper - lower) * interpFn(clamp01(value)) + lower
def lerp = interp(identity)
def v3lerp(v: Double, a: BABYLON.Vector3, b: BABYLON.Vector3) = BABYLON.Vector3(lerp(v, a.x, b.x), lerp(v, a.y, b.y), lerp(v, a.z, b.z))

def reverseInterp(other: Interpolator): Interpolator = v => 1-other(1-v)

def powerInterp(power: Int): Interpolator = { require(power>0, s"power=${power} !>0"); math.pow(_, power) }
def eInterp(k: Double): Interpolator = v => { require(k>0, s"k=${k} !>0"); 1-1/(1+math.pow(math.E, (v*2-1)/k)) }
def circInterp: Interpolator = v => math.sqrt(1 - v * v) - 1
def expoInterp: Interpolator = v => math.pow(2, 10 * (v - 1))
def bounceInterp: Interpolator = _ match {
    case v if v < (1 / 2.75d) => 7.5625d * v * v
    case v if v < (2 / 2.75d) => { val av = v - (1.5d / 2.75d); 7.5625d * av * av + 0.75d }
    case v if v < (2.5 / 2.75d) => { val av = v - (2.25d / 2.75d); 7.5625d * av * av + 0.9375d }
    case v => { val av = v - (2.625d / 2.75d); 7.5625d * av * av + 0.984375d }
}
def sinInterp: Interpolator = v => 1d - math.sin(0.5d * math.Pi * (1d - v))
def springInterp: Interpolator = c => (math.sin(c * math.Pi * (0.2f + 2.5f * c * c * c)) * math.pow(1f - c, 2.2f) + c) * (1f + (1.2f * (1f - c)))

def durationMod(x: Duration, divisor: Duration) = Duration(x.toNanos%divisor.toNanos, NANOSECONDS) 
def isEven(x: Long) = x % 2 == 0
def isEven(x: Double) = math.floor(x % 2) == 0