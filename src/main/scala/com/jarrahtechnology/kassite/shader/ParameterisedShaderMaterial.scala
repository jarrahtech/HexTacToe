package com.jarrahtechnology.kassite.shader

import scala.scalajs.js
import js.JSConverters.*
import typings.babylonjs.*
import typings.babylonjs.global.BABYLON as BABYLON_IMPL

// TODO: add v4 to jt.util?
// TODO: be able to change initial values?
final class ParameterisedShaderMaterial(name: String, scene: BABYLON.Scene, val shader: ParameterisedShader) extends BABYLON_IMPL.ShaderMaterial(name, scene, shader.toShaderPath, shader.toShaderOpts) {
  private val params = collection.mutable.Map.from(shader.defaults.map(p => (p.name -> p.initialValue)))
  shader.defaults.foreach(_.setInitial(this))

  override def setArray2(name: String, value: js.Array[Double]) = { super.setArray2(name, value); params(name) = Some(value); this }
  override def setArray3(name: String, value: js.Array[Double]) = { super.setArray3(name, value); params(name) = Some(value); this }
  override def setArray4(name: String, value: js.Array[Double]) = { super.setArray4(name, value); params(name) = Some(value); this }
  override def setColor3(name: String, value: BABYLON.Color3) = { super.setColor3(name, value); params(name) = Some(value); this }
  override def setColor3Array(name: String, value: js.Array[BABYLON.Color3]) = { super.setColor3Array(name, value); params(name) = Some(value); this }
  override def setColor4(name: String, value: BABYLON.Color4) = { super.setColor4(name, value); params(name) = Some(value); this }
  override def setColor4Array(name: String, value: js.Array[BABYLON.Color4]) = { super.setColor4Array(name, value); params(name) = Some(value); this }
  override def setExternalTexture(name: String, texture: BABYLON.ExternalTexture) = { super.setExternalTexture(name, texture); params(name) = Some(texture); this }
  override def setFloat(name: String, value: Double) = { super.setFloat(name, value); params(name) = Some(value); this }
  override def setFloats(name: String, value: js.Array[Double]) = { super.setFloats(name, value); params(name) = Some(value); this }
  override def setInt(name: String, value: Double) = { super.setInt(name, value); params(name) = Some(value); this }
  override def setMatrices(name: String, value: js.Array[BABYLON.Matrix]) = { super.setMatrices(name, value); params(name) = Some(value); this }
  override def setMatrix(name: String, value: BABYLON.Matrix) = { super.setMatrix(name, value); params(name) = Some(value); this }
  // TODO: how to handle the below? Same issue in ShaderParameter
  //override def setMatrix2x2(name: String, value: js.Array[Double]) = { super.setMatrix2x2(name, value); params(name) = Some(value); this }
  //override def setMatrix2x2(name: String, value: js.typedarray.Float32Array) = { super.setMatrix2x2(name, value); params(name) = Some(value); this }
  //override def setMatrix3x3(name: String, value: js.Array[Double]) = { super.setMatrix3x3(name, value); params(name) = Some(value); this }
  //override def setMatrix3x3(name: String, value: js.typedarray.Float32Array) = { super.setMatrix3x3(name, value); params(name) = Some(value); this }
  override def setQuaternion(name: String, value: BABYLON.Quaternion) = { super.setQuaternion(name, value); params(name) = Some(value); this }
  override def setQuaternionArray(name: String, value: js.Array[BABYLON.Quaternion]) = { super.setQuaternionArray(name, value); params(name) = Some(value); this }
  override def setStorageBuffer(name: String, buffer: BABYLON.StorageBuffer) = { super.setStorageBuffer(name, buffer); params(name) = Some(buffer); this }
  override def setTexture(name: String, value: BABYLON.BaseTexture) = { super.setTexture(name, value); params(name) = Some(value); this }
  override def setTextureArray(name: String, textures: js.Array[BABYLON.BaseTexture]) = { super.setTextureArray(name, textures); params(name) = Some(textures); this }
  override def setTextureSampler(name: String, sampler: BABYLON.TextureSampler) = { super.setTextureSampler(name, sampler); params(name) = Some(sampler); this }
  override def setUInt(name: String, value: Double) = { super.setUInt(name, value); params(name) = Some(value); this }
  override def setUniformBuffer(name: String, buffer: BABYLON.UniformBuffer) = { super.setUniformBuffer(name, buffer); params(name) = Some(buffer); this }
  override def setVector2(name: String, value: BABYLON.Vector2) = { super.setVector2(name, value); params(name) = Some(value); this }
  override def setVector3(name: String, value: BABYLON.Vector3) = { super.setVector3(name, value); params(name) = Some(value); this }
  override def setVector4(name: String, value: BABYLON.Vector4) = { super.setVector4(name, value); params(name) = Some(value); this }
  
  def get(name: String) = params(name)
  // TODO: complete these
  def getColor3(name: String): Option[BABYLON.Color3] = params(name) match {
    case Some(c: BABYLON_IMPL.Color3) => Some(c)
    case _ => None
  }
  def getFloat(name: String): Option[Double] = params(name) match {
    case Some(c: Double) => Some(c)
    case _ => None
  }
  
}
