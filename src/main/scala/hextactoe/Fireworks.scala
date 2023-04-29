package hextactoe

import scala.concurrent.duration._
import typings.babylonjs.global._
import com.jarrahtechnology.kassite.shader._
import com.jarrahtechnology.kassite.shader.ShaderParamType._
import com.jarrahtechnology.kassite.tween._
import typings.std.stdStrings.del

// from https://playground.babylonjs.com/#1OH09K#1672
object Fireworks {
  lazy val fireworkShader = ParameterisedShader(
    VertexShader("firework", 
                  ShaderParams(List(FloatShaderParameter("time", Uniform, Some(0d)))), 
                  "precision highp float; attribute vec3 position; attribute vec3 normal; uniform mat4 worldViewProjection; uniform float time; void main(void) { vec3 p = position; vec3 j = vec3(0., -1.0, 0.); p = p + normal * log2(1. + time) * 25.0; gl_Position = worldViewProjection * vec4(p, 1.0); }"),
    FragmentShader("unlit", 
                  ShaderParams(List(FloatShaderParameter("time", Uniform, Some(0d)))), 
                  "precision highp float; uniform float time; void main(void) { gl_FragColor = vec4(1. - log2(1. + time)/100., 1. * log2(1. + time), 0., 1. - log2(1. + time/2.)/log2(1. + 3.95)); }")
  )
  //TODO: needAlphaBlending: true & shaderMaterial.backFaceCulling = false;

  def fireworks(scene: BABYLON.Scene, tweenMgr: TweenManager, count: Int) = {
    (1 to count).foreach(_ =>
        firework(scene, tweenMgr, math.random()*0.5d+0.75d, 
                    Duration(math.random()*4000+1000, MILLISECONDS), 
                    Duration(math.random()*3000-1000, MILLISECONDS), 
                    math.random()*0.6d+0.7d,
                    math.random()*0.4d+0.8d)
    )
  }

  def firework(scene: BABYLON.Scene, tweenMgr: TweenManager, size: Double, duration: Duration, delay: Duration, speed: Double, scale: Double) = {
    val sphere = BABYLON.Mesh.CreateSphere("fireworks_sphere", 32, size, scene)
    sphere.convertToFlatShadedMesh()
    sphere.position = BABYLON.Vector3(math.random()*6-3, math.random()*6-3, math.random()*6-3)
    val mat = fireworkShader.toMaterial(scene)
    sphere.material = mat
    val t = MaterialTween.shaderFloatParameter(duration, delay, mat, "time", speed, Some(_ => sphere.dispose()))
            .runOn(tweenMgr)
    t.setTimeScale(scale)
  }
}
