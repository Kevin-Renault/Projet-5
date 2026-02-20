import { defineConfig } from "cypress";
import fs from "fs";
import path from "path";

export default defineConfig({
  allowCypressEnv: false,

  e2e: {
    baseUrl: "http://localhost:4201",
    viewportWidth: 1920,
    viewportHeight: 1080,
    specPattern: "cypress/e2e/**/*.cy.{js,jsx,ts,tsx}",
    supportFile: "cypress/support/e2e.ts",
    setupNodeEvents(on, config) {
      on("task", {
        saveCoverage(coverage) {
          if (!coverage) {
            return null;
          }

          const outDir = path.join(config.projectRoot, ".nyc_output");
          fs.mkdirSync(outDir, { recursive: true });

          const filename = `out_${Date.now()}_${Math.random().toString(16).slice(2)}.json`;
          fs.writeFileSync(path.join(outDir, filename), JSON.stringify(coverage), "utf8");
          return null;
        },
      });

      return config;
    },
  },
});
