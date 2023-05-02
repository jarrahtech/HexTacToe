# HexTacToe

Simple hex based tic-tac-toe game as a Scala.js and Babylon.js proof-of-concept. Also includes Vite.js for quick dev turnaround and Electron.js for native packaging.

[See the game running here](https://www.jarrahtechnology.com/HexTacToe/)

It is not fancy, it just shows how to get all these technologies to work together.

## Demonstrated functionality

* Scala & Babylon.js interoperability (including libraries)
* Using Vite for fast turnaround of changes
* Babylon features: drawing on planes; bespoke shaders; load/display 3rd party meshes; particles; mouse events; and detecting where on the screen a click has occurred
* Tweening; including of shader material parameters
* Deployment to Github Pages
* Packaging to a native executable

## Tutorials used

* [Scala.js](https://www.scala-js.org/doc/tutorial/scalajs-vite.html)
* [Vite](https://vitejs.dev/guide/)
* [Babylon](https://doc.babylonjs.com/journey/theFirstStep)
* [Electron](https://www.electronjs.org/docs/latest/tutorial/quick-start)
* [Electron-Packager](https://electron.github.io/electron-packager/main/index.html)
* [Deploy to Github Pages (use the first comment, not the article itself :)](https://dev.to/shashannkbawa/deploying-vite-app-to-github-pages-3ane#comment-22iei)

## Setup

`npm install`

## Notes

* It appears that no extension methods are allowed in Scala.js
* Got a weird error with some valid Scala code making use of generics. Adding a `println` fixed the issue (seriously :/ - reminded me of my old C programming days!). Decided just to remove the generics. It is the `run` method in TweenManager if you want to try fixing it
* When loading a mesh, ensure that the name provided matches the name inside the loaded file
* base folder? [[check latest idea]]
* `typings.babylonjs.*` vs `typings.babylonjs.global.*`
* Why proxy ShaderMaterial
* ParameterisedShaderMaterial and overload problems
* `@js.native @JSImport("/SpaceKit_Kenney", JSImport.Default); val meshesUrl: String = js.native` handles Vite's base url when pointing to a file, but does not seem to like folders (as used by `BABYLON.SceneLoader.ImportMesh`) so have to import via writing the base path to the environment inside vite config, then picking that up environment variable inside sbt and writing to a generated file (using sbt-buildinfo) and then using that inside scala
* electron-forge couldn't get it to work as it requires packaging from the root directory and disallows setting the "dir" config option
* in package.json `"main": "electron.cjs"` is the electron build entry point, should be ignored by the web build
* electron gets confused by baseURL set to / (tries to load out of the root of the filesystem), so catch and rewrite this as blank in `vite.config.js`
* electron going to network for babylon js & bootstrap stylesheet, probably want to include these locally
* electron wants a CSP set (see the electron.cjs files) & https://www.electronjs.org/docs/latest/tutorial/security#7-define-a-content-security-policy
