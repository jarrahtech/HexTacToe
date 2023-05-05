package com.jarrahtechnology.kassite.shader

import typings.babylonjs.*
import typings.babylonjs.global.BABYLON as BABYLON_IMPL
import com.jarrahtechnology.kassite.shader.ShaderParamType._

object Shaders {
    
  lazy val unlitTransparent = ParameterisedShader(
    VertexShader("basic", 
                  ShaderParams(List.empty), 
                  "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}"),
    FragmentShader("unlit", 
                  ShaderParams(List(Color3ShaderParameter("color", Uniform, Some(BABYLON_IMPL.Color3(1,1,1))), FloatShaderParameter("opacity", Uniform, Some(0.99d)), TextureShaderParameter("tex", Uniform, None))), 
                  "precision highp float;varying vec2 vUV;uniform sampler2D tex;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=texture2D(tex,vUV)*vec4(color,opacity);}")
  )
}
