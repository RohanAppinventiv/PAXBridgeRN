package com.quivio.emvhandheldlib.builder

import android.annotation.SuppressLint
import android.content.Context
import com.datacap.android.dsiEMVAndroid

/**
 * Singleton builder for managing DSI EMV Android instances.
 * 
 * This object provides a centralized way to create and manage DSI EMV Android instances
 * throughout the application lifecycle. It implements the singleton pattern to ensure
 * that only one instance of the DSI EMV Android manager exists at any time, preventing
 * resource conflicts and ensuring consistent state management.
 * 
 * Key Features:
 * - Singleton pattern implementation for instance management
 * - Lazy initialization of the DSI EMV Android instance
 * - Context-aware instance creation
 * - Thread-safe access to the EMV instance
 * 
 * Usage:
 * The builder automatically initializes the DSI EMV Android instance when first accessed
 * through the getInstance() method. Subsequent calls will return the same instance.
 * 
 * Note: This class uses @SuppressLint("StaticFieldLeak") because the DSI EMV Android
 * instance is designed to be a long-lived singleton that outlives individual activities.
 */
internal object DsiEMVInstanceBuilder {
    
    /**
     * The singleton DSI EMV Android instance.
     * 
     * This field holds the single instance of the DSI EMV Android manager.
     * It is marked as @SuppressLint("StaticFieldLeak") because the DSI EMV Android
     * instance is intentionally designed to be a long-lived singleton that persists
     * across the application lifecycle.
     */
    @SuppressLint("StaticFieldLeak")
    private var emvInstance: dsiEMVAndroid? = null

    /**
     * Initializes the DSI EMV Android instance if it doesn't exist.
     * 
     * This private method implements lazy initialization of the DSI EMV Android instance.
     * It creates a new instance only if one doesn't already exist, ensuring the singleton
     * pattern is maintained.
     * 
     * @param context The Android application context required for DSI EMV initialization
     * @return The initialized DSI EMV Android instance, or null if initialization fails
     */
    private fun initialize(context: Context): dsiEMVAndroid? {
        if (emvInstance == null) {
            emvInstance = dsiEMVAndroid(context)
        }
        return emvInstance
    }

    /**
     * Gets the singleton DSI EMV Android instance.
     * 
     * This method provides access to the singleton DSI EMV Android instance. If the
     * instance doesn't exist, it will be automatically initialized with the provided
     * context. This method ensures that only one instance exists throughout the
     * application lifecycle.
     * 
     * @param context The Android application context required for DSI EMV operations
     * @return The singleton DSI EMV Android instance
     * @throws IllegalStateException if the instance cannot be initialized
     */
    fun getInstance(context: Context): dsiEMVAndroid {
        return initialize(context)
            ?: throw IllegalStateException("dsiEMVAndroidManager is not initialized. Call initialize(context) first.")
    }
}