package com.jarrahtechnology.kassite.tween

import com.jarrahtechnology.util.Math._

trait EaseType(val method: Interpolator)

object EaseType {
  def reverse(other: Interpolator): Interpolator = v => 1-other(1-v)
  def inOut(in: Interpolator): Interpolator = v => if (v<=0.5d) in(v*2d)/2d else (reverse(in)(v*2d-1d)+1d)/2d
  def outIn(in: Interpolator): Interpolator = v => if (v<=0.5d) reverse(in)(v*2d)/2d else (in(v*2d-1d)+1d)/2d

  case object Linear extends EaseType(identity)
  case object Spring extends EaseType(springInterp)
  case object OutInSpring extends EaseType(outIn(Spring.method))

  case object Sigmoid extends EaseType(eInterp(0.2))
  case object SteepSigmoid extends EaseType(eInterp(0.1))

  case object InQuad extends EaseType(powerInterp(2))
  case object OutQuad extends EaseType(reverse(InQuad.method))
  case object InOutQuad extends EaseType(inOut(InQuad.method))
  case object OutInQuad extends EaseType(outIn(InQuad.method))

  case object InCubic extends EaseType(powerInterp(3))
  case object OutCubic extends EaseType(reverse(InCubic.method))

  case object InQuart extends EaseType(powerInterp(4))
  case object OutQuart extends EaseType(reverse(InQuart.method))
  case object InOutQuart extends EaseType(inOut(InQuart.method))

  case object InSin extends EaseType(sinInterp)
  case object OutSin extends EaseType(reverse(InSin.method))

  case object InBounce extends EaseType(bounceInterp)
  case object OutBounce extends EaseType(reverse(InBounce.method))
  
  // TODO: interp curves
}

// TODO: here down should be in util (and used in InterpGraph too)
type Interpolator = Double => Double

def interp(interpFn: Interpolator)(value: Double, lower: Double, upper: Double) = (upper - lower) * interpFn(clamp01(value)) + lower
def lerp = interp(identity)
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
