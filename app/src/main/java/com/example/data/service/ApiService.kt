package com.example.data.service

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// --- Network Request/Response DTOs ---

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class AuthRequest(
    val email: String,
    val phone: String,
    val passwordHash: String
)

data class SignUpRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String
)

data class OtpRequest(
    val email: String,
    val otpCode: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otpCode: String,
    val newPasswordHash: String
)

data class FcmTokenRequest(
    val email: String,
    val fcmToken: String
)

data class NetworkUserDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: NetworkUserDto
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val token: String,
    val refreshToken: String
)

data class ApiGenericResponse(
    val success: Boolean,
    val message: String
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phone: String
)

data class ChangePasswordRequest(
    val oldPasswordHash: String,
    val newPasswordHash: String
)

data class UploadProfilePictureRequest(
    val profilePictureUrl: String
)

// --- Retrofit Endpoint Definition ---

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): ApiGenericResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): AuthResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiGenericResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiGenericResponse

    @POST("auth/register-fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): ApiGenericResponse

    @GET("trains/search")
    suspend fun searchTrains(
        @Query("source") source: String,
        @Query("destination") destination: String,
        @Query("type") type: String
    ): List<TrainSearchItem>

    @GET("trains/{number}/live-status")
    suspend fun getLiveStatus(@Path("number") trainNumber: String): LiveStatus

    @GET("trains/{number}/schedule")
    suspend fun getTrainSchedule(@Path("number") trainNumber: String): TrainSchedule

    @GET("trains/station/{code}")
    suspend fun getStationInfo(@Path("code") stationCode: String): StationInfo

    @GET("trains/freight")
    suspend fun getFreightTrains(): List<FreightTrainItem>

    @GET("trains/weather")
    suspend fun getWeather(@Query("location") location: String): WeatherData

    @GET("trains/prayer")
    suspend fun getNamazTimings(@Query("location") location: String): NamazTimingsData

    @GET("trains/news")
    suspend fun getNews(): List<NewsItem>

    @GET("trains/blogs")
    suspend fun getBlogs(): List<BlogItem>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): TokenRefreshResponse

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiGenericResponse

    @POST("users/profile/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiGenericResponse

    @DELETE("users/profile")
    suspend fun deleteAccount(): ApiGenericResponse

    @POST("users/profile/picture")
    suspend fun uploadProfilePicture(@Body request: UploadProfilePictureRequest): ApiGenericResponse
}

// --- Secure Token Storage Helper ---

class TokenManager(private val context: android.content.Context) {
    private val prefs = context.getSharedPreferences("secure_prefs", android.content.Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        val encrypted = CryptographyHelper.encrypt(token)
        prefs.edit().putString("auth_token", encrypted).apply()
    }

    fun getToken(): String? {
        val encrypted = prefs.getString("auth_token", null) ?: return null
        return CryptographyHelper.decrypt(encrypted)
    }

    fun saveRefreshToken(token: String) {
        val encrypted = CryptographyHelper.encrypt(token)
        prefs.edit().putString("refresh_token", encrypted).apply()
    }

    fun getRefreshToken(): String? {
        val encrypted = prefs.getString("refresh_token", null) ?: return null
        return CryptographyHelper.decrypt(encrypted)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

// --- ApiClient Builder ---

object ApiClient {
    private val BASE_URL: String = run {
        val raw = com.example.BuildConfig.API_URL.takeIf { !it.isNullOrBlank() }
            ?: "https://train-tracker-production-b6d0.up.railway.app/api/"
        var url = raw.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        if (!url.endsWith("/")) {
            url = "$url/"
        }
        if (!url.endsWith("api/")) {
            url = "${url}api/"
        }
        url
    }
    private var tokenManager: TokenManager? = null

    fun initialize(context: android.content.Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    fun getTokenManager(): TokenManager? = tokenManager

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val builder = chain.request().newBuilder()
        tokenManager?.getToken()?.let { token ->
            builder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(builder.build())
    }

    private val tokenAuthenticator = okhttp3.Authenticator { _, response ->
        val refreshToken = tokenManager?.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            return@Authenticator null
        }

        synchronized(this) {
            val currentToken = tokenManager?.getToken()
            val requestHeaderToken = response.request.header("Authorization")?.substringAfter("Bearer ")
            if (currentToken != null && currentToken != requestHeaderToken) {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            try {
                val refreshClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build()

                val refreshService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(refreshClient)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                    .create(ApiService::class.java)

                val tokenResponse = kotlinx.coroutines.runBlocking {
                    refreshService.refreshToken(TokenRefreshRequest(refreshToken))
                }

                tokenManager?.saveToken(tokenResponse.token)
                tokenManager?.saveRefreshToken(tokenResponse.refreshToken)

                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokenResponse.token}")
                    .build()
            } catch (e: Exception) {
                tokenManager?.clear()
                return@Authenticator null
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    fun parseError(e: Throwable): String {
        return try {
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val adapter = moshi.adapter(ApiGenericResponse::class.java)
                    adapter.fromJson(errorBody)?.message ?: "An unexpected server error occurred"
                } else {
                    "Server error (HTTP ${e.code()})"
                }
            } else {
                e.message ?: "Network connection error. Please check your internet connection."
            }
        } catch (ex: Exception) {
            "Network error or server is offline"
        }
    }
}
