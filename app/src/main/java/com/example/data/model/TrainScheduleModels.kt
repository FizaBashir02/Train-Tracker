package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrainScheduleItem(
    @Json(name = "id") val id: String = "",
    @Json(name = "trainNumber") val trainNumber: String = "",
    @Json(name = "trainName") val trainName: String = "",
    @Json(name = "trainType") val trainType: String = "Express",
    @Json(name = "sourceStation") val sourceStation: String = "",
    @Json(name = "destinationStation") val destinationStation: String = "",
    @Json(name = "departureTime") val departureTime: String = "00:00",
    @Json(name = "arrivalTime") val arrivalTime: String = "00:00",
    @Json(name = "duration") val duration: String = "12h 00m",
    @Json(name = "distance") val distance: Int = 500,
    @Json(name = "status") val status: String = "On Time",
    @Json(name = "platform") val platform: String = "1",
    @Json(name = "fareEconomy") val fareEconomy: Int = 1500,
    @Json(name = "fareBusiness") val fareBusiness: Int = 3500,
    @Json(name = "fareAC") val fareAC: Int = 5500,
    @Json(name = "daysOfOperation") val daysOfOperation: List<String> = listOf("Daily"),
    @Json(name = "intermediateStations") val intermediateStations: List<IntermediateStation> = emptyList(),
    @Json(name = "route") val route: String = "Main Line 1",
    @Json(name = "availableSeats") val availableSeats: Int = 50,
    @Json(name = "lastUpdated") val lastUpdated: String = "Just now"
)

@JsonClass(generateAdapter = true)
data class IntermediateStation(
    @Json(name = "stationCode") val stationCode: String = "",
    @Json(name = "stationName") val stationName: String = "",
    @Json(name = "arrival") val arrival: String = "",
    @Json(name = "departure") val departure: String = "",
    @Json(name = "stopDurationMinutes") val stopDurationMinutes: Int = 0,
    @Json(name = "distanceKm") val distanceKm: Int = 0,
    @Json(name = "platform") val platform: String = "1"
)

@JsonClass(generateAdapter = true)
data class StationItem(
    @Json(name = "code") val code: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "contactNumber") val contactNumber: String = "117",
    @Json(name = "facilities") val facilities: List<String> = emptyList(),
    @Json(name = "latitude") val latitude: Double = 0.0,
    @Json(name = "longitude") val longitude: Double = 0.0,
    @Json(name = "todayArrivals") val todayArrivals: List<StationScheduleEvent> = emptyList(),
    @Json(name = "todayDepartures") val todayDepartures: List<StationScheduleEvent> = emptyList(),
    @Json(name = "nearbyHotels") val nearbyHotels: List<String> = emptyList(),
    @Json(name = "nearbyRestaurants") val nearbyRestaurants: List<String> = emptyList()
) {
    val stationName: String get() = name
    val stationCode: String get() = code
}

@JsonClass(generateAdapter = true)
data class StationScheduleEvent(
    @Json(name = "trainName") val trainName: String = "",
    @Json(name = "trainNumber") val trainNumber: String = "",
    @Json(name = "time") val time: String = ""
)

@JsonClass(generateAdapter = true)
data class RouteItem(
    @Json(name = "routeId") val routeId: String = "",
    @Json(name = "routeName") val routeName: String = "",
    @Json(name = "origin") val origin: String = "",
    @Json(name = "terminus") val terminus: String = "",
    @Json(name = "totalDistanceKm") val totalDistanceKm: Int = 0,
    @Json(name = "stations") val stations: List<String> = emptyList(),
    @Json(name = "trainsCount") val trainsCount: Int = 0
)

data class FilterOptions(
    val query: String = "",
    val source: String = "",
    val destination: String = "",
    val status: String = "All", // "All", "On Time", "Delayed", "Cancelled", "Boarding Soon", "Departed", "Arrived"
    val trainType: String = "All", // "All", "Express", "Passenger", "Freight"
    val sortBy: String = "Departure" // "Departure", "Duration", "Fare"
)
