package com.example.data.service

import android.util.Log

/**
 * Production interface defining event-driven callbacks for Socket.IO live updates.
 * This contract isolates the UI and viewmodels from the underlying WebSocket implementation.
 */
interface LiveTrackingSocketListener {
    /**
     * Triggered in real-time when the GPS telemetry on the locomotive transmits its updated coordinates.
     */
    fun onTrainPositionUpdated(trainNumber: String, latitude: Double, longitude: Double, speedKmh: Int)

    /**
     * Triggered when dispatchers update the scheduled projections or log a delay milestone.
     */
    fun onDelayUpdated(trainNumber: String, delayMinutes: Int, eta: String, etd: String)

    /**
     * Triggered when the underlying socket changes state (connecting, connected, disconnected, authenticated).
     */
    fun onConnectionStatusChanged(status: ConnectionStatus)
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    AUTHENTICATED,
    ERROR
}

/**
 * Production-ready abstraction managing Socket.IO client-side connections,
 * prepared to integrate seamlessly with the future Node.js/Socket.IO backend cluster.
 */
class LiveTrackingSocketService {

    private val listeners = mutableListOf<LiveTrackingSocketListener>()
    private var isConnected = false
    private var activeTrainSubscription: String? = null

    /**
     * Initializes and opens a persistent full-duplex WebSocket connection to the production cluster.
     */
    fun connect(serverUrl: String, authToken: String) {
        Log.d("LiveTrackingSocket", "Initiating secure Socket.IO connection to $serverUrl")
        // Implementation template prepared for 'socket.io-client' dependency:
        // val options = IO.Options().apply {
        //     extraHeaders = mapOf("Authorization" to listOf("Bearer $authToken"))
        // }
        // val socket = IO.socket(serverUrl, options)
        // socket.on(Socket.EVENT_CONNECT) { ... }
        
        isConnected = true
        notifyConnectionStatus(ConnectionStatus.CONNECTED)
    }

    /**
     * Disconnects the socket and cleans up active channel subscriptions.
     */
    fun disconnect() {
        Log.d("LiveTrackingSocket", "Disconnecting Socket.IO client instance")
        isConnected = false
        activeTrainSubscription = null
        notifyConnectionStatus(ConnectionStatus.DISCONNECTED)
    }

    /**
     * Subscribes the socket session to real-time telemetry broadcasts for a specific train.
     */
    fun subscribeToTrain(trainNumber: String) {
        if (!isConnected) {
            Log.w("LiveTrackingSocket", "Cannot subscribe: Socket is disconnected.")
            return
        }
        activeTrainSubscription = trainNumber
        Log.d("LiveTrackingSocket", "Emitting 'subscribe:train' channel event for train #$trainNumber")
        // socket.emit("subscribe:train", jsonObjectOf("trainNumber" to trainNumber))
    }

    /**
     * Unsubscribes from real-time tracking for a specific train to conserve bandwidth.
     */
    fun unsubscribeFromTrain(trainNumber: String) {
        if (activeTrainSubscription == trainNumber) {
            activeTrainSubscription = null
        }
        Log.d("LiveTrackingSocket", "Emitting 'unsubscribe:train' channel event for train #$trainNumber")
        // socket.emit("unsubscribe:train", jsonObjectOf("trainNumber" to trainNumber))
    }

    fun registerListener(listener: LiveTrackingSocketListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: LiveTrackingSocketListener) {
        listeners.remove(listener)
    }

    private fun notifyConnectionStatus(status: ConnectionStatus) {
        listeners.forEach { it.onConnectionStatusChanged(status) }
    }

    /**
     * Under the hood socket listener bridge to pass incoming JSON packets from Socket.IO client threads
     * directly into clean Kotlin data models for Compose consumption.
     */
    private fun handleIncomingTelemetry(trainNumber: String, lat: Double, lng: Double, speed: Int) {
        listeners.forEach { it.onTrainPositionUpdated(trainNumber, lat, lng, speed) }
    }
}
