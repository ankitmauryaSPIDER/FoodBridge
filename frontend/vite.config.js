import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  if (mode === 'production' && !process.env.VITE_API_BASE_URL) {
    throw new Error('VITE_API_BASE_URL must be set for production builds.');
  }

  return {
    plugins: [react()],
    server: { host: '0.0.0.0', port: 5173 },
  };
});
