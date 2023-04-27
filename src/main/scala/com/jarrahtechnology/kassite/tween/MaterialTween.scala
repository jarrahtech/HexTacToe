package com.jarrahtechnology.kassite.tween

import typings.babylonjs.global.*
import com.jarrahtechnology.kassite.shader._
import scala.concurrent.duration._

// TODO: handle typings.babylonjs.global.* vs typings.babylonjs.*

final case class MaterialColor3TweenParameters(d: Duration, val mat: BABYLON.ShaderMaterial, val name: String, val dest: BABYLON.Color3, val origin: BABYLON.Color3) 
  extends TweenParameters[LinearMoveTweenParameters](d, v => mat.setColor3(name, MoveTween.c3lerp(v, origin, dest)), LoopType.PingPongForever, EaseType.Sigmoid, Duration.Zero, None)
final case class ParamMaterialColor3TweenParameters(d: Duration, val mat: ParameterisedShaderMaterial, val name: String, val dest: BABYLON.Color3, val origin: BABYLON.Color3) 
  extends TweenParameters[LinearMoveTweenParameters](d, v => mat.setColor3(name, MoveTween.c3lerp(v, origin, dest)), LoopType.PingPongForever, EaseType.Sigmoid, Duration.Zero, None)

// Can't share materials without them all changing, except at start, see https://www.babylonjs-playground.com/#2IFRKC#251
    
object MaterialTween {
    def shaderColor3Parameter(duration: Duration, mat: BABYLON.ShaderMaterial, name: String, dest: BABYLON.Color3, origin: BABYLON.Color3) = 
        MaterialColor3TweenParameters(duration, mat, name, dest, origin) 
    def shaderColor3Parameter(duration: Duration, mat: ParameterisedShaderMaterial, name: String, dest: BABYLON.Color3) = {
        ParamMaterialColor3TweenParameters(duration, mat, name, dest, mat.getColor3(name).getOrElse(BABYLON.Color3(1,1,1)))
    }
}
