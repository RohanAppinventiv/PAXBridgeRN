/**
 * Configuration file for PAX Bridge React Native
 * 
 * This file contains the configuration settings for the EMV payment processing.
 * IMPORTANT: This file should be added to .gitignore to prevent sensitive
 * configuration data from being committed to the repository.
 */

export interface Config {
  merchantID: string;
  onlineMerchantID: string;
  isSandBox: boolean;
  secureDeviceName: string;
  operatorID: string;
  posPackageID: string;
  pinPadIPAddress: string;
  pinPadPort: string;
}

/**
 * Default configuration for development/testing
 * 
 * WARNING: These are example values. Replace with your actual configuration
 * before using in production. Never commit real credentials to version control.
 */
export const defaultConfig: Config = {
  merchantID: 'SONNYTAMA35000GP',
  onlineMerchantID: 'SONNYTAMA35000GP',
  isSandBox: true,
  secureDeviceName: 'EMV_A920PRO_DATACAP_E2E',
  operatorID: 'SONNY1012',
  posPackageID: 'com.quivio.payment:1.0',
  pinPadIPAddress: '127.0.0.1',
  pinPadPort: '1235',
};

/**
 * Production configuration
 * 
 * This will be imported from config.production.ts if it exists,
 * otherwise it will fall back to default values.
 */
let productionConfig: Config;

try {
  // Try to import production configuration
  const prodConfig = require('./config.production');
  productionConfig = prodConfig.productionConfig;
} catch (error) {
  // Fall back to default production config if file doesn't exist
  productionConfig = {
    merchantID: 'YOUR_PRODUCTION_MERCHANT_ID',
    onlineMerchantID: 'YOUR_PRODUCTION_ONLINE_MERCHANT_ID',
    isSandBox: false,
    secureDeviceName: 'YOUR_PRODUCTION_DEVICE_NAME',
    operatorID: 'YOUR_PRODUCTION_OPERATOR_ID',
    posPackageID: 'YOUR_PRODUCTION_PACKAGE_ID',
    pinPadIPAddress: 'YOUR_PRODUCTION_IP_ADDRESS',
    pinPadPort: 'YOUR_PRODUCTION_PORT',
  };
}

export { productionConfig };

/**
 * Get the appropriate configuration based on environment
 * 
 * @param isProduction Whether to use production configuration
 * @returns The configuration object
 */
export function getConfig(isProduction: boolean = false): Config {
  return isProduction ? productionConfig : defaultConfig;
}
