package com.ecodeli.network

import android.content.Context
import android.util.Log
import com.google.gson.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {

    companion object {
        private const val TAG = "ApiClient"
    }

    // Créer un Gson personnalisé pour gérer les types flexibles
    private val gson = GsonBuilder()
        .registerTypeAdapter(Any::class.java, FlexibleTypeAdapter())
        .setLenient() // Pour être plus tolérant avec le JSON
        .create()

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = getStoredToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader(ApiConfig.AUTHORIZATION_HEADER, "${ApiConfig.BEARER_PREFIX}$token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    private fun getStoredToken(): String? {
        val prefs = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }

    fun saveToken(token: String) {
        val prefs = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auth_token", token).apply()
        Log.d(TAG, "Token sauvegardé")
    }

    fun clearToken() {
        val prefs = context.getSharedPreferences("ecodeli_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("auth_token").apply()
        Log.d(TAG, "Token supprimé")
    }

    // Adaptateur pour gérer les types flexibles (objet ou ID)
    class FlexibleTypeAdapter : JsonDeserializer<Any>, JsonSerializer<Any> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Any {
            return when {
                json.isJsonNull -> {
                    Log.d(TAG, "JSON null trouvé")
                    null as Any
                }
                json.isJsonPrimitive -> {
                    val primitive = json.asJsonPrimitive
                    when {
                        primitive.isBoolean -> {
                            Log.d(TAG, "Boolean trouvé: ${primitive.asBoolean}")
                            primitive.asBoolean
                        }
                        primitive.isNumber -> {
                            Log.d(TAG, "Number trouvé: ${primitive.asNumber}")
                            // Essayer de convertir en Int d'abord, puis en Double
                            try {
                                primitive.asInt
                            } catch (e: NumberFormatException) {
                                primitive.asDouble
                            }
                        }
                        primitive.isString -> {
                            Log.d(TAG, "String trouvé: ${primitive.asString}")
                            primitive.asString
                        }
                        else -> {
                            Log.d(TAG, "Primitive inconnu: $primitive")
                            primitive.toString()
                        }
                    }
                }
                json.isJsonObject -> {
                    Log.d(TAG, "Object trouvé: ${json.asJsonObject}")
                    json.asJsonObject
                }
                json.isJsonArray -> {
                    Log.d(TAG, "Array trouvé: ${json.asJsonArray}")
                    json.asJsonArray
                }
                else -> {
                    Log.d(TAG, "Type JSON inconnu: $json")
                    json.toString()
                }
            }
        }

        override fun serialize(src: Any, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src)
        }
    }
}