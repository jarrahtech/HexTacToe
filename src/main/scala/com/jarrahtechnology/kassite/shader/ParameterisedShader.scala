package com.jarrahtechnology.kassite.shader

import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes
import typings.babylonjs.global.*
import scala.scalajs.js.Array
import collection.Map

enum ShaderType(val storeSuffix: String, val pathKey: String) {
  case Vertex extends ShaderType("VertexShader", "vertex")
  case Fragment extends ShaderType("FragmentShader", "fragment")
}

enum ShaderParamType {
  case Uniform
  case Texture
}

// TODO: extract params from shader code
final case class SubShader(val name: String, val shaderType: ShaderType, val params: Map[String, ShaderParamType], val code: String) {
  if (!BABYLON.Effect.ShadersStore.contains(name)) BABYLON.Effect.ShadersStore.addOne((s"${name}${shaderType.storeSuffix}", code))

  val path = (shaderType.pathKey, name)
  def collectNames(typ: ShaderParamType) = params.foldLeft(Array.apply[String]())((c, p) => if (p._2==typ) c:+p._1 else c)
  lazy val uniformNames = collectNames(ShaderParamType.Uniform)
  lazy val textureNames = collectNames(ShaderParamType.Texture)
}

final case class ParameterisedShader(val vertex: SubShader, val fragment: SubShader, val defaults: Map[String, Option[Any]]) {
  require(vertex.shaderType==ShaderType.Vertex, "need a vertex shader")
  require(fragment.shaderType==ShaderType.Fragment, "need a fragment shader")

  lazy val toShaderPath = StringDictionary(vertex.path, fragment.path)
  lazy val toShaderOpts = PartialIShaderMaterialOptAttributes.MutableBuilder(PartialIShaderMaterialOptAttributes())
      .setUniformBuffers(vertex.uniformNames++fragment.uniformNames)
      .setSamplers(vertex.textureNames++fragment.textureNames)
  def set(param: String, newDefault:Some[_]): ParameterisedShader = set((param -> newDefault))
  def set(newDefaults: (String, Some[_])*) = ParameterisedShader(vertex, fragment, defaults ++ newDefaults)
}

object ParameterisedShader {
  def apply(vertex: SubShader, fragment: SubShader): ParameterisedShader =
    ParameterisedShader(vertex, fragment, Map.from((vertex.params.keySet union fragment.params.keySet).map(p => (p, Option.empty[Any]))))
}
