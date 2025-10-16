# PAX Bridge React Native

A React Native bridge library for integrating PAX payment terminals with mobile applications. This project provides a comprehensive solution for EMV payment processing, card reading, and configuration management through a clean React Native interface.

## 🚀 Features

- **EMV Payment Processing**: Complete support for EMV sale and recurring transactions
- **Card Reading**: Prepaid card data extraction and validation
- **Configuration Management**: Dynamic configuration download and management
- **Error Handling**: Comprehensive error handling and recovery mechanisms
- **Event-Driven Architecture**: Real-time event communication between native and JavaScript layers
- **Cross-Platform Support**: Android and iOS compatibility
- **TypeScript Support**: Full TypeScript definitions for type safety

## 📋 Prerequisites

- React Native 0.70+
- Android Studio (for Android development)
- Xcode (for iOS development)
- PAX payment terminal hardware
- DSI EMV Android library (`.aar` file)

## 🛠️ Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd PAXBridgeRN
```

### 2. Install Dependencies

```bash
# Install JavaScript dependencies
npm install

# For iOS, install CocoaPods dependencies
cd ios && pod install && cd ..
```

### 3. Android Setup

1. Place the DSI EMV Android library (`.aar` file) in `android/app/libs/`
2. Ensure the library is properly referenced in `android/app/build.gradle`

### 4. iOS Setup

1. Follow the standard React Native iOS setup process
2. Install CocoaPods dependencies as shown above

## 🏗️ Architecture

### Native Bridge Components

#### Android
- **`DsiEMVManagerModule`**: Main React Native bridge module
- **`POSConfigFactory`**: Configuration management and validation
- **`DsiEMVManager`**: Core EMV transaction management
- **`POSTransactionExecutor`**: Transaction execution and coordination
- **`XMLResponseExtractor`**: Response parsing and data extraction

#### iOS
- **`PAXPaymentPackage`**: iOS bridge package
- **`DsiEMVManagerModule`**: iOS implementation of the bridge module

### Key Interfaces

- **`ConfigFactory`**: Configuration data contract
- **`EMVTransactionCommunicator`**: Transaction event callbacks
- **`ConfigurationCommunicator`**: Configuration event callbacks

## 📱 Usage

### 1. Import the Module

```typescript
import { DsiEMVManager } from 'react-native-pax-bridge';
```

### 2. Initialize the Manager

```typescript
const config = {
  merchantID: 'your-merchant-id',
  onlineMerchantID: 'your-online-merchant-id',
  isSandBox: true, // true for sandbox, false for production
  secureDeviceName: 'your-device-name',
  operatorID: 'operator-123',
  posPackageID: 'your-package-id',
  pinPadIPAddress: '192.168.1.100',
  pinPadPort: '8080'
};

await DsiEMVManager.initialize(config);
```

### 3. Set Up Event Listeners

```typescript
import { DeviceEventEmitter } from 'react-native';

// Listen for transaction events
DeviceEventEmitter.addListener('SALE_SUCCESS', (data) => {
  console.log('Sale completed:', data);
});

DeviceEventEmitter.addListener('ERROR_EVENT', (error) => {
  console.error('Transaction error:', error);
});

// Listen for configuration events
DeviceEventEmitter.addListener('CONFIG_COMPLETED', (data) => {
  console.log('Configuration completed:', data);
});
```

### 4. Execute Transactions

```typescript
// Perform a sale transaction
await DsiEMVManager.doSale('10.00');

// Perform a recurring sale
await DsiEMVManager.doRecurringSale('25.00');

// Read prepaid card data
await DsiEMVManager.readPrepaidCard();

// Replace card in recurring setup
await DsiEMVManager.replaceCreditCard();

// Download configuration
await DsiEMVManager.downloadConfig();

// Get client version
await DsiEMVManager.getClientVersion();

// Cancel current transaction
await DsiEMVManager.cancelTransaction();
```

### 5. Cleanup

```typescript
// Clean up resources when done
await DsiEMVManager.cleanup();
```

## 📊 Event Types

### Transaction Events
- `SALE_SUCCESS`: Sale transaction completed successfully
- `RECURRING_SALE_SUCCESS`: Recurring sale transaction completed
- `ZERO_AUTH_SUCCESS`: Card replacement transaction completed
- `PREPAID_READ_SUCCESS`: Prepaid card data read successfully
- `ERROR_EVENT`: Transaction or operation error occurred

### Configuration Events
- `CONFIG_COMPLETED`: Configuration setup completed
- `CONFIG_PING_SUCCESS`: Configuration ping successful
- `CONFIG_PING_FAIL`: Configuration ping failed
- `CONFIG_ERROR`: Configuration error occurred

### System Events
- `MESSAGE_EVENT`: System message from EMV library
- `CLIENT_VERSION_FETCH_SUCCESS`: Client version retrieved successfully

## 🔧 Configuration

### Required Configuration Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `merchantID` | string | Unique merchant identifier |
| `onlineMerchantID` | string | Online merchant identifier |
| `isSandBox` | boolean | Environment flag (true = sandbox, false = production) |
| `secureDeviceName` | string | Terminal device name |
| `operatorID` | string | Operator/employee identifier |
| `posPackageID` | string | POS package identifier |
| `pinPadIPAddress` | string | PIN pad IP address |
| `pinPadPort` | string | PIN pad port number |

## 🚀 Getting Started

### Step 1: Start Metro

```bash
npm start
# OR
yarn start
```

### Step 2: Build and Run

#### Android
```bash
npm run android
# OR
yarn android
```

#### iOS
```bash
npm run ios
# OR
yarn ios
```

## 🧪 Testing

The project includes comprehensive test coverage for all major components:

```bash
# Run tests
npm test

# Run tests with coverage
npm run test:coverage
```

## 📚 API Reference

### DsiEMVManager Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `initialize` | `config: ConfigObject` | Initialize the EMV manager |
| `doSale` | `amount: string` | Execute a sale transaction |
| `doRecurringSale` | `amount: string` | Execute a recurring sale |
| `readPrepaidCard` | - | Read prepaid card data |
| `replaceCreditCard` | - | Replace card in recurring setup |
| `downloadConfig` | - | Download configuration parameters |
| `getClientVersion` | - | Get client version information |
| `cancelTransaction` | - | Cancel current transaction |
| `cleanup` | - | Clean up resources |

## 🔒 Security Considerations

- All sensitive data is handled securely through the native layer
- Configuration data is validated before use
- Transaction data is encrypted during transmission
- Proper error handling prevents data leakage

## 🐛 Troubleshooting

### Common Issues

1. **Initialization Failed**
   - Verify all configuration parameters are provided
   - Check network connectivity to PIN pad
   - Ensure DSI EMV library is properly installed

2. **Transaction Errors**
   - Verify PIN pad connectivity
   - Check merchant configuration
   - Ensure proper card insertion

3. **Configuration Issues**
   - Verify network settings
   - Check PIN pad IP address and port
   - Ensure proper authentication credentials

### Debug Mode

Enable debug logging by setting the log level in the native configuration:

```typescript
// Enable debug logging
const config = {
  // ... other config
  debugMode: true
};
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation

## 🔄 Version History

- **v1.0.0**: Initial release with basic EMV functionality
- **v1.1.0**: Added recurring payment support
- **v1.2.0**: Enhanced error handling and configuration management
- **v1.3.0**: Added TypeScript support and improved documentation
