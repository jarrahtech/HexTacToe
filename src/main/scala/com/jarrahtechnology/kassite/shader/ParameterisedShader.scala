package com.jarrahtechnology.kassite.shader

import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes
import typings.babylonjs.global.*

enum ShaderType(val storeSuffix: String, val pathKey: String) {
  case Vertex extends ShaderType("VertexShader", "vertex")
  case Fragment extends ShaderType("FragmentShader", "fragment")
}

sealed trait SubShader() {
  require(params.params.distinctBy(_.name).length==params.params.length, "no duplicate parameter names")

  if (!BABYLON.Effect.ShadersStore.contains(name)) BABYLON.Effect.ShadersStore.addOne((s"${name}${shaderType.storeSuffix}", code))

  def name: String
  def shaderType: ShaderType
  def params: ShaderParams
  def code: String
  val path = (shaderType.pathKey, name)
}
final case class VertexShader(val name: String, val params: ShaderParams, val code: String) extends SubShader {
  require(code.contains("gl_Position"), "need a vertex shader")
  def shaderType = ShaderType.Vertex
}
final case class FragmentShader(val name: String, val params: ShaderParams, val code: String) extends SubShader {
  require(code.contains("gl_FragColor"), "need a fragment shader")
  def shaderType = ShaderType.Fragment
}

final case class ParameterisedShader(val vertex: VertexShader, val fragment: FragmentShader) {
  lazy val defaults = vertex.params.union(fragment.params)
  lazy val toShaderPath = StringDictionary(vertex.path, fragment.path)
  lazy val toShaderOpts = PartialIShaderMaterialOptAttributes.MutableBuilder(PartialIShaderMaterialOptAttributes())
      .setUniformBuffers(defaults.uniformNames)
      .setSamplers(defaults.textureNames)

  def toMaterial(scene: typings.babylonjs.BABYLON.Scene): ParameterisedShaderMaterial = toMaterial(scene, s"${fragment.name}_material")
  def toMaterial(scene: typings.babylonjs.BABYLON.Scene, name: String) = ParameterisedShaderMaterial(name, scene, this)
}
