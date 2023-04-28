package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import scala.concurrent.duration._

final case class RotateAroundTweenParameters(d: Duration, val mesh: typings.babylonjs.BABYLON.AbstractMesh, val axis: BABYLON.Vector3) 
  extends TweenParameters[RotateAroundTweenParameters](d, {v => mesh.rotate(axis, 2*math.Pi/v); println(""+mesh.rotationQuaternion)}, LoopType.CycleForever, EaseType.Linear, Duration.Zero, None) 

object RotationTween {
  def rotateAround(duration: Duration, mesh: typings.babylonjs.BABYLON.AbstractMesh, axis: BABYLON.Vector3) = RotateAroundTweenParameters(duration, mesh, axis)
}
