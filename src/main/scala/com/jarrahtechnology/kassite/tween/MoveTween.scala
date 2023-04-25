package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import scala.concurrent.duration._

final case class LinearMoveTweenParameters(d: Duration, val mesh: BABYLON.Mesh, val dest: BABYLON.Vector3, val origin: BABYLON.Vector3) 
  extends TweenParameters[LinearMoveTweenParameters](d, v => mesh.position = MoveTween.v3lerp(v, origin, dest), LoopType.Cycle, EaseType.Sigmoid, Duration.Zero, None)

object MoveTween {
  def linear(duration: Duration, mesh: BABYLON.Mesh, dest: BABYLON.Vector3) = LinearMoveTweenParameters(duration, mesh, dest, mesh.position.asInstanceOf[BABYLON.Vector3])

  def v3lerp(v: Double, a: BABYLON.Vector3, b: BABYLON.Vector3) = BABYLON.Vector3(lerp(v, a.x, b.x), lerp(v, a.y, b.y), lerp(v, a.z, b.z))
}