package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class CalculationMethod(val id: Int, val displayName: String, val fajrAngle: Double, val ishaAngle: Double) {
    KARACHI(1, "University of Islamic Sciences, Karachi", 18.0, 18.0),
    ISNA(2, "Islamic Society of North America (ISNA)", 15.0, 15.0),
    MWL(3, "Muslim World League (MWL)", 18.0, 17.5),
    MAKKAH(4, "Umm Al-Qura University, Makkah", 18.5, 0.0), // 0 means 90 mins after Maghrib
    EGYPT(5, "Egyptian General Authority of Survey", 19.5, 17.5)
}

enum class AsrSchool(val id: Int, val displayName: String, val shadowFactor: Int) {
    STANDARD(0, "Standard (Shafi, Maliki, Hanbali)", 1),
    HANAFI(1, "Hanafi", 2)
}

enum class TimeFormat(val displayName: String) {
    FORMAT_12H("12-Hour (AM/PM)"),
    FORMAT_24H("24-Hour (24h)")
}

data class PrayerTimingSettings(
    val method: CalculationMethod = CalculationMethod.KARACHI,
    val school: AsrSchool = AsrSchool.HANAFI,
    val timeFormat: TimeFormat = TimeFormat.FORMAT_12H,
    val notificationsEnabled: Boolean = true
)

data class PrayerTimeItem(
    val name: String,
    val timeFormatted: String,
    val timeMillis: Long,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

data class PrayerTimesData(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val gregorianDate: String,
    val hijriDate: String,
    val qiblaDirection: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val currentPrayerName: String,
    val nextPrayerName: String,
    val nextPrayerFormattedTime: String,
    val countdownSeconds: Long,
    val items: List<PrayerTimeItem>
)

data class CityCoordinates(val city: String, val lat: Double, val lng: Double, val timeZone: String = "Asia/Karachi")

object PrayerTimeCalculator {

    val defaultCities = listOf(
        CityCoordinates("Islamabad", 33.6844, 73.0479),
        CityCoordinates("Rawalpindi", 33.5651, 73.0169),
        CityCoordinates("Lahore", 31.5204, 74.3587),
        CityCoordinates("Karachi", 24.8607, 67.0011),
        CityCoordinates("Peshawar", 34.0151, 71.5249),
        CityCoordinates("Quetta", 30.1798, 66.9750),
        CityCoordinates("Faisalabad", 31.4504, 73.1350),
        CityCoordinates("Multan", 30.1575, 71.5249),
        CityCoordinates("Sargodha", 32.0836, 72.6711),
        CityCoordinates("Gujranwala", 32.1617, 74.1883),
        CityCoordinates("Hyderabad", 25.3960, 68.3578),
        CityCoordinates("Bahawalpur", 29.3956, 71.6836),
        CityCoordinates("Sialkot", 32.4945, 74.5229),
        CityCoordinates("Abbottabad", 34.1688, 73.2215),
        CityCoordinates("Sukkur", 27.7244, 68.8228)
    )

    fun getCoordinatesForCity(cityName: String): CityCoordinates {
        return defaultCities.find { it.city.equals(cityName, ignoreCase = true) }
            ?: CityCoordinates(cityName, 31.5204, 74.3587)
    }

    // Mathematical Calculation of Astronomical Prayer Times
    fun calculatePrayerTimes(
        locationName: String,
        lat: Double,
        lng: Double,
        date: Date = Date(),
        settings: PrayerTimingSettings = PrayerTimingSettings()
    ): PrayerTimesData {
        val calendar = Calendar.getInstance().apply { time = date }
        val tzOffset = TimeZone.getDefault().getOffset(calendar.timeInMillis) / 3600000.0

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Day
        val julianDay = julianDate(year, month, day) - lng / (15.0 * 24.0)
        val d = julianDay - 2451545.0

        // Sun parameters
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(d2r(g)) + 0.020 * sin(d2r(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = r2d(atan2(cos(d2r(e)) * sin(d2r(l)), cos(d2r(l)))) / 15.0
        val raFixed = fixHour(ra)

        val decl = r2d(asin(sin(d2r(e)) * sin(d2r(l))))
        val eqt = q / 15.0 - raFixed

        // Dhuhr (Noon)
        val dhuhrBase = 12.0 + tzOffset - lng / 15.0 - eqt

        // Sun altitude calculations
        val sunriseAngle = -0.8333
        val maghribAngle = -0.8333

        val sunriseH = hourAngle(sunriseAngle, lat, decl)
        val maghribH = hourAngle(maghribAngle, lat, decl)
        val fajrH = hourAngle(-settings.method.fajrAngle, lat, decl)

        val ishaH = if (settings.method == CalculationMethod.MAKKAH) {
            0.0
        } else {
            hourAngle(-settings.method.ishaAngle, lat, decl)
        }

        // Asr Calculation based on Shadow Factor (1 for Standard, 2 for Hanafi)
        val asrAngle = -r2d(atan(1.0 / (settings.school.shadowFactor + tan(d2r(abs(lat - decl))))))
        val asrH = hourAngle(asrAngle, lat, decl)

        val fajrHours = dhuhrBase - fajrH / 15.0
        val sunriseHours = dhuhrBase - sunriseH / 15.0
        val dhuhrHours = dhuhrBase + 0.033 // +2 mins safety buffer
        val asrHours = dhuhrBase + asrH / 15.0
        val maghribHours = dhuhrBase + maghribH / 15.0 + 0.033
        val ishaHours = if (settings.method == CalculationMethod.MAKKAH) {
            maghribHours + 1.5 // 90 mins after Maghrib
        } else {
            dhuhrBase + ishaH / 15.0
        }

        // Convert calculated hours to millis for the current calendar date
        val fajrMillis = getTimeMillisForHours(calendar, fajrHours)
        val sunriseMillis = getTimeMillisForHours(calendar, sunriseHours)
        val dhuhrMillis = getTimeMillisForHours(calendar, dhuhrHours)
        val asrMillis = getTimeMillisForHours(calendar, asrHours)
        val maghribMillis = getTimeMillisForHours(calendar, maghribHours)
        val ishaMillis = getTimeMillisForHours(calendar, ishaHours)

        val nowMillis = System.currentTimeMillis()

        // Formatting
        val fajrStr = formatTime(fajrMillis, settings.timeFormat)
        val sunriseStr = formatTime(sunriseMillis, settings.timeFormat)
        val dhuhrStr = formatTime(dhuhrMillis, settings.timeFormat)
        val asrStr = formatTime(asrMillis, settings.timeFormat)
        val maghribStr = formatTime(maghribMillis, settings.timeFormat)
        val ishaStr = formatTime(ishaMillis, settings.timeFormat)

        // Determine Current and Next Prayer
        val rawList = listOf(
            Triple("Fajr", fajrStr, fajrMillis),
            Triple("Sunrise", sunriseStr, sunriseMillis),
            Triple("Dhuhr", dhuhrStr, dhuhrMillis),
            Triple("Asr", asrStr, asrMillis),
            Triple("Maghrib", maghribStr, maghribMillis),
            Triple("Isha", ishaStr, ishaMillis)
        )

        var currentPrayer = "Isha"
        var nextPrayer = "Fajr"
        var nextPrayerTimeMillis = fajrMillis + 24 * 3600 * 1000L // Tomorrow's Fajr
        var nextPrayerFormatted = fajrStr

        if (nowMillis < fajrMillis) {
            currentPrayer = "Isha"
            nextPrayer = "Fajr"
            nextPrayerTimeMillis = fajrMillis
            nextPrayerFormatted = fajrStr
        } else if (nowMillis in fajrMillis until sunriseMillis) {
            currentPrayer = "Fajr"
            nextPrayer = "Sunrise"
            nextPrayerTimeMillis = sunriseMillis
            nextPrayerFormatted = sunriseStr
        } else if (nowMillis in sunriseMillis until dhuhrMillis) {
            currentPrayer = "Sunrise"
            nextPrayer = "Dhuhr"
            nextPrayerTimeMillis = dhuhrMillis
            nextPrayerFormatted = dhuhrStr
        } else if (nowMillis in dhuhrMillis until asrMillis) {
            currentPrayer = "Dhuhr"
            nextPrayer = "Asr"
            nextPrayerTimeMillis = asrMillis
            nextPrayerFormatted = asrStr
        } else if (nowMillis in asrMillis until maghribMillis) {
            currentPrayer = "Asr"
            nextPrayer = "Maghrib"
            nextPrayerTimeMillis = maghribMillis
            nextPrayerFormatted = maghribStr
        } else if (nowMillis in maghribMillis until ishaMillis) {
            currentPrayer = "Maghrib"
            nextPrayer = "Isha"
            nextPrayerTimeMillis = ishaMillis
            nextPrayerFormatted = ishaStr
        } else {
            currentPrayer = "Isha"
            nextPrayer = "Fajr"
            nextPrayerTimeMillis = fajrMillis + 24 * 3600 * 1000L // Tomorrow's Fajr
            nextPrayerFormatted = fajrStr
        }

        val countdownSecs = ((nextPrayerTimeMillis - nowMillis) / 1000).coerceAtLeast(0)

        val items = rawList.map { (name, formattedTime, millis) ->
            PrayerTimeItem(
                name = name,
                timeFormatted = formattedTime,
                timeMillis = millis,
                isCurrent = name.equals(currentPrayer, ignoreCase = true),
                isNext = name.equals(nextPrayer, ignoreCase = true)
            )
        }

        val qiblaDegree = calculateQiblaDirection(lat, lng)
        val qiblaText = "${String.format("%.1f", qiblaDegree)}° W"

        val sdfGregorian = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
        val gregorianDateStr = sdfGregorian.format(date)

        val hijriDateStr = calculateHijriDate(calendar)

        return PrayerTimesData(
            locationName = locationName,
            latitude = lat,
            longitude = lng,
            gregorianDate = gregorianDateStr,
            hijriDate = hijriDateStr,
            qiblaDirection = qiblaText,
            fajr = fajrStr,
            sunrise = sunriseStr,
            dhuhr = dhuhrStr,
            asr = asrStr,
            maghrib = maghribStr,
            isha = ishaStr,
            currentPrayerName = currentPrayer,
            nextPrayerName = nextPrayer,
            nextPrayerFormattedTime = nextPrayerFormatted,
            countdownSeconds = countdownSecs,
            items = items
        )
    }

    // Calculate Qibla Direction from given coordinates relative to Makkah (21.4225 N, 39.8262 E)
    private fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val makkahLat = d2r(21.4225)
        val makkahLng = d2r(39.8262)
        val phi = d2r(lat)
        val lambda = d2r(lng)

        val y = sin(makkahLng - lambda)
        val x = cos(phi) * tan(makkahLat) - sin(phi) * cos(makkahLng - lambda)
        var qibla = r2d(atan2(y, x))
        if (qibla < 0) qibla += 360.0
        return qibla
    }

    // Hijri date approximation
    private fun calculateHijriDate(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        val z = jd - 1948440 + 10632
        val n = floor((z - 1) / 10631.0)
        val z1 = z - 10631 * n + 354
        val j = (floor((10982 - z1) / 5316.0)) * (floor((50 * z1 + 5219) / 17719.0)) +
                (floor(z1 / 5670.0)) * (floor((43 * z1 - 1524) / 12592.0))
        val z2 = z1 - (floor((30 - j) / 15.0)) * (floor((17719 * j / 50.0) - 5219)) -
                (floor(j / 30.0)) * (floor((12592 * j / 43.0) + 1524)) + 1
        val hMonth = floor((24 * z2 + 22) / 709.0).toInt()
        val hDay = (z2 - floor((709 * hMonth - 28) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val hijriMonths = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        val monthName = if (hMonth in 1..12) hijriMonths[hMonth - 1] else "Safar"
        return "$hDay $monthName $hYear AH"
    }

    private fun getTimeMillisForHours(baseCalendar: Calendar, hours: Double): Long {
        val cal = baseCalendar.clone() as Calendar
        val h = hours.toInt()
        val remMinutes = (hours - h) * 60
        val m = remMinutes.toInt()
        val s = ((remMinutes - m) * 60).toInt()

        cal.set(Calendar.HOUR_OF_DAY, h.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, m.coerceIn(0, 59))
        cal.set(Calendar.SECOND, s.coerceIn(0, 59))
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatTime(millis: Long, format: TimeFormat): String {
        val sdf = if (format == TimeFormat.FORMAT_24H) {
            SimpleDateFormat("HH:mm", Locale.ENGLISH)
        } else {
            SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        }
        return sdf.format(Date(millis))
    }

    private fun hourAngle(angle: Double, lat: Double, decl: Double): Double {
        val top = -sin(d2r(angle)) - sin(d2r(lat)) * sin(d2r(decl))
        val bot = cos(d2r(lat)) * cos(d2r(decl))
        val cosH = top / bot
        return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 180.0 else r2d(acos(cosH))
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var b = angle - 360.0 * floor(angle / 360.0)
        if (b < 0) b += 360.0
        return b
    }

    private fun fixHour(hour: Double): Double {
        var b = hour - 24.0 * floor(hour / 24.0)
        if (b < 0) b += 24.0
        return b
    }

    private fun d2r(d: Double) = d * Math.PI / 180.0
    private fun r2d(r: Double) = r * 180.0 / Math.PI
}
