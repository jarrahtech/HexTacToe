package com.jarrahtechnology.kassite.shader

import typings.babylonjs.global.*

final case class ParameterisedShaderMaterial(name: String, scene: BABYLON.Scene, val shader: ParameterisedShader) {
  val underlying = BABYLON.ShaderMaterial(name, scene, shader.toShaderPath, shader.toShaderOpts)
  val params = collection.mutable.Map.from(shader.defaults)
  params.foreach(p => p._2.foreach(setAny(p._1, _)))

  def set(name: String, value: BABYLON.Color3) = { underlying.setColor3(name, value); params(name) = Some(value) }
  def set(name: String, value: Double) = { underlying.setFloat(name, value); params(name) = Some(value) }
  def set(name: String, value: BABYLON.BaseTexture) = { underlying.setTexture(name, value); params(name) = Some(value) }
  def setAny[T](name: String, value: T) = value match {
    case v: BABYLON.Color3 => set(name, v)
    case v: Double => set(name, v)
    case v: BABYLON.BaseTexture => set(name, v)
    case _ => {}
  }
  def get(name: String) = params(name)
}
