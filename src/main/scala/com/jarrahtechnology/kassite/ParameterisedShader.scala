package com.jarrahtechnology.kassite

import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes
import typings.babylonjs.global.*
import scala.scalajs.js.Array
import collection.Map

/*
def loadUnlitTransparentShader = {
    BABYLON.Effect.ShadersStore.addOne(("basicVertexShader", "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}"))
    BABYLON.Effect.ShadersStore.addOne(("unlitFragmentShader", "precision highp float;varying vec2 vUV;uniform sampler2D textureSampler;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=texture2D(textureSampler,vUV)*vec4(color,opacity);}"))
  }
attr=position attr=uv uniform=worldViewProjection varying=vUV
varying=vUV uniform=textureSampler , color, opacity

  val unlitTransparentShaderPath = StringDictionary(("vertex", "basic"),("fragment", "unlit"))
  val unlitTransparentShaderOpts = PartialIShaderMaterialOptAttributes.MutableBuilder(PartialIShaderMaterialOptAttributes())
        .setAttributesVarargs("position", "uv")
        .setUniformBuffersVarargs("worldViewProjection", "color", "opacity")
        .setSamplersVarargs("textureSampler")

  val nullPlaneOptions = typings.babylonjs.anon.SourcePlane()

  def unlitTransparentMaterial(scene: BABYLON.Scene, texture: BABYLON.DynamicTexture) = {
    val mat = BABYLON.ShaderMaterial("shader", scene, unlitTransparentShaderPath, unlitTransparentShaderOpts)
    mat.setTexture("textureSampler", texture)
    mat.setColor3("color", BABYLON.Color3(1,1,1));
    mat.setFloat("opacity", 1);
    mat.alpha = 0.99 // <1 so bg disappears
    mat
  }
  */
object Shaders {
    import com.jarrahtechnology.kassite.ShaderType._
    import com.jarrahtechnology.kassite.ShaderParamType._

    lazy val unlitTransparent = ParameterisedShader.derive(
        SubShader("basic2", Vertex, Map.empty, "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}"),
        SubShader("unlit2", Fragment, Map("color" -> Uniform, "opacity" -> Uniform, "tex" -> Uniform), "precision highp float;varying vec2 vUV;uniform sampler2D tex;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=vec4(color,opacity);}")
    )
}
 
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
  def derive(vertex: SubShader, fragment: SubShader): ParameterisedShader =
    ParameterisedShader(vertex, fragment, Map.from((vertex.params.keySet union fragment.params.keySet).map(p => (p, Option.empty[Any]))))
}

final class ParameterisedShaderMaterial(name: String, scene: BABYLON.Scene, val shader: ParameterisedShader) {
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



/*
object ParameterisedShaderMaterial {
    def createFrom(scene: BABYLON.Scene, vertex: VertexShader, fragment: FragmentShader) = {
        val mat = BABYLON.ShaderMaterial("shader", scene, unlitTransparentShaderPath, unlitTransparentShaderOpts)
        mat.setTexture("textureSampler", texture)
        mat.setColor3("color", BABYLON.Color3(1,1,1));
        mat.setFloat("opacity", 1);
        mat.alpha = 0.99 // <1 so bg disappears
        mat
    }
}*/
