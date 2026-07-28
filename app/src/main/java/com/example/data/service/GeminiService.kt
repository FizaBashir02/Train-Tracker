package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// --- Models for UI ---
data class TrainSearchItem(
    val trainName: String,
    val trainNumber: String,
    val source: String,
    val destination: String,
    val departure: String,
    val arrival: String,
    val duration: String,
    val trainType: String // "Express", "Passenger", "Freight"
)

data class LiveStatus(
    val trainName: String,
    val trainNumber: String,
    val currentStation: String,
    val previousStation: String,
    val nextStation: String,
    val eta: String,
    val etd: String,
    val delayMinutes: Int,
    val journeyProgress: Float, // 0.0f to 1.0f
    val distanceRemainingKm: Int,
    val runningDays: String,
    val lastUpdated: String
)

data class ScheduleStation(
    val stationName: String,
    val stationCode: String,
    val arrival: String,
    val departure: String,
    val distanceKm: Int,
    val stopDurationMinutes: Int,
    val dayNumber: Int
)

data class TrainSchedule(
    val trainName: String,
    val trainNumber: String,
    val stations: List<ScheduleStation>,
    val totalStops: Int,
    val totalDistanceKm: Int,
    val totalJourneyTime: String
)

data class StationInfo(
    val stationName: String,
    val code: String,
    val address: String,
    val contactNumber: String,
    val facilities: List<String>, // ["Waiting Area", "Prayer Area", "Parking", "Washrooms", "Food Stalls", "Ticket Counter"]
    val nearbyHotels: List<String>,
    val nearbyRestaurants: List<String>,
    val nearbyBusStops: List<String>,
    val todayArrivals: List<StationTimeItem>,
    val todayDepartures: List<StationTimeItem>,
    val delayedTrains: List<DelayedTrainItem>
)

data class StationTimeItem(
    val trainName: String,
    val trainNumber: String,
    val time: String,
    val status: String
)

data class DelayedTrainItem(
    val trainName: String,
    val trainNumber: String,
    val originalTime: String,
    val delayMinutes: Int,
    val status: String
)

data class FreightTrainItem(
    val trainName: String,
    val trainNumber: String,
    val route: String,
    val status: String,
    val eta: String,
    val cargoType: String,
    val currentPosition: String
)

data class WeatherData(
    val location: String,
    val temperature: String,
    val humidity: String,
    val condition: String // "Sunny", "Rainy", "Cloudy", "Foggy"
)

data class NamazTimingsData(
    val islamicDate: String,
    val fajr: String,
    val sunrise: String = "05:30 AM",
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val qiblaDirection: String
)

data class NewsItem(
    val title: String,
    val category: String, // "Announcements", "Delays", "Maintenance", "New Trains"
    val date: String,
    val summary: String
)

