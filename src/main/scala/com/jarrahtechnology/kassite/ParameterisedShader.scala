package com.jarrahtechnology.kassite

import org.scalablytyped.runtime.StringDictionary
import typings.babylonjs.anon.PartialIShaderMaterialOptAttributes
import typings.babylonjs.global.*
import scala.scalajs.js.Array

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

    lazy val unlitTransparent = ParameterisedShader(
        SubShader("basic2", Vertex, "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}",
                  Set.empty[ShaderParam[_]]),
        SubShader("unlit2", Fragment, "precision highp float;varying vec2 vUV;uniform sampler2D tex;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=texture2D(tex,vUV)*vec4(color,opacity);}",
                  Set(ShaderParam("color", Uniform, Some(BABYLON.Color3.White())), ShaderParam("opacity", Uniform, Some(0.9d)), ShaderParam("tex", Uniform, Option.empty[BABYLON.Texture])))
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
 
final case class ShaderParam[T](val name: String, val paramType: ShaderParamType, val defaultValue: Option[T]) {
  override def hashCode(): Int = name.hashCode
  override def equals(x: Any): Boolean = canEqual(x) && name == x.asInstanceOf[ShaderParam[_]].name
  def set(parameter: String, newDefault: Some[T]) = ShaderParam[T](name, paramType, newDefault)
}

// TODO: extract params from shader code
final case class SubShader(val name: String, val shaderType: ShaderType, val code: String, val params: Set[ShaderParam[_]]) {
  if (!BABYLON.Effect.ShadersStore.contains(name)) BABYLON.Effect.ShadersStore.addOne((s"${name}${shaderType.storeSuffix}", code))

  val path = (shaderType.pathKey, name)
  def collectNames(typ: ShaderParamType) = params.foldLeft(Array.apply[String]())((c,p) => if (p.paramType==typ) c:+p.name else c)
  lazy val uniformNames = collectNames(ShaderParamType.Uniform)
  lazy val textureNames = collectNames(ShaderParamType.Texture)
  def set(parameter: String, newDefault: Some[_]) = SubShader(name, shaderType, code, {
    params.find(_.name==parameter).map(p => ShaderParam(p.name, p.paramType, newDefault)).map(params + _).getOrElse(params)
  })
}

final case class ParameterisedShader(val vertex: SubShader, val fragment: SubShader) {
  require(vertex.shaderType==ShaderType.Vertex, "need a vertex shader")
  require(fragment.shaderType==ShaderType.Fragment, "need a fragment shader")

  def paramMap = collection.mutable.Map.from((vertex.params union fragment.params).map(p => (p.name, p.defaultValue)))
  lazy val toShaderPath = StringDictionary(vertex.path, fragment.path)
  lazy val toShaderOpts = PartialIShaderMaterialOptAttributes.MutableBuilder(PartialIShaderMaterialOptAttributes())
      .setUniformBuffers(vertex.uniformNames++fragment.uniformNames)
      .setSamplers(vertex.textureNames++fragment.textureNames)
  def set(parameter: String, newDefault: Some[_]) = ParameterisedShader(vertex.set(parameter, newDefault), fragment.set(parameter, newDefault))
}

final class ParameterisedShaderMaterial(name: String, scene: BABYLON.Scene, val shader: ParameterisedShader) {
  val underlying = BABYLON.ShaderMaterial(name, scene, shader.toShaderPath, shader.toShaderOpts)
  val params = shader.paramMap
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
