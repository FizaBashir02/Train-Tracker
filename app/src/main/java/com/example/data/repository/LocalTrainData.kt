package com.example.data.repository

import com.example.data.model.*

object LocalTrainData {
    val defaultStations = listOf(
        StationItem("KHI", "Karachi Cantt", "Dr. Daudpota Road, Karachi", "021-99206080", listOf("VIP Lounge", "Air-Conditioned Mosque", "Food Court", "Ticket Counter"), 24.8532, 67.0345),
        StationItem("LHR", "Lahore Junction", "Empress Road, Lahore", "042-99201888", listOf("Executive Lounge", "Grand Mosque", "Subway Outlets", "Cloak Room"), 31.5744, 74.3317),
        StationItem("RWP", "Rawalpindi", "Saddar, Rawalpindi", "051-9270830", listOf("Waiting Hall", "Canteen", "Taxi Stand", "Prayer Area"), 33.5971, 73.0483),
        StationItem("ISB", "Islamabad", "Sector H-9, Islamabad", "051-9257048", listOf("Modern Waiting Lounge", "Cafe", "Prayer Hall"), 33.6702, 73.0234),
        StationItem("FSD", "Faisalabad", "Station Road, Faisalabad", "041-9200388", listOf("Waiting Room", "Food Stalls", "Mosque"), 31.4180, 73.0790),
        StationItem("MUX", "Multan Cantt", "Cantt Area, Multan", "061-9200588", listOf("AC Waiting Lounge", "Food Court", "Mosque"), 30.1852, 71.4391),
        StationItem("HYD", "Hyderabad Junction", "Station Road, Hyderabad", "022-9200188", listOf("Waiting Hall", "Tea Stalls", "Prayer Room"), 25.3924, 68.3737),
        StationItem("SKR", "Sukkur", "Station Road, Sukkur", "071-9310188", listOf("Waiting Area", "Mosque", "Refreshments"), 27.7052, 68.8574),
        StationItem("RRI", "Rohri Junction", "Rohri, Sukkur District", "071-9310200", listOf("Major Junction Lounge", "Food Shops"), 27.6833, 68.8950),
        StationItem("SWL", "Sahiwal", "Railway Road, Sahiwal", "040-9200100", listOf("Waiting Area", "Ticket Window"), 30.6682, 73.1114),
        StationItem("GRW", "Gujranwala", "G.T. Road, Gujranwala", "055-9200150", listOf("Waiting Room", "Food Outlets"), 32.1617, 74.1883),
        StationItem("PEW", "Peshawar Cantt", "Saddar Road, Peshawar", "091-9210188", listOf("Historical Lounge", "Security Desk"), 34.0085, 71.5369),
        StationItem("UET", "Quetta", "Zarghoon Road, Quetta", "081-9201188", listOf("Heated Waiting Area", "Tea Shops"), 30.1980, 67.0125)
    )

    val defaultRoutes = listOf(
        RouteItem("R-ML1", "Main Line 1 (ML-1) Karachi - Peshawar", "Karachi Cantt", "Peshawar Cantt", 1721, listOf("KHI", "HYD", "RRI", "MUX", "SWL", "LHR", "GRW", "RWP", "ISB", "PEW"), 30),
        RouteItem("R-ML2", "Main Line 2 (ML-2) Kotri - Attock", "Kotri Junction", "Attock City Junction", 1250, listOf("HYD", "SKR", "D.G. Khan", "Mianwali", "Attock"), 8),
        RouteItem("R-ML3", "Main Line 3 (ML-3) Rohri - Chaman", "Rohri Junction", "Chaman", 523, listOf("RRI", "Jacobabad", "Sibi", "UET", "Chaman"), 6),
        RouteItem("R-KQR", "Karachi - Mirpur Khas Feeder", "Karachi City", "Khokhrapar", 380, listOf("KHI", "HYD", "Mirpur Khas", "Zero Point"), 4),
        RouteItem("R-FSD", "Lahore - Faisalabad Loop", "Lahore Junction", "Faisalabad", 140, listOf("LHR", "Sheikhupura", "FSD"), 5)
    )

