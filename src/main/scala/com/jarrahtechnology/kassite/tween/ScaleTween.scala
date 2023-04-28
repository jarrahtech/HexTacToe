package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import scala.concurrent.duration._

final case class ScaleTweenParameters(d: Duration, val mesh: BABYLON.Mesh, val dest: BABYLON.Vector3, val origin: BABYLON.Vector3) 
  extends TweenParameters[ScaleTweenParameters](d, v => {println("ddd"); val s = MoveTween.v3lerp(v, origin, dest); println(s"dd=$s"); mesh.scaling = s}, LoopType.Once, EaseType.Sigmoid, Duration.Zero, None) 


object ScaleTween {
  def scaleTo(duration: Duration, mesh: BABYLON.Mesh, dest: BABYLON.Vector3) = 
    ScaleTweenParameters(duration, mesh, dest, mesh.scaling.asInstanceOf[BABYLON.Vector3])
}
