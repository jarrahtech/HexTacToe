package com.jarrahtechnology.kassite.shader

import com.jarrahtechnology.kassite.shader.ShaderType._
import com.jarrahtechnology.kassite.shader.ShaderParamType._

object Shaders {
    
  lazy val unlitTransparent = ParameterisedShader(
    SubShader("basic2", Vertex, Map.empty, "precision highp float;attribute vec3 position;attribute vec2 uv;uniform mat4 worldViewProjection;varying vec2 vUV;void main(void){gl_Position=worldViewProjection*vec4(position,1.0);vUV =uv;}"),
    SubShader("unlit2", Fragment, Map("color" -> Uniform, "opacity" -> Uniform, "tex" -> Uniform), "precision highp float;varying vec2 vUV;uniform sampler2D tex;uniform vec3 color;uniform float opacity;void main(void){gl_FragColor=vec4(color,opacity);}")
  )
}
