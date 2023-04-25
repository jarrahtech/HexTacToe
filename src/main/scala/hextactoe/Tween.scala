package hextactoe

enum LoopType(val progress: Double => Double => Double, val hasFinished: Double => Double => Boolean) {
  case Once extends LoopType(duration => runTime => runTime/duration, duration => runTime => runTime>=duration)
  // reverse?
  case Cycle extends LoopType(duration => runTime => (runTime%duration)/duration, _ => _ => false)
  case PingPong extends LoopType(duration => runTime => {
    val runPercent = (runTime%duration)/duration
    if (math.floor(runTime/duration) % 2 == 0) runPercent // forward
    else 1-runPercent // backward
  }, _ => _ => false)
}

enum EaseMethod(val easeFn: Interpolator) {
  case Linear extends EaseMethod(identity)
  case Spring extends EaseMethod(springInterp)
  case Log extends EaseMethod(eInterp(2))

  case InQuad extends EaseMethod(powerInterp(2))
  case OutQuad extends EaseMethod(reverseInterp andThen InQuad.easeFn)
  case InOutQuad extends EaseMethod(inOutInterp andThen InQuad.easeFn)

  case InCubic extends EaseMethod(powerInterp(3))
  case OutCubic extends EaseMethod(reverseInterp andThen InCubic.easeFn)
  case InOutCubic extends EaseMethod(inOutInterp andThen InCubic.easeFn)

  case InQuart extends EaseMethod(powerInterp(4))
  case OutQuart extends EaseMethod(reverseInterp andThen InQuart.easeFn)
  case InOutQuart extends EaseMethod(inOutInterp andThen InQuart.easeFn)

  case InSin extends EaseMethod(sinInterp)
  case OutSin extends EaseMethod(reverseInterp andThen InSin.easeFn)
  case InOutSin extends EaseMethod(inOutInterp andThen InSin.easeFn) 

  case InBounce extends EaseMethod(bounceInterp)
  case OutBounce extends EaseMethod(reverseInterp andThen InBounce.easeFn)
  case InOutBounce extends EaseMethod(inOutInterp andThen InBounce.easeFn)
}

import com.jarrahtechnology.util.Math._

// TODO: replace "time" in names with time unit - once known
final class Tween(val duration: Double, val loop: LoopType, val delay: Double, ease: EaseMethod, tween: Double => Unit, onFinish: Tween => Unit, val manager: TweenManager) {
  require(duration>0, s"duration=${duration} !> 0")

  private var runTime = -delay
  private var mult = 1d
  val updateTweenWith = loop.progress(duration) andThen clamp01 andThen ease.easeFn andThen tween
  val hasFinishedAfter = loop.hasFinished(duration)

  def update(deltaTime: Double) = {runTime += deltaTime*mult; updateTweenWith(runTime); if (hasFinishedAfter(runTime)) stop }
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
    tweens.foreach(_.update(scene.deltaTime))
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

  def move(d: Double, mesh: BABYLON.Mesh, origin: BABYLON.Vector3, dest: BABYLON.Vector3) = 
    add(Tween(d, LoopType.Cycle, 0, EaseMethod.Spring, v => mesh.position = v3lerp(v, origin, dest), _ => println("done move"), this))
}

type Interpolator = Double => Double

// TODO: here down should be in util (and used in InterpGraph too)
def interp(interpFn: Interpolator)(value: Double, lower: Double, upper: Double) = (upper - lower) * interpFn(clamp01(value)) + lower
def lerp = interp(identity)
def v3lerp(v: Double, a: BABYLON.Vector3, b: BABYLON.Vector3) = BABYLON.Vector3(lerp(v, a.x, b.x), lerp(v, a.y, b.y), lerp(v, a.z, b.z))

def reverseInterp: Interpolator = v => 1-v
def inOutInterp: Interpolator = v => if (v<0.5d) v else reverseInterp(v)

def powerInterp(power: Int): Interpolator = { require(power>0, s"power=${power} !>0"); math.pow(_, power) }
def eInterp(k: Double): Interpolator = v => { require(k>0, s"k=${k} !>0"); 1/(1-math.pow(math.E, -k*(v-0.5d))) }
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
