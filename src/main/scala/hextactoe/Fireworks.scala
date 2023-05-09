package hextactoe

import scala.concurrent.duration.*
import facade.babylonjs.global.BABYLON as BABYLON_IMPL
import com.jarrahtechnology.kassite.shader.*
import com.jarrahtechnology.kassite.shader.ShaderParamType.*
import com.jarrahtechnology.kassite.tween.*

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

  def fireworks(state: GameState, count: Int) = {
    (1 to count).foreach(_ =>
        firework(state, math.random()*0.5d+0.7d, 
                    Duration(math.random()*6000+2000, MILLISECONDS), 
                    Duration(math.max(1, math.random()*2000-1000), MILLISECONDS), 
                    math.random()*3+1d)
    )
  }

  def firework(state: GameState, size: Double, duration: Duration, delay: Duration, scale: Double) = {
    val sphere = BABYLON_IMPL.Mesh.CreateSphere("fireworks_sphere", 32, size, state.scene)
    sphere.convertToFlatShadedMesh()
    sphere.position = BABYLON_IMPL.Vector3(math.random()*6-3, math.random()*6-3, math.random()*10+5)
    val mat = fireworkShader.toMaterial(state.scene)
    sphere.material = mat
    sphere.scaling.setAll(0)
    val t = MaterialTween.shaderFloatParameter(duration, delay, mat, "time", 8, Some(_ => sphere.scaling.setAll(1)), Some(_ => sphere.dispose()))
            .runOn(state.tweenMgr)
    t.setTimeScale(scale)
  }
}
