package hextactoe

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

// import javascriptLogo from "/javascript.svg"
@js.native @JSImport("/javascript.svg", JSImport.Default)
val javascriptLogo: String = js.native

@main
def HexTacToe(): Unit = {
  renderOnDomContentLoaded(dom.document.getElementById("app"), Main.appElement())
  renderOnDomContentLoaded(dom.document.getElementById("test"), counterButton())
}

object Main {
  def appElement(): Element = {
    div(
      a(href := "https://vitejs.dev", target := "_blank",
        img(src := "/vite.svg", className := "logo", alt := "Vite logo"),
      ),
      img(src := "/binoculars_fill.svg", className := "logo", alt := "binoculars"),
      a(href := "https://developer.mozilla.org/en-US/docs/Web/JavaScript", target := "_blank",
        img(src := javascriptLogo, className := "logo vanilla", alt := "JavaScript logo"),
      ),
      h1("Hello Laminar & Scala.js!"),
      div(idAttr := "test",
        className := "card"
      ),
      p(className := "read-the-docs",
        "Click on the Vite logo to learn more",
      ),
    )
  }
}

def counterButton(): Element = {
    val counter = Var(0)
    button(
      tpe := "button",
      "count is ",
      child.text <-- counter,
      onClick --> { event => counter.update(c => c + 1) },
    )
  }