# HexTacToe

Simple hex based tic-tac-toe game as a Scala.js and Babylon.js proof-of-concept.

## Demonstrated functionality

* Scala & Babylon.js interoperability
* Using Vite for fast turnaround of changes
* Features: drawing on planes; bespoke shaders; load/display meshes; particles; mouse events; and detecting where on the screen a click has occurred
* Tweening; including of shader material parameters
* Deployment to Github Pages

## Notes

* No extensions allowed in Scala.js
* Got a weird error with some valid Scala code making use of generics. Adding a `println` fixed the issue (seriously :/ - reminded me of my old C programming days!). Decided just to remove the generics. It is the `run` method in TweenManager.
* When loading a mesh, ensure that the name provided matches the name inside the loaded file
* base folder? [[check latest idea]]
* `typings.babylonjs.*` vs `typings.babylonjs.global.*`
* Why proxy ShaderMaterial
* ParameterisedShaderMaterial and overload problems

## Deploy to Github Pages

From [the comments in this article](https://dev.to/shashannkbawa/deploying-vite-app-to-github-pages-3ane):

1. add `base: "/<repo>/"` to vite.config.js in the `defineConfig` section.
2. `npm install gh-pages --save-dev`
3. add `"homepage": "https://<username>.github.io/<repo>/"` to package.json in the root section
4. package.json in the `build` section add

```javascript
    "predeploy": "npm run build",
    "deploy": "gh-pages -d dist",
```

Then `npm run deploy` should deploy to github in a the `gh-pages` branch and pages should be available (if not change it to use that branch in settings).
