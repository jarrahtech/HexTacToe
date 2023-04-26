package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import scala.concurrent.duration._

final case class MaterialColor3TweenParameters(d: Duration, val mat: BABYLON.ShaderMaterial, val name: String, val dest: BABYLON.Color3, val origin: BABYLON.Color3) 
  extends TweenParameters[LinearMoveTweenParameters](d, v => mat.setColor3(name, MoveTween.c3lerp(v, origin, dest)), LoopType.PingPongForever, EaseType.Sigmoid, Duration.Zero, None)

// Can't share materials without them all changing, except at start, see https://www.babylonjs-playground.com/#2IFRKC#251
    
object MaterialTween {
    def shaderColor3Parameter(duration: Duration, mat: BABYLON.ShaderMaterial, name: String, dest: BABYLON.Color3, origin: BABYLON.Color3) = 
        MaterialColor3TweenParameters(duration, mat, name, dest, origin) //mat.getVector3("name"))
}
