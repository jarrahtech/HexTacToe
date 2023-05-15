# HexTacToe

Simple hex based tic-tac-toe game as a Scala.js and Babylon.js proof-of-concept. Also includes Vite.js for quick dev turnaround and Electron.js for native packaging. ScalablyTyped was used to create the Babylon.js facade.

[See the game running here](https://www.jarrahtechnology.com/HexTacToe/)

It is not fancy, it just shows how to get all these technologies to work together.

## Demonstrated functionality

* Scala & Babylon.js interoperability (including libraries)
* Using Vite for fast turnaround of changes
* Babylon.js features: drawing on planes; bespoke shaders; load/display 3rd party meshes; particles; mouse events; and detecting where on the screen a click has occurred
* Tweening; including of shader material parameters
* Deployment to Github Pages
* Packaging to a native executable

## Tutorials used

* [Scala.js](https://www.scala-js.org/doc/tutorial/scalajs-vite.html)
* [Vite](https://vitejs.dev/guide/)
* [Babylon.js](https://doc.babylonjs.com/journey/theFirstStep)
* [ScalablyTyped](https://scalablytyped.org/docs/library-developer)
* [Electron.js](https://www.electronjs.org/docs/latest/tutorial/quick-start)
* [Electron-Packager](https://electron.github.io/electron-packager/main/index.html)
* [Deploy to Github Pages (use the first comment, not the article itself :)](https://dev.to/shashannkbawa/deploying-vite-app-to-github-pages-3ane#comment-22iei)

## Setup

1. First do `npm install` which should install the javascript packages required.
2. `sbt fastLinkJS` will build all the files for running it in dev
3. `npm run dev` (on a webpage) and `npm run dev:electron` (as an executable) will run a dev version

Also:

* if you have permission, `npm run deploy` will upload to github pages
* `npm run build` will create optimised javascript in the `dist` folder
* `npm run package` will create an electron executable of the game under the folder `electron/out`

## Notes

* The Babylon.js code was imported in Scala.js using ScalablyTyped. That facade is in a separate library (`libraryDependencies += "com.jarrahtechnology" %%% "babylonjsfacade" % "6.2.0"` for the 6.2 release of Babylon.js) so that all the different libraries that use it don't need to import and compile it repeatedly (it is quite large).
* It appears that extension methods can not be exported in Scala.js, so I have not used them
* When loading a mesh, ensure that the name provided matches the name inside the loaded file (it may not be what you expect)
* `typings.babylonjs.*` contains Babylon.js' interfaces while `typings.babylonjs.global.*` contains the concrete classes (this took me far to long to work out). To make this clearer, the latter is imported as `BABYLON_IMPL` in the scala code
* ShaderMaterial has been proxied with ParameterisedShaderMaterial (now in the kassite library) so that the values of the shader parameters can have getters as well as setters. This is useful for tweening.
* Some of the proxied setter methods in ParameterisedShaderMaterial are commented out. This is because compiler errors resulted if any of them were active - they are ambiguous which is probably something to do with how these parameters are seen in javascript. So 2x2 & 3x3 matrices can not be be used by this class.
* When loading external files in scala, the scala.js docs suggest using something like: `@js.native @JSImport("/SpaceKit_Kenney", JSImport.Default); val meshesUrl: String = js.native` as this handles Vite's base url. However, it does not seem to like folders (as used by `BABYLON.SceneLoader.ImportMesh`) so instead I imported the folder path via writing the base path to the environment inside vite config, then picking that up environment variable inside sbt and writing to a generated file (using sbt-buildinfo) and then using that inside scala.
* Particles (shown when you lose) are done through one of the default Babylon particle effects which is downloaded as needed. Thus its start can be delayed while for it to be downloaded. Probably should do this inside the code or preload it instead.
* I could not get electron-forge to work as it requires packaging from the root directory and disallows setting the "dir" config option
* In `package.json` the line `"main": "electron.cjs"` is the electron.js build entry point, and this should be ignored by the web build. I have created two such files, one for dev and one for prod - they are copied as required in `package.json`
* electron.js gets confused by baseURL set to `/` (it tries to load out of the root of the filesystem), so the build system catches this and rewrites it as blank in `vite.config.js`
* electron.js goes to the network for the babylon javascript file, bootstrap stylesheet and the particles definition, probably want to include these locally in the build
* electron.js wants a Content Security Policy (CSP) (it is currently set in the electron.cjs files) see [here for more info](https://www.electronjs.org/docs/latest/tutorial/security#7-define-a-content-security-policy)
* BabylonGrid is a bit of mess, could probably do this a lot better, but it works