    fun getDummyTrains(): List<TrainScheduleItem> {
        val list = mutableListOf<TrainScheduleItem>()

        // 1. Green Line Express
        list.add(
            TrainScheduleItem(
                id = "1UP", trainNumber = "1UP", trainName = "Green Line Express", trainType = "Express",
                sourceStation = "Karachi Cantt", destinationStation = "Islamabad",
                departureTime = "22:00", arrivalTime = "20:15", duration = "22h 15m", distance = 1522,
                status = "On Time", platform = "2", fareEconomy = 4500, fareBusiness = 8500, fareAC = 11500,
                daysOfOperation = listOf("Daily"), route = "Main Line 1 (ML-1)", availableSeats = 42, lastUpdated = "5 mins ago",
                intermediateStations = listOf(
                    IntermediateStation("KHI", "Karachi Cantt", "22:00", "22:00", 0, 0, "2"),
                    IntermediateStation("HYD", "Hyderabad", "00:30", "00:35", 5, 178, "1"),
                    IntermediateStation("RRI", "Rohri Junction", "05:10", "05:25", 15, 480, "3"),
                    IntermediateStation("MUX", "Multan Cantt", "11:15", "11:35", 20, 925, "2"),
                    IntermediateStation("LHR", "Lahore Junction", "15:45", "16:15", 30, 1214, "1"),
                    IntermediateStation("RWP", "Rawalpindi", "19:40", "19:55", 15, 1504, "2"),
                    IntermediateStation("ISB", "Islamabad", "20:15", "20:15", 0, 1522, "1")
                )
            )
        )

        // 2. Tezgam Express
        list.add(
            TrainScheduleItem(
                id = "7UP", trainNumber = "7UP", trainName = "Tezgam Express", trainType = "Express",
                sourceStation = "Karachi Cantt", destinationStation = "Rawalpindi",
                departureTime = "17:30", arrivalTime = "19:00", duration = "25h 30m", distance = 1548,
                status = "On Time", platform = "1", fareEconomy = 3200, fareBusiness = 6800, fareAC = 9500,
                daysOfOperation = listOf("Daily"), route = "Main Line 1 (ML-1)", availableSeats = 18, lastUpdated = "2 mins ago",
                intermediateStations = listOf(
                    IntermediateStation("KHI", "Karachi Cantt", "17:30", "17:30", 0, 0, "1"),
                    IntermediateStation("HYD", "Hyderabad", "20:00", "20:10", 10, 178, "2"),
                    IntermediateStation("RRI", "Rohri Junction", "01:30", "01:50", 20, 480, "2"),
                    IntermediateStation("MUX", "Multan Cantt", "08:00", "08:20", 20, 925, "1"),
                    IntermediateStation("SWL", "Sahiwal", "11:10", "11:15", 5, 1080, "1"),
                    IntermediateStation("LHR", "Lahore Junction", "13:50", "14:20", 30, 1214, "3"),
                    IntermediateStation("GRW", "Gujranwala", "15:25", "15:30", 5, 1280, "1"),
                    IntermediateStation("RWP", "Rawalpindi", "19:00", "19:00", 0, 1548, "1")
                )
            )
        )

        // 3. Khyber Mail
        list.add(
            TrainScheduleItem(
                id = "2UP", trainNumber = "2UP", trainName = "Khyber Mail", trainType = "Express",
                sourceStation = "Karachi Cantt", destinationStation = "Peshawar Cantt",
                departureTime = "21:15", arrivalTime = "05:15", duration = "32h 00m", distance = 1721,
                status = "Delayed", platform = "3", fareEconomy = 3100, fareBusiness = 6200, fareAC = 9000,
                daysOfOperation = listOf("Daily"), route = "Main Line 1 (ML-1)", availableSeats = 5, lastUpdated = "10 mins ago",
                intermediateStations = listOf(
                    IntermediateStation("KHI", "Karachi Cantt", "21:15", "21:15", 0, 0, "3"),
                    IntermediateStation("HYD", "Hyderabad", "23:45", "23:55", 10, 178, "1"),
                    IntermediateStation("RRI", "Rohri Junction", "05:00", "05:20", 20, 480, "2"),
                    IntermediateStation("MUX", "Multan Cantt", "12:00", "12:20", 20, 925, "2"),
                    IntermediateStation("LHR", "Lahore Junction", "18:00", "18:30", 30, 1214, "2"),
                    IntermediateStation("RWP", "Rawalpindi", "23:30", "23:50", 20, 1504, "1"),
                    IntermediateStation("PEW", "Peshawar Cantt", "05:15", "05:15", 0, 1721, "1")
                )
            )
        )

        // Generate remaining 47+ trains programmatically
        val trainBlueprints = listOf(
            Triple("15UP", "Karachi Express", "Karachi Cantt" to "Lahore Junction"),
            Triple("25UP", "Bahauddin Zakaria Express", "Karachi Cantt" to "Multan Cantt"),
            Triple("39UP", "Jaffar Express", "Quetta" to "Peshawar Cantt"),
            Triple("9UP", "Allama Iqbal Express", "Karachi Cantt" to "Sialkot Junction"),
            Triple("11UP", "Hazara Express", "Karachi City" to "Havelian"),
            Triple("45UP", "Pakistan Express", "Karachi Cantt" to "Rawalpindi"),
            Triple("17UP", "Millat Express", "Karachi Cantt" to "Lala Musa"),
            Triple("13UP", "Awam Express", "Karachi Cantt" to "Peshawar Cantt"),
            Triple("115UP", "Musa Pak Express", "Multan Cantt" to "Lahore Junction"),
            Triple("47UP", "Rehman Baba Express", "Karachi Cantt" to "Peshawar Cantt"),
            Triple("149UP", "Mehran Express", "Karachi City" to "Mirpur Khas"),
            Triple("43UP", "Shah Hussain Express", "Karachi Cantt" to "Lahore Junction"),
            Triple("3UP", "Bolan Mail", "Karachi City" to "Quetta"),
            Triple("131UP", "Rohi Express", "Sukkur" to "Khanewal Junction"),
            Triple("27UP", "Shalimar Express", "Karachi Cantt" to "Lahore Junction"),
            Triple("103UP", "Subak Kharam", "Lahore Junction" to "Rawalpindi"),
            Triple("101UP", "Subak Raftar", "Lahore Junction" to "Rawalpindi"),
            Triple("105UP", "Rawal Express", "Lahore Junction" to "Rawalpindi"),
            Triple("113UP", "Ghauri Express", "Lahore Junction" to "Faisalabad"),
            Triple("111UP", "Badar Express", "Lahore Junction" to "Faisalabad"),
            Triple("107UP", "Islamabad Express", "Lahore Junction" to "Rawalpindi"),
            Triple("41UP", "Karakoram Express", "Karachi Cantt" to "Lahore Junction"),
            Triple("35UP", "Sir Syed Express", "Karachi Cantt" to "Rawalpindi"),
            Triple("119UP", "Shah Rukn-e-Alam Express", "Multan Cantt" to "Lahore Junction"),
            Triple("213UP", "Mohenjo Daro Express", "Kotri Junction" to "Rohri Junction"),
            Triple("145UP", "Sukkur Express", "Karachi City" to "Jacobabad Junction"),
            Triple("401UP", "Thar Express", "Karachi Cantt" to "Zero Point"),
            Triple("303UP", "Chaman Passenger", "Quetta" to "Chaman"),
            Triple("137UP", "Farid Express", "Karachi City" to "Lahore Junction"),
            Triple("211UP", "Kashmore Passenger", "Sukkur" to "Kashmore"),
            Triple("209UP", "Faiz Ahmed Faiz Express", "Lahore Junction" to "Narowal"),
            Triple("215UP", "Lasan Passenger", "Lahore Junction" to "Sialkot"),
            Triple("135UP", "Chenab Express", "Sargodha" to "Lala Musa"),
            Triple("121UP", "Sargodha Express", "Lahore Junction" to "Sargodha"),
            Triple("205UP", "Babu Passenger", "Lahore Junction" to "Wazirabad"),
            Triple("147UP", "Mianwali Express", "Lahore Junction" to "Mari Indus"),
            Triple("109UP", "Dhabeji Express", "Karachi City" to "Dhabeji"),
            Triple("221UP", "Marvi Passenger", "Mirpur Khas" to "Khokhrapar"),
            Triple("125UP", "Sammi Express", "Multan Cantt" to "Rawalpindi"),
            Triple("133UP", "Kohat Express", "Rawalpindi" to "Kohat Cantt"),
            Triple("129UP", "Sandbar Express", "Multan Cantt" to "Lahore Junction"),
            Triple("F-01", "Laser Freight Express", "Karachi Port" to "Lahore Dry Port"),
            Triple("F-02", "Goods Cargo Special", "Karachi Port" to "Rawalpindi Freight Yard"),
            Triple("F-03", "Coal Freight Special", "Port Qasim" to "Sahiwal Power Plant"),
            Triple("F-04", "Oil Tanker Special", "Karachi Refinery" to "Multan Oil Depot"),
            Triple("F-05", "Peshawar Cargo Freight", "Karachi Port" to "Peshawar Dry Port"),
            Triple("F-06", "Quetta Container Freight", "Karachi Port" to "Quetta Dry Port"),
            Triple("F-07", "Cement Cargo Special", "Daud Khel" to "Karachi Port")
        )

        val statuses = listOf("On Time", "On Time", "Delayed", "Boarding Soon", "Departed", "Arrived")

        trainBlueprints.forEachIndexed { idx, bp ->
            val num = bp.first
            val name = bp.second
            val src = bp.third.first
            val dst = bp.third.second
            val isFreight = num.startsWith("F-")
            val isPassenger = name.contains("Passenger")
            val type = when {
                isFreight -> "Freight"
                isPassenger -> "Passenger"
                else -> "Express"
            }
            val dist = 200 + (idx * 30) % 1300
            val status = statuses[idx % statuses.size]
            val depHour = (5 + (idx * 3) % 18).toString().padStart(2, '0')

            list.add(
                TrainScheduleItem(
                    id = num,
                    trainNumber = num,
                    trainName = name,
                    trainType = type,
                    sourceStation = src,
                    destinationStation = dst,
                    departureTime = "$depHour:30",
                    arrivalTime = "${((depHour.toIntOrNull() ?: 8 + 10) % 24).toString().padStart(2, '0')}:45",
                    duration = "${8 + (idx % 16)}h 15m",
                    distance = dist,
                    status = status,
                    platform = ((idx % 4) + 1).toString(),
                    fareEconomy = if (isFreight) 0 else (dist * 2.5).toInt(),
                    fareBusiness = if (isFreight) 0 else (dist * 5.2).toInt(),
                    fareAC = if (isFreight) 0 else (dist * 7.5).toInt(),
                    daysOfOperation = listOf("Daily"),
                    route = "Main Line",
                    availableSeats = if (isFreight) 0 else (15 + (idx * 7) % 50),
                    lastUpdated = "1 min ago",
                    intermediateStations = listOf(
                        IntermediateStation("SRC", src, "$depHour:30", "$depHour:30", 0, 0, "1"),
                        IntermediateStation("MID", "Midway Junction", "${((depHour.toIntOrNull() ?: 8) + 4) % 24}:15", "${((depHour.toIntOrNull() ?: 8) + 4) % 24}:25", 10, dist / 2, "2"),
                        IntermediateStation("DST", dst, "${(((depHour.toIntOrNull() ?: 8) + 10) % 24).toString().padStart(2, '0')}:45", "${(((depHour.toIntOrNull() ?: 8) + 10) % 24).toString().padStart(2, '0')}:45", 0, dist, "1")
                    )
                )
            )
        }

        return list
    }
}
