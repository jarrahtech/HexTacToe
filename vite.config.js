import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

var baseUrl = "/HexTacToe/";
process.env['BASE_URL'] = baseUrl;
export default defineConfig({
  base: baseUrl,
  plugins: [scalaJSPlugin()],
});

