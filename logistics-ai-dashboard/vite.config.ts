import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  base: '/',
  plugins: [react()],
  build: {
    outDir: '../app/src/main/resources/static', // relative to vite.config.js
    emptyOutDir: true
  }
});
