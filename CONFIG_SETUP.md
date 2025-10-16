# Configuration Setup Guide

This guide explains how to properly configure the PAX Bridge React Native application with your sensitive configuration data while keeping it secure.

## 🔒 Security Overview

The application uses a secure configuration system that:
- Keeps sensitive data out of version control
- Provides different configurations for development and production
- Uses template files for easy setup

## 📁 Configuration Files

### Core Files (Safe to Commit)
- `config.ts` - Main configuration file with development defaults
- `config.template.ts` - Template for production configuration
- `CONFIG_SETUP.md` - This setup guide

### Sensitive Files (Added to .gitignore)
- `config.production.ts` - Production configuration with real credentials
- `config.local.ts` - Local development overrides
- `.env` - Environment variables (if using env-based config)

## 🚀 Setup Instructions

### 1. Development Configuration

The development configuration is already set up in `config.ts` with example values. You can modify these values directly in the file for development purposes.

### 2. Production Configuration

To set up production configuration:

1. **Copy the template file:**
   ```bash
   cp config.template.ts config.production.ts
   ```

2. **Edit the production configuration:**
   Open `config.production.ts` and replace all placeholder values with your actual production credentials:
   ```typescript
   export const productionConfig: Config = {
     merchantID: 'your-actual-merchant-id',
     onlineMerchantID: 'your-actual-online-merchant-id',
     isSandBox: false,
     secureDeviceName: 'your-actual-device-name',
     operatorID: 'your-actual-operator-id',
     posPackageID: 'your-actual-package-id',
     pinPadIPAddress: 'your-actual-ip-address',
     pinPadPort: 'your-actual-port',
   };
   ```

3. **Verify the file is ignored:**
   The `config.production.ts` file should be automatically ignored by git. You can verify this by running:
   ```bash
   git status
   ```
   The file should not appear in the git status output.

### 3. Using Different Configurations

In your application code, you can switch between configurations:

```typescript
import { getConfig } from './config';

// Development configuration
const devConfig = getConfig(false);

// Production configuration
const prodConfig = getConfig(true);
```

## 🔧 Configuration Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `merchantID` | Your merchant identifier | `'MERCHANT123'` |
| `onlineMerchantID` | Online merchant identifier | `'ONLINE_MERCHANT123'` |
| `isSandBox` | Environment flag | `true` for sandbox, `false` for production |
| `secureDeviceName` | Terminal device name | `'EMV_A920PRO_DATACAP_E2E'` |
| `operatorID` | Operator/employee ID | `'OPERATOR123'` |
| `posPackageID` | POS package identifier | `'com.company.payment:1.0'` |
| `pinPadIPAddress` | PIN pad IP address | `'192.168.1.100'` |
| `pinPadPort` | PIN pad port number | `'8080'` |

## 🛡️ Security Best Practices

1. **Never commit sensitive files:**
   - `config.production.ts`
   - `config.local.ts`
   - `.env` files

2. **Use environment-specific configurations:**
   - Development: Use `config.ts` with test values
   - Production: Use `config.production.ts` with real values

3. **Regular security audits:**
   - Review configuration files periodically
   - Rotate credentials as needed
   - Monitor for accidental commits

4. **Team coordination:**
   - Share production configuration securely (not via git)
   - Use secure communication channels for sensitive data
   - Document the setup process for new team members

## 🚨 Troubleshooting

### Configuration Not Loading
- Ensure `config.production.ts` exists and is properly formatted
- Check that the file is not being ignored by your build system
- Verify the import paths are correct

### Git Still Tracking Sensitive Files
- Check your `.gitignore` file includes the sensitive file patterns
- Remove already tracked files: `git rm --cached config.production.ts`
- Commit the removal: `git commit -m "Remove sensitive config from tracking"`

### Build Errors
- Ensure all required configuration parameters are provided
- Check for typos in parameter names
- Verify TypeScript types match the interface

## 📞 Support

If you encounter issues with configuration setup:
1. Check this guide first
2. Review the main README.md
3. Check the troubleshooting section
4. Create an issue in the repository

## 🔄 Updates

When updating configuration:
1. Update the template file (`config.template.ts`)
2. Update this guide if needed
3. Notify team members of changes
4. Test in development before production deployment
