package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import scala.concurrent.duration._

final case class LinearMoveTweenParameters(d: Duration, val mesh: BABYLON.Mesh, val dest: BABYLON.Vector3, val origin: BABYLON.Vector3) 
  extends TweenParameters[LinearMoveTweenParameters](d, v => mesh.position = MoveTween.v3lerp(v, origin, dest), LoopType.Once, EaseType.Sigmoid, Duration.Zero, None)

object MoveTween {
  def moveTo(duration: Duration, mesh: BABYLON.Mesh, dest: BABYLON.Vector3) =  moveFromTo(duration, mesh, dest, mesh.position.asInstanceOf[BABYLON.Vector3])
  def moveFrom(duration: Duration, mesh: BABYLON.Mesh, origin: BABYLON.Vector3) = moveFromTo(duration, mesh, mesh.position.asInstanceOf[BABYLON.Vector3], origin)
  def moveFromTo(duration: Duration, mesh: BABYLON.Mesh, dest: BABYLON.Vector3, origin: BABYLON.Vector3) = 
    LinearMoveTweenParameters(duration, mesh, dest, origin)

  // TODO: move to util and add conversions to/from color/jtV3/bjsV3 & overloads for other vectors/colours
  def v3lerp(v: Double, a: BABYLON.Vector3, b: BABYLON.Vector3) = BABYLON.Vector3(lerp(v, a.x, b.x), lerp(v, a.y, b.y), lerp(v, a.z, b.z))
  def c3lerp(v: Double, a: BABYLON.Color3, b: BABYLON.Color3) = BABYLON.Color3(lerp(v, a.r, b.r), lerp(v, a.g, b.g), lerp(v, a.b, b.b))
}