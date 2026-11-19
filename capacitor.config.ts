import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.assuretechnologies.customer',
  appName: 'Assure',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    hostname: 'roboticnaturalautofarming.com',
    allowNavigation: [
      'roboticnaturalautofarming.com',
      '*.roboticnaturalautofarming.com',
      'assuretechnologies-backend-production-75c1.up.railway.app',
      '*.railway.app',
      '*.razorpay.com',
      'api.razorpay.com',
      'checkout.razorpay.com',
      '*.phonepe.com',
      'mercury.phonepe.com',
      'mercury-t2.phonepe.com',
      '*.paytm.com',
      '*.paytmbank.com',
      '*.npci.org.in',
      '*.google.com',
      '*.cashfree.com',
      '*.billdesk.com',
      '*.ccavenue.com',
      '*.payu.in',
      '*.mobikwik.com',
      '*.freecharge.in'
    ]
  },
  plugins: {
    CapacitorHttp: {
      enabled: true,
    },
  },
};

export default config;
