package com.jarrahtechnology.kassite.tween

import scala.concurrent.duration._
import com.jarrahtechnology.util.Math._

type ProgressFn = Duration => Duration => Double
type FinishedFn = Duration => Duration => Boolean

class LoopType(val progress: ProgressFn, val hasFinished: FinishedFn)

object LoopType {

  def runForever: FinishedFn = _ => _ => false
  def stopAfterDuration: FinishedFn = duration => runTime => runTime>=duration
  def stopAfterRepeating(times: Int): FinishedFn = duration => runTime => runTime.toNanos/duration.toNanos>=times

  def forward: ProgressFn = duration => runTime => clamp01(runTime/duration)
  def reverse: ProgressFn = duration => runTime => clamp01(1- runTime/duration)
  def loopForwardFromStart: ProgressFn = duration => runTime => durationMod(runTime, duration)/duration
  def loopForwardThenReverse: ProgressFn = duration => runTime => {
      val runPercent = durationMod(runTime, duration)/duration
      if (isEven(runTime/duration)) runPercent else 1-runPercent // backward
    }
  def repeat(times: Int, underlying: ProgressFn, end: Double): ProgressFn = 
    duration => runTime => if (stopAfterRepeating(times)(duration)(runTime)) end else underlying(duration)(runTime)

  case object Once extends LoopType(forward, stopAfterDuration)
  case object Reverse extends LoopType(reverse, stopAfterDuration)
  final case class Cycle(times: Int) extends LoopType(repeat(times, loopForwardFromStart, 1), stopAfterRepeating(times))
  case object CycleForever extends LoopType(loopForwardFromStart, runForever)
  final case class PingPong(times: Int) extends LoopType(repeat(times, loopForwardThenReverse, if (isEven(times)) 0d else 1d), stopAfterRepeating(times))
  case object PingPongForever extends LoopType(loopForwardThenReverse, runForever)
}
