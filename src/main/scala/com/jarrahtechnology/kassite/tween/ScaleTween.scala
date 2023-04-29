package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import scala.concurrent.duration._

final case class ScaleTweenParameters(d: Duration, val mesh: BABYLON.Mesh, val dest: BABYLON.Vector3, val origin: BABYLON.Vector3) 
  extends TweenParameters[ScaleTweenParameters](d, v => mesh.scaling = MoveTween.v3lerp(v, origin, dest), LoopType.Once, EaseType.Sigmoid, Duration.Zero, None, None) 


object ScaleTween {
  def scaleTo(duration: Duration, mesh: BABYLON.Mesh, dest: BABYLON.Vector3) = 
    ScaleTweenParameters(duration, mesh, dest, mesh.scaling.asInstanceOf[BABYLON.Vector3])
}