data class BlogItem(
    val title: String,
    val category: String, // "Travel Tips", "Tourism", "Railway History", "Safety"
    val readTime: String,
    val content: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Base REST call to Gemini using JSON API
    private suspend fun callGeminiJson(prompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to simulated database data.")
            return@withContext null
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey"

        val systemInstruction = """
            You are the backend assistant for Pakistan Railways Train Schedule app.
            You must ALWAYS return responses as raw, valid, and well-formed JSON matches the requested schema EXACTLY.
            Do not enclose the JSON in ```json blocks or markdown formatting. Output raw JSON ONLY.
            Use realistic values for Pakistan Railways stations, trains, timings, Namaz, and news.
        """.trimIndent()

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", prompt)
                }))
            }))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", systemInstruction)
                }))
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API call failed with code: ${response.code} - ${response.message}")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                text?.trim()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in callGeminiJson", e)
            null
        }
    }

    // --- Search Trains ---
    suspend fun searchTrains(source: String, dest: String, type: String): List<TrainSearchItem> {
        val cleanSource = source.trim()
        val cleanDest = dest.trim()
        val prompt = """
            Generate a list of 4-6 realistic trains running between '$cleanSource' and '$cleanDest' for Pakistan Railways.
            If either '$cleanSource' or '$cleanDest' is empty, return a general search list of famous Pakistan Railways trains (e.g., Green Line, Karakoram Express, Tezgam, Khyber Mail, Pak Business).
            The 'trainType' parameter filter is '$type'. It can be 'Express', 'Passenger', 'Freight' or 'All'. Filter the results accordingly.
            
            Return a JSON array of objects with this schema:
            [
              {
                "trainName": "Karakoram Express",
                "trainNumber": "42DN",
                "source": "Karachi Cantt",
                "destination": "Lahore Junction",
                "departure": "15:30",
                "arrival": "08:45",
                "duration": "17h 15m",
                "trainType": "Express"
              }
            ]
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<TrainSearchItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TrainSearchItem(
                            trainName = obj.optString("trainName", "Train"),
                            trainNumber = obj.optString("trainNumber", "00"),
                            source = obj.optString("source", cleanSource.ifEmpty { "Lahore" }),
                            destination = obj.optString("destination", cleanDest.ifEmpty { "Karachi" }),
                            departure = obj.optString("departure", "00:00"),
                            arrival = obj.optString("arrival", "00:00"),
                            duration = obj.optString("duration", "0h"),
                            trainType = obj.optString("trainType", "Express")
                        )
                    )
                }
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse searchTrains JSON", e)
            }
        }
        return getFallbackTrains(cleanSource, cleanDest, type)
    }

    private fun getFallbackTrains(source: String, dest: String, type: String): List<TrainSearchItem> {
        val s = source.ifEmpty { "Lahore Junction" }
        val d = dest.ifEmpty { "Karachi Cantt" }
        val all = listOf(
            TrainSearchItem("Karakoram Express", "42DN", "Karachi Cantt", "Lahore Junction", "15:30", "08:45", "17h 15m", "Express"),
            TrainSearchItem("Green Line", "5UP", "Karachi Cantt", "Margalla (Islamabad)", "22:00", "20:15", "22h 15m", "Express"),
            TrainSearchItem("Tezgam", "7UP", "Karachi Cantt", "Rawalpindi", "17:30", "19:00", "25h 30m", "Express"),
            TrainSearchItem("Khyber Mail", "1UP", "Karachi Cantt", "Peshawar Cantt", "22:15", "06:00", "31h 45m", "Express"),
            TrainSearchItem("Shalimar Express", "27UP", "Karachi Cantt", "Lahore Junction", "06:00", "22:30", "16h 30m", "Express"),
            TrainSearchItem("Pak Business Express", "33UP", "Karachi Cantt", "Lahore Junction", "16:00", "09:30", "17h 30m", "Express"),
            TrainSearchItem("Rawalpindi Express", "109UP", "Lahore Junction", "Rawalpindi", "06:00", "10:30", "4h 30m", "Express"),
            TrainSearchItem("Bolan Mail", "3UP", "Karachi City", "Quetta", "18:00", "19:40", "25h 40m", "Passenger"),
            TrainSearchItem("Lahore Passenger", "211UP", "Lahore Junction", "Sialkot", "08:00", "11:15", "3h 15m", "Passenger"),
            TrainSearchItem("Coal Special", "F-901", "Port Qasim", "Sahiwal Power Plant", "02:00", "21:30", "19h 30m", "Freight")
        )
        return all.filter {
            val matchesType = type == "All" || it.trainType.equals(type, ignoreCase = true)
            val matchesRoute = if (source.isNotEmpty() && dest.isNotEmpty()) {
                it.source.contains(source, true) || it.destination.contains(dest, true)
            } else true
            matchesType && matchesRoute
        }.ifEmpty {
            all.filter { type == "All" || it.trainType.equals(type, ignoreCase = true) }
        }
    }

    // --- Live Train Status ---
    suspend fun getLiveStatus(trainNumber: String): LiveStatus {
        val prompt = """
            Provide a realistic live running status for Pakistan Railways train number '$trainNumber'.
            Generate detailed delay statistics, current progress, distance remaining, running days, and previous/next station.
            
            Return a JSON object with this schema:
            {
              "trainName": "Karakoram Express",
              "trainNumber": "$trainNumber",
              "currentStation": "Sahiwal",
              "previousStation": "Khanewal Junction",
              "nextStation": "Okara",
              "eta": "22:45",
              "etd": "22:50",
              "delayMinutes": 25,
              "journeyProgress": 0.72,
              "distanceRemainingKm": 210,
              "runningDays": "Daily",
              "lastUpdated": "2 mins ago"
            }
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val obj = JSONObject(jsonStr)
                return LiveStatus(
                    trainName = obj.optString("trainName", "Express Train"),
                    trainNumber = obj.optString("trainNumber", trainNumber),
                    currentStation = obj.optString("currentStation", "Jhelum"),
                    previousStation = obj.optString("previousStation", "Gujrat"),
                    nextStation = obj.optString("nextStation", "Rawalpindi"),
                    eta = obj.optString("eta", "12:30"),
                    etd = obj.optString("etd", "12:35"),
                    delayMinutes = obj.optInt("delayMinutes", 15),
                    journeyProgress = obj.optDouble("journeyProgress", 0.6).toFloat(),
                    distanceRemainingKm = obj.optInt("distanceRemainingKm", 120),
                    runningDays = obj.optString("runningDays", "Daily"),
                    lastUpdated = obj.optString("lastUpdated", "Just now")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse live status JSON", e)
            }
        }
        return getFallbackLiveStatus(trainNumber)
    }

    private fun getFallbackLiveStatus(trainNumber: String): LiveStatus {
        val name = when (trainNumber.uppercase()) {
            "42DN", "42" -> "Karakoram Express"
            "5UP", "5" -> "Green Line"
            "7UP", "7" -> "Tezgam"
            "1UP", "1" -> "Khyber Mail"
            "27UP", "27" -> "Shalimar Express"
            "33UP", "33" -> "Pak Business Express"
            "F-901" -> "Coal Special"
            else -> "Tezgam"
        }
        return LiveStatus(
            trainName = name,
            trainNumber = trainNumber,
            currentStation = "Rohri Junction",
            previousStation = "Khairpur",
            nextStation = "Bahawalpur",
            eta = "14:15",
            etd = "14:30",
            delayMinutes = 40,
            journeyProgress = 0.45f,
            distanceRemainingKm = 480,
            runningDays = "Daily",
            lastUpdated = "5 mins ago"
        )
    }

    // --- Train Schedule ---
    suspend fun getTrainSchedule(trainNumber: String): TrainSchedule {
        val prompt = """
            Generate the complete station-by-station schedule for Pakistan Railways train number '$trainNumber'.
            List 5-8 intermediate major stations on the route, with proper codes, distance, and stop durations.
            
            Return a JSON object with this schema:
            {
              "trainName": "Karakoram Express",
              "trainNumber": "$trainNumber",
              "totalStops": 8,
              "totalDistanceKm": 1210,
              "totalJourneyTime": "17h 15m",
              "stations": [
                {
                  "stationName": "Karachi Cantt",
                  "stationCode": "KC",
                  "arrival": "15:30",
                  "departure": "15:30",
                  "distanceKm": 0,
                  "stopDurationMinutes": 0,
                  "dayNumber": 1
                },
                {
                  "stationName": "Hyderabad Junction",
                  "stationCode": "HDR",
                  "arrival": "17:45",
                  "departure": "17:50",
                  "distanceKm": 180,
                  "stopDurationMinutes": 5,
                  "dayNumber": 1
                }
              ]
            }
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val obj = JSONObject(jsonStr)
                val stationsArray = obj.getJSONArray("stations")
                val stationsList = mutableListOf<ScheduleStation>()
                for (i in 0 until stationsArray.length()) {
                    val st = stationsArray.getJSONObject(i)
                    stationsList.add(
                        ScheduleStation(
                            stationName = st.optString("stationName", "Station"),
                            stationCode = st.optString("stationCode", "ST"),
                            arrival = st.optString("arrival", "00:00"),
                            departure = st.optString("departure", "00:00"),
                            distanceKm = st.optInt("distanceKm", 0),
                            stopDurationMinutes = st.optInt("stopDurationMinutes", 0),
                            dayNumber = st.optInt("dayNumber", 1)
                        )
                    )
                }
                return TrainSchedule(
                    trainName = obj.optString("trainName", "Express Train"),
                    trainNumber = obj.optString("trainNumber", trainNumber),
                    stations = stationsList,
                    totalStops = obj.optInt("totalStops", stationsList.size),
                    totalDistanceKm = obj.optInt("totalDistanceKm", 1000),
                    totalJourneyTime = obj.optString("totalJourneyTime", "15h")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse schedule JSON", e)
            }
        }
        return getFallbackSchedule(trainNumber)
    }

    private fun getFallbackSchedule(trainNumber: String): TrainSchedule {
        val name = when (trainNumber.uppercase()) {
            "42DN", "42" -> "Karakoram Express"
            "5UP", "5" -> "Green Line"
            "7UP", "7" -> "Tezgam"
            else -> "Karakoram Express"
        }
        val stations = listOf(
            ScheduleStation("Karachi Cantt", "KC", "15:30", "15:30", 0, 0, 1),
            ScheduleStation("Hyderabad Jn", "HYD", "17:45", "17:55", 180, 10, 1),
            ScheduleStation("Rohri Jn", "ROH", "21:30", "21:55", 480, 25, 1),
            ScheduleStation("Bahawalpur", "BWP", "02:40", "02:45", 780, 5, 2),
            ScheduleStation("Multan Cantt", "MUX", "04:10", "04:30", 870, 20, 2),
            ScheduleStation("Sahiwal", "SWAL", "06:40", "06:45", 1020, 5, 2),
            ScheduleStation("Lahore Junction", "LHR", "08:45", "08:45", 1210, 0, 2)
        )
        return TrainSchedule(name, trainNumber, stations, stations.size, 1210, "17h 15m")
    }

    // --- Station Information ---
    suspend fun getStationInfo(stationCode: String): StationInfo {
        val prompt = """
            Provide details about the Pakistan Railways station with code '$stationCode'.
            Include address, contact number, station facilities (Prayer Area, Waiting Room, Ticket Counter, washrooms, dining, etc), nearby amenities (hotels, restaurants, bus stops), today's Arrivals/Departures, and currently delayed trains.
            
            Return a JSON object with this schema:
            {
              "stationName": "Lahore Junction",
              "code": "$stationCode",
              "address": "Allama Iqbal Road, Garhi Shahu, Lahore",
              "contactNumber": "+92-42-99201625",
              "facilities": ["Waiting Area", "Prayer Area", "Parking", "Washrooms", "Food Stalls", "Ticket Counter"],
              "nearbyHotels": ["Luxus Grand Hotel", "Pearl Continental", "Royal Fort Hotel"],
              "nearbyRestaurants": ["Butt Karahi", "Haveli Restaurant", "Gourmet Bakers"],
              "nearbyBusStops": ["Lari Adda Bus Station", "Metro Bus Station", "Daewoo Express"],
              "todayArrivals": [
                { "trainName": "Green Line", "trainNumber": "5UP", "time": "20:15", "status": "On Time" }
              ],
              "todayDepartures": [
                { "trainName": "Tezgam", "trainNumber": "7UP", "time": "17:30", "status": "Delayed" }
              ],
              "delayedTrains": [
                { "trainName": "Khyber Mail", "trainNumber": "1UP", "originalTime": "22:15", "delayMinutes": 45, "status": "Delayed" }
              ]
            }
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val obj = JSONObject(jsonStr)
                
                val arrArray = obj.getJSONArray("todayArrivals")
                val depArray = obj.getJSONArray("todayDepartures")
                val delArray = obj.getJSONArray("delayedTrains")
                
                val arrivals = mutableListOf<StationTimeItem>()
                for (i in 0 until arrArray.length()) {
                    val o = arrArray.getJSONObject(i)
                    arrivals.add(StationTimeItem(o.getString("trainName"), o.getString("trainNumber"), o.getString("time"), o.getString("status")))
                }
                
                val departures = mutableListOf<StationTimeItem>()
                for (i in 0 until depArray.length()) {
                    val o = depArray.getJSONObject(i)
                    departures.add(StationTimeItem(o.getString("trainName"), o.getString("trainNumber"), o.getString("time"), o.getString("status")))
                }
                
                val delayed = mutableListOf<DelayedTrainItem>()
                for (i in 0 until delArray.length()) {
                    val o = delArray.getJSONObject(i)
                    delayed.add(DelayedTrainItem(o.getString("trainName"), o.getString("trainNumber"), o.getString("originalTime"), o.getInt("delayMinutes"), o.getString("status")))
                }

                return StationInfo(
                    stationName = obj.optString("stationName", "Railway Station"),
                    code = obj.optString("code", stationCode),
                    address = obj.optString("address", "Pakistan"),
                    contactNumber = obj.optString("contactNumber", "+92-111-PR-HELP"),
                    facilities = jsonArrayToList(obj.optJSONArray("facilities")),
                    nearbyHotels = jsonArrayToList(obj.optJSONArray("nearbyHotels")),
                    nearbyRestaurants = jsonArrayToList(obj.optJSONArray("nearbyRestaurants")),
                    nearbyBusStops = jsonArrayToList(obj.optJSONArray("nearbyBusStops")),
                    todayArrivals = arrivals,
                    todayDepartures = departures,
                    delayedTrains = delayed
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse stationInfo JSON", e)
            }
        }
        return getFallbackStationInfo(stationCode)
    }

    private fun jsonArrayToList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    private fun getFallbackStationInfo(stationCode: String): StationInfo {
        val name = when (stationCode.uppercase()) {
            "LHR" -> "Lahore Junction"
            "KC", "KCT" -> "Karachi Cantt"
            "RWP" -> "Rawalpindi Station"
            "MUX" -> "Multan Cantt"
            "PEW" -> "Peshawar Cantt"
            else -> "Lahore Junction"
        }
        return StationInfo(
            stationName = name,
            code = stationCode,
            address = "Station Road, Pakistan",
            contactNumber = "+92-42-111-772-457",
            facilities = listOf("Waiting Area", "Prayer Area", "Parking", "Washrooms", "Food Stalls", "Ticket Counter"),
            nearbyHotels = listOf("Railway Retiring Rooms", "Local Inn Hotel", "City Heights Hotel"),
            nearbyRestaurants = listOf("Railway Canteen", "Al-Habib Restaurant", "Gourmet Refreshments"),
            nearbyBusStops = listOf("City Daewoo Terminal", "Local Rickshaw Stand"),
            todayArrivals = listOf(
                StationTimeItem("Green Line", "5UP", "20:15", "On Time"),
                StationTimeItem("Karakoram Express", "42DN", "08:45", "On Time")
            ),
            todayDepartures = listOf(
                StationTimeItem("Tezgam", "7UP", "17:30", "Delayed"),
                StationTimeItem("Khyber Mail", "1UP", "22:15", "On Time")
            ),
            delayedTrains = listOf(
                DelayedTrainItem("Tezgam", "7UP", "17:30", 35, "Delayed")
            )
        )
    }

    // --- Freight Trains ---
    suspend fun getFreightTrains(): List<FreightTrainItem> {
        val prompt = """
            Provide a realistic list of 3-4 active Freight (Cargo) trains currently running on the Pakistan Railways network.
            Include cargo type (e.g., Coal, Oil, Containers, Wheat), routes, current status, and ETA.
            
            Return a JSON array of objects with this schema:
            [
              {
                "trainName": "Coal Cargo Special",
                "trainNumber": "F-801",
                "route": "Port Qasim (Karachi) to Sahiwal Power Plant",
                "status": "In Transit",
                "eta": "19:30",
                "cargoType": "Coal",
                "currentPosition": "Multan Junction"
              }
            ]
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<FreightTrainItem>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        FreightTrainItem(
                            trainName = o.getString("trainName"),
                            trainNumber = o.getString("trainNumber"),
                            route = o.getString("route"),
                            status = o.getString("status"),
                            eta = o.getString("eta"),
                            cargoType = o.getString("cargoType"),
                            currentPosition = o.getString("currentPosition")
                        )
                    )
                }
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse freight trains JSON", e)
            }
        }
        return listOf(
            FreightTrainItem("Coal Special", "F-901", "Port Qasim to Sahiwal", "In Transit", "21:30", "Coal", "Khanewal Jn"),
            FreightTrainItem("Oil Tanker Express", "F-32", "Keamari (Karachi) to Machike (Sheikhupura)", "Delayed", "04:15", "Petroleum Oil", "Rohri Jn"),
            FreightTrainItem("Container Carrier", "F-102", "Karachi Port to Lahore Dry Port", "In Transit", "08:00", "Shipping Containers", "Sadiqabad")
        )
    }

    // --- Weather ---
    suspend fun getWeather(location: String): WeatherData {
        val prompt = """
            Provide real-time weather information for '$location' in Pakistan.
            
            Return a JSON object with this schema:
            {
              "location": "$location",
              "temperature": "32°C",
              "humidity": "65%",
              "condition": "Cloudy"
            }
            Supported condition strings: "Sunny", "Rainy", "Cloudy", "Foggy"
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val o = JSONObject(jsonStr)
                return WeatherData(
                    location = o.optString("location", location),
                    temperature = o.optString("temperature", "30°C"),
                    humidity = o.optString("humidity", "55%"),
                    condition = o.optString("condition", "Sunny")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse weather JSON", e)
            }
        }
        return WeatherData(location.ifEmpty { "Lahore" }, "34°C", "58%", "Sunny")
    }

    // --- Namaz Timings ---
    suspend fun getNamazTimings(location: String): NamazTimingsData {
        val prompt = """
            Calculate or fetch today's Namaz (Islamic Prayer) timings and Islamic Hijri Date for '$location', Pakistan.
            Include Qibla direction angle from North.
            
            Return a JSON object with this schema:
            {
              "islamicDate": "25 Muharram 1448 AH",
              "fajr": "04:15 AM",
              "dhuhr": "12:15 PM",
              "asr": "04:45 PM",
              "maghrib": "07:15 PM",
              "isha": "08:45 PM",
              "qiblaDirection": "262° West of North"
            }
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val o = JSONObject(jsonStr)
                return NamazTimingsData(
                    islamicDate = o.optString("islamicDate", "25 Muharram 1448 AH"),
                    fajr = o.optString("fajr", "04:12 AM"),
                    sunrise = o.optString("sunrise", "05:32 AM"),
                    dhuhr = o.optString("dhuhr", "12:18 PM"),
                    asr = o.optString("asr", "04:55 PM"),
                    maghrib = o.optString("maghrib", "07:12 PM"),
                    isha = o.optString("isha", "08:44 PM"),
                    qiblaDirection = o.optString("qiblaDirection", "262° (West-South-West)")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Namaz JSON", e)
            }
        }
        return NamazTimingsData("25 Muharram 1448 AH", "04:10 AM", "05:30 AM", "12:15 PM", "04:50 PM", "07:10 PM", "08:40 PM", "262° W")
    }

    // --- Railway News & Blogs ---
    suspend fun getNews(): List<NewsItem> {
        val prompt = """
            Generate 4 realistic news bulletins for Pakistan Railways. Use categories: "Announcements", "Delays", "Maintenance", "New Trains".
            Include titles, summaries, and dates.
            
            Return a JSON array of objects with this schema:
            [
              {
                "title": "Pakistan Railways introduces 50 new high-speed passenger coaches",
                "category": "New Trains",
                "date": "Today",
                "summary": "PR has successfully integrated 50 state-of-the-art Chinese manufactured coaches into its primary lines to enhance passenger comfort and speed."
              }
            ]
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<NewsItem>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        NewsItem(
                            title = o.getString("title"),
                            category = o.getString("category"),
                            date = o.getString("date"),
                            summary = o.getString("summary")
                        )
                    )
                }
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse news JSON", e)
            }
        }
        return listOf(
            NewsItem("New Coaches Added to Green Line", "New Trains", "Today", "Pakistan Railways has added modern, comfortable Chinese passenger coaches to the premium Green Line Express starting this Friday."),
            NewsItem("Railway Line Maintenance near Jhelum to Cause Brief Delays", "Maintenance", "Yesterday", "Due to routine annual safety upgrades on the main up-line near Jhelum, trains departing Lahore may experience 15-20 min delays."),
            NewsItem("PR Helpline 117 Upgraded with 24/7 Live Status Support", "Announcements", "2 days ago", "The official railway helpline 117 has been modernized with additional live operators and instant AI status inquiry systems.")
        )
    }

    suspend fun getBlogs(): List<BlogItem> {
        val prompt = """
            Generate 3 realistic and engaging travel/railway blogs for Pakistan Railways app.
            Categories: "Travel Tips", "Tourism", "Railway History", "Safety".
            
            Return a JSON array of objects with this schema:
            [
              {
                "title": "Exploring Bolan Pass: Pakistan's Most Scenic Train Journey",
                "category": "Tourism",
                "readTime": "4 mins read",
                "content": "Full detailed story about the Bolan Pass historic tunnels and mountains."
              }
            ]
        """.trimIndent()

        val jsonStr = callGeminiJson(prompt)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<BlogItem>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        BlogItem(
                            title = o.getString("title"),
                            category = o.getString("category"),
                            readTime = o.getString("readTime"),
                            content = o.getString("content")
                        )
                    )
                }
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse blogs JSON", e)
            }
        }
        return listOf(
            BlogItem("Scenic Tunnels of Khewra & Salt Range", "Tourism", "5 mins read", "Traveling through the Salt Range by train reveals gorgeous vistas and deep historical tunnels carved more than a century ago..."),
            BlogItem("Top 5 Tips for Night Travel on Karakoram Express", "Travel Tips", "3 mins read", "To ensure a cozy night journey from Karachi to Lahore, always book AC Sleeper classes, carry noise-cancelling earplugs, and keep light snacks..."),
            BlogItem("A Glimpse into 160 Years of Railway History", "Railway History", "6 mins read", "Pakistan Railways traces its roots back to the 1861 Scinde Railway line. Since then, it has acted as the economic and cultural lifeline of Pakistan...")
        )
    }
}
