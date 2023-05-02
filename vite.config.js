import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

var baseUrl = process.env['BASE_URL'];
if (baseUrl == undefined || baseUrl.trim()==="/") { // required as Electron.js does not like 
  console.log("Rewriting root Base URL to empty")
  baseUrl = "";
  process.env['BASE_URL'] = baseUrl;
} else {
  baseUrl = baseUrl.trim();
}
console.log("Base URL: '"+baseUrl+"'")
export default defineConfig({
  base: baseUrl,
  plugins: [scalaJSPlugin()],
});

