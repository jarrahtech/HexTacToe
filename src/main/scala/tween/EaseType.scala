package tween

import com.jarrahtechnology.util.Math._

trait EaseType(val fn: Interpolator)

object EaseType {
  case object Linear extends EaseType(identity)
  case object Spring extends EaseType(springInterp)
  case object Sigmoid extends EaseType(eInterp(0.2))
  case object Punch extends EaseType(eInterp(0.1))
  case object InQuad extends EaseType(powerInterp(2))
  case object OutQuad extends EaseType(reverseInterp(InQuad.fn))
  case object InCubic extends EaseType(powerInterp(3))
  case object OutCubic extends EaseType(reverseInterp(InCubic.fn))
  case object InQuart extends EaseType(powerInterp(4))
  case object OutQuart extends EaseType(reverseInterp(InQuart.fn))
  case object InSin extends EaseType(sinInterp)
  case object OutSin extends EaseType(reverseInterp(InSin.fn))
  case object InBounce extends EaseType(bounceInterp)
  case object OutBounce extends EaseType(reverseInterp(InBounce.fn))

  // TODO: ease in/out, interp curves
}

// TODO: here down should be in util (and used in InterpGraph too)
type Interpolator = Double => Double

def interp(interpFn: Interpolator)(value: Double, lower: Double, upper: Double) = (upper - lower) * interpFn(clamp01(value)) + lower
def lerp = interp(identity)

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

import scala.concurrent.duration._
def durationMod(x: Duration, divisor: Duration) = Duration(x.toNanos%divisor.toNanos, NANOSECONDS) 
def isEven(x: Long) = x % 2 == 0
def isEven(x: Double) = math.floor(x % 2) == 0