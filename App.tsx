/**
 * PAX Bridge React Native App
 * EMV Payment Processing Interface
 *
 * @format
 */

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  StatusBar,
  useColorScheme,
  ScrollView,
  TextInput,
} from 'react-native';
import {
  SafeAreaProvider,
  SafeAreaView,
} from 'react-native-safe-area-context';
import { NativeModules } from 'react-native';

const { DsiEMVManager } = NativeModules;

interface Config {
  merchantID: string;
  onlineMerchantID: string;
  isSandBox: boolean;
  secureDeviceName: string;
  operatorID: string;
  posPackageID: string;
  pinPadIPAddress: string;
  pinPadPort: string;
}

function App() {
  const isDarkMode = useColorScheme() === 'dark';

  return (
    <SafeAreaProvider>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <AppContent />
    </SafeAreaProvider>
  );
}

function AppContent() {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [amount, setAmount] = useState('10.00');
  const [config, setConfig] = useState<Config>({
    merchantID: 'SONNYTAMA35000GP',
    onlineMerchantID: 'SONNYTAMA35000GP',
    isSandBox: true,
    secureDeviceName: 'EMV_A920PRO_DATACAP_E2E',
    operatorID: 'SONNY1012',
    posPackageID: 'com.quivio.payment:1.0',
    pinPadIPAddress: '127.0.0.1',
    pinPadPort: '1235',
  });

  useEffect(() => {
    initializeEMV();
  }, []);

  const initializeEMV = async () => {
    try {
      setIsLoading(true);
      const result = await DsiEMVManager.initialize(config);
      console.log('EMV Manager initialized:', result);
      setIsInitialized(true);
      Alert.alert('Success', 'EMV Manager initialized successfully');
    } catch (error) {
      console.error('Failed to initialize EMV Manager:', error);
      Alert.alert('Error', `Failed to initialize: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSale = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'Please initialize EMV Manager first');
      return;
    }

    try {
      setIsLoading(true);
      const result = await DsiEMVManager.doSale(amount);
      console.log('Sale transaction initiated:', result);
      Alert.alert('Success', 'Sale transaction initiated');
    } catch (error) {
      console.error('Sale transaction failed:', error);
      Alert.alert('Error', `Sale failed: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePrepaidSale = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'Please initialize EMV Manager first');
      return;
    }

    try {
      setIsLoading(true);
      const result = await DsiEMVManager.readPrepaidCard();
      console.log('Prepaid card read initiated:', result);
      Alert.alert('Success', 'Prepaid card read initiated');
    } catch (error) {
      console.error('Prepaid card read failed:', error);
      Alert.alert('Error', `Prepaid card read failed: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRecurringSale = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'Please initialize EMV Manager first');
      return;
    }

    try {
      setIsLoading(true);
      const result = await DsiEMVManager.doRecurringSale(amount);
      console.log('Recurring sale initiated:', result);
      Alert.alert('Success', 'Recurring sale initiated');
    } catch (error) {
      console.error('Recurring sale failed:', error);
      Alert.alert('Error', `Recurring sale failed: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleZeroAuth = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'Please initialize EMV Manager first');
      return;
    }

    try {
      setIsLoading(true);
      const result = await DsiEMVManager.doSale('0.00');
      console.log('$0 Auth initiated:', result);
      Alert.alert('Success', '$0 Auth initiated');
    } catch (error) {
      console.error('$0 Auth failed:', error);
      Alert.alert('Error', `$0 Auth failed: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelTransaction = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'Please initialize EMV Manager first');
      return;
    }

    try {
      setIsLoading(true);
      const result = await DsiEMVManager.cancelTransaction();
      console.log('Transaction cancelled:', result);
      Alert.alert('Success', 'Transaction cancelled');
    } catch (error) {
      console.error('Cancel transaction failed:', error);
      Alert.alert('Error', `Cancel failed: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownloadConfig = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'Please initialize EMV Manager first');
      return;
    }

    try {
      setIsLoading(true);
      const result = await DsiEMVManager.downloadConfig();
      console.log('Config download initiated:', result);
      Alert.alert('Success', 'Config download initiated');
    } catch (error) {
      console.error('Config download failed:', error);
      Alert.alert('Error', `Config download failed: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <View style={styles.header}>
          <Text style={styles.title}>PAX EMV Payment</Text>
          <Text style={styles.subtitle}>EMV Payment Processing Interface</Text>
        </View>

        <View style={styles.statusContainer}>
          <Text style={styles.statusLabel}>Status:</Text>
          <Text style={[styles.statusText, { color: isInitialized ? '#4CAF50' : '#F44336' }]}>
            {isInitialized ? 'Initialized' : 'Not Initialized'}
          </Text>
        </View>

        <View style={styles.amountContainer}>
          <Text style={styles.label}>Transaction Amount:</Text>
          <TextInput
            style={styles.amountInput}
            value={amount}
            onChangeText={setAmount}
            placeholder="Enter amount"
            keyboardType="numeric"
            editable={!isLoading}
          />
        </View>

        <View style={styles.buttonContainer}>
          <TouchableOpacity
            style={[styles.button, styles.primaryButton]}
            onPress={handleSale}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>Sale</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.button, styles.secondaryButton]}
            onPress={handlePrepaidSale}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>Prepaid Sale</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.button, styles.tertiaryButton]}
            onPress={handleRecurringSale}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>Recurring Sale</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.button, styles.quaternaryButton]}
            onPress={handleZeroAuth}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>$0 Auth</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.utilityContainer}>
          <TouchableOpacity
            style={[styles.button, styles.utilityButton]}
            onPress={handleCancelTransaction}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>Cancel Transaction</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.button, styles.utilityButton]}
            onPress={handleDownloadConfig}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>Download Config</Text>
          </TouchableOpacity>
        </View>

        {isLoading && (
          <View style={styles.loadingContainer}>
            <Text style={styles.loadingText}>Processing...</Text>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollContainer: {
    flexGrow: 1,
    padding: 20,
  },
  header: {
    alignItems: 'center',
    marginBottom: 30,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
    padding: 15,
    backgroundColor: '#fff',
    borderRadius: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  statusLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginRight: 10,
  },
  statusText: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  amountContainer: {
    marginBottom: 30,
    padding: 20,
    backgroundColor: '#fff',
    borderRadius: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 10,
  },
  amountInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  buttonContainer: {
    marginBottom: 20,
  },
  button: {
    padding: 15,
    borderRadius: 10,
    marginBottom: 15,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  primaryButton: {
    backgroundColor: '#2196F3',
  },
  secondaryButton: {
    backgroundColor: '#4CAF50',
  },
  tertiaryButton: {
    backgroundColor: '#FF9800',
  },
  quaternaryButton: {
    backgroundColor: '#9C27B0',
  },
  utilityButton: {
    backgroundColor: '#607D8B',
    marginBottom: 10,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  utilityContainer: {
    marginTop: 20,
  },
  loadingContainer: {
    alignItems: 'center',
    marginTop: 20,
  },
  loadingText: {
    fontSize: 16,
    color: '#666',
    fontStyle: 'italic',
  },
});

export default App;
