import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.assuretechnologies.customer',
  appName: 'Assure',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    hostname: 'assuretechnologies-backend-production-75c1.up.railway.app'
  },
  plugins: {
    CapacitorHttp: {
      enabled: true,
    },
  },
};

export default config;
