package com.intellisoft.nacare.network_request

import android.content.Context
import com.intellisoft.nacare.helper_class.FormatterClass
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

  fun getRetrofit(context: Context,BASE_URL: String): Retrofit {

    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    val formatter = FormatterClass()
      val username=formatter.getSharedPref("username", context)
      val password= formatter.getSharedPref("password", context)

    val basicAuthInterceptor = BasicAuthInterceptor(username.toString(),password.toString())

    val client =
        OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(2, TimeUnit.MINUTES)
            .addInterceptor(interceptor)
            .addInterceptor(basicAuthInterceptor)
            .build()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build() // Doesn't require the adapter
  }
}
