/**
 * Configuration Template for PAX Bridge React Native
 * 
 * This is a template file for creating your own configuration.
 * Copy this file to config.production.ts and replace the values
 * with your actual production configuration.
 * 
 * IMPORTANT: 
 * - Never commit config.production.ts to version control
 * - This template file can be safely committed
 * - Replace all placeholder values with your actual configuration
 */

import { Config } from './config';

/**
 * Production configuration template
 * 
 * Copy this file to config.production.ts and replace the values below:
 */
export const productionConfig: Config = {
  // Replace with your actual merchant ID
  merchantID: 'YOUR_PRODUCTION_MERCHANT_ID',
  
  // Replace with your actual online merchant ID
  onlineMerchantID: 'YOUR_PRODUCTION_ONLINE_MERCHANT_ID',
  
  // Set to false for production environment
  isSandBox: false,
  
  // Replace with your actual secure device name
  secureDeviceName: 'YOUR_PRODUCTION_DEVICE_NAME',
  
  // Replace with your actual operator ID
  operatorID: 'YOUR_PRODUCTION_OPERATOR_ID',
  
  // Replace with your actual POS package ID
  posPackageID: 'YOUR_PRODUCTION_PACKAGE_ID',
  
  // Replace with your actual PIN pad IP address
  pinPadIPAddress: 'YOUR_PRODUCTION_IP_ADDRESS',
  
  // Replace with your actual PIN pad port
  pinPadPort: 'YOUR_PRODUCTION_PORT',
};
