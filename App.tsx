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
import { NativeModules, DeviceEventEmitter } from 'react-native';
import { Config, getConfig } from './config';

const { DsiEMVManager } = NativeModules;

// Event names mirrored from native `BridgeEvent`
const BridgeEvent = {
  ERROR_EVENT: 'ERROR_EVENT',
  SALE_SUCCESS: 'SALE_SUCCESS',
  PREPAID_READ_SUCCESS: 'PREPAID_READ_SUCCESS',
  RECURRING_SALE_SUCCESS: 'RECURRING_SALE_SUCCESS',
  ZERO_AUTH_SUCCESS: 'ZERO_AUTH_SUCCESS',
  CLIENT_VERSION_FETCH_SUCCESS: 'CLIENT_VERSION_FETCH_SUCCESS',
  MESSAGE_EVENT: 'MESSAGE_EVENT',
  CONFIG_PING_SUCCESS: 'CONFIG_PING_SUCCESS',
  CONFIG_PING_FAIL: 'CONFIG_PING_FAIL',
  CONFIG_ERROR: 'CONFIG_ERROR',
  CONFIG_COMPLETED: 'CONFIG_COMPLETED',
} as const;

// Config interface is now imported from './config'

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
  const [lastEvent, setLastEvent] = useState<{ name: string; payload?: any } | null>(null);
  // Load configuration from external file
  const [config, setConfig] = useState<Config>(getConfig(false)); // false = development mode

  useEffect(() => {
    initializeEMV();
  }, []);

  // Centralized native event subscriptions
  useEffect(() => {
    const subs = [
      DeviceEventEmitter.addListener(BridgeEvent.ERROR_EVENT, (payload) => {
        console.log('[EVENT]', BridgeEvent.ERROR_EVENT, payload);
        setIsLoading(false);
        setLastEvent({ name: BridgeEvent.ERROR_EVENT, payload });
        Alert.alert('Error', payload?.textResponse || 'Operation failed');
      }),
      DeviceEventEmitter.addListener(BridgeEvent.SALE_SUCCESS, (payload) => {
        console.log('[EVENT]', BridgeEvent.SALE_SUCCESS, payload);
        setIsLoading(false);
        setLastEvent({ name: BridgeEvent.SALE_SUCCESS, payload });
        Alert.alert('Sale Success', `Auth: ${payload?.authCode ?? 'N/A'}  Amount: ${payload?.amount?.purchase ?? ''}`);
      }),
      DeviceEventEmitter.addListener(BridgeEvent.PREPAID_READ_SUCCESS, (payload) => {
        console.log('[EVENT]', BridgeEvent.PREPAID_READ_SUCCESS, payload);
        setIsLoading(false);
        setLastEvent({ name: BridgeEvent.PREPAID_READ_SUCCESS, payload });
        Alert.alert('Card Read', payload?.cardholderName ?? 'Prepaid card read');
      }),
      DeviceEventEmitter.addListener(BridgeEvent.RECURRING_SALE_SUCCESS, (payload) => {
        console.log('[EVENT]', BridgeEvent.RECURRING_SALE_SUCCESS, payload);
        setIsLoading(false);
        setLastEvent({ name: BridgeEvent.RECURRING_SALE_SUCCESS, payload });
        Alert.alert('Recurring Sale', `Auth: ${payload?.authCode ?? 'N/A'}`);
      }),
      DeviceEventEmitter.addListener(BridgeEvent.ZERO_AUTH_SUCCESS, (payload) => {
        console.log('[EVENT]', BridgeEvent.ZERO_AUTH_SUCCESS, payload);
        setIsLoading(false);
        setLastEvent({ name: BridgeEvent.ZERO_AUTH_SUCCESS, payload });
        Alert.alert('$0 Auth', `Ref: ${payload?.refNo ?? 'N/A'}`);
      }),
      DeviceEventEmitter.addListener(BridgeEvent.CLIENT_VERSION_FETCH_SUCCESS, (payload) => {
        console.log('[EVENT]', BridgeEvent.CLIENT_VERSION_FETCH_SUCCESS, payload);
        setIsLoading(false);
        setLastEvent({ name: BridgeEvent.CLIENT_VERSION_FETCH_SUCCESS, payload });
      }),
      DeviceEventEmitter.addListener(BridgeEvent.MESSAGE_EVENT, (message) => {
        console.log('[EVENT]', BridgeEvent.MESSAGE_EVENT, message);
        setLastEvent({ name: BridgeEvent.MESSAGE_EVENT, payload: message });
      }),
      DeviceEventEmitter.addListener(BridgeEvent.CONFIG_PING_SUCCESS, (payload) => {
        console.log('[EVENT]', BridgeEvent.CONFIG_PING_SUCCESS, payload);
        setLastEvent({ name: BridgeEvent.CONFIG_PING_SUCCESS, payload });
      }),
      DeviceEventEmitter.addListener(BridgeEvent.CONFIG_PING_FAIL, (payload) => {
        console.log('[EVENT]', BridgeEvent.CONFIG_PING_FAIL, payload);
        setLastEvent({ name: BridgeEvent.CONFIG_PING_FAIL, payload });
      }),
      DeviceEventEmitter.addListener(BridgeEvent.CONFIG_ERROR, (payload) => {
        console.log('[EVENT]', BridgeEvent.CONFIG_ERROR, payload);
        setLastEvent({ name: BridgeEvent.CONFIG_ERROR, payload });
      }),
      DeviceEventEmitter.addListener(BridgeEvent.CONFIG_COMPLETED, (payload) => {
        console.log('[EVENT]', BridgeEvent.CONFIG_COMPLETED, payload);
        setLastEvent({ name: BridgeEvent.CONFIG_COMPLETED, payload });
      }),
    ];

    return () => subs.forEach((s) => s.remove());
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
      Alert.alert('Error', `Failed to initialize: ${(error as any)?.message ?? String(error)}`);
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
      Alert.alert('Error', `Sale failed: ${(error as any)?.message ?? String(error)}`);
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
      Alert.alert('Error', `Prepaid card read failed: ${(error as any)?.message ?? String(error)}`);
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
      Alert.alert('Error', `Recurring sale failed: ${(error as any)?.message ?? String(error)}`);
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
      const result = await DsiEMVManager.replaceCreditCard();
      console.log('$0 Auth initiated:', result);
      Alert.alert('Success', '$0 Auth initiated');
    } catch (error) {
      console.error('$0 Auth failed:', error);
      Alert.alert('Error', `$0 Auth failed: ${(error as any)?.message ?? String(error)}`);
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
      Alert.alert('Error', `Cancel failed: ${(error as any)?.message ?? String(error)}`);
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
      Alert.alert('Error', `Config download failed: ${(error as any)?.message ?? String(error)}`);
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
          <View style={styles.loadingContainer} pointerEvents="none">
            <Text style={styles.loadingText}>Processing...</Text>
          </View>
        )}

        {lastEvent && (
          <View style={{ marginTop: 12, alignItems: 'center' }}>
            <Text style={{ fontSize: 12, color: '#666' }}>
              Last Event: {lastEvent.name}
            </Text>
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
