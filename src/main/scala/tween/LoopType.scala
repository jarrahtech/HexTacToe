package tween

import scala.concurrent.duration._
import com.jarrahtechnology.util.Math._

trait LoopType(val progress: Duration => Duration => Double, val hasFinished: Duration => Duration => Boolean)

object LoopType {

  def runForever: Duration => Duration => Boolean = _ => _ => false
  def stopAfterDuration: Duration => Duration => Boolean = duration => runTime => runTime>=duration
  def stopAfterRepeating(times: Int): Duration => Duration => Boolean = duration => runTime => runTime.toNanos/duration.toNanos>=times

  def forward: Duration => Duration => Double = duration => runTime => runTime/duration
  def reverse: Duration => Duration => Double = duration => runTime => 1- runTime/duration
  def loopForwardFromStart: Duration => Duration => Double = duration => runTime => durationMod(runTime, duration)/duration
  def loopForwardThenReverse: Duration => Duration => Double = duration => runTime => {
      val runPercent = durationMod(runTime, duration)/duration
      if (isEven(runTime/duration)) runPercent else 1-runPercent // backward
    }

  case object Once extends LoopType(forward, stopAfterDuration)
  final case class Repeat(val times: Int) extends LoopType(loopForwardFromStart, stopAfterRepeating(times))
  final case class Reverse() extends LoopType(reverse, stopAfterDuration)
  final case class Cycle() extends LoopType(loopForwardFromStart, runForever)
  final case class PingPong() extends LoopType(loopForwardThenReverse, runForever)
}
