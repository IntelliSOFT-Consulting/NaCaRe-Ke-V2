package com.capture.app.network


import android.content.Context
import com.capture.app.data.FormatterClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitBuilder {

    fun getRetrofit(context: Context, BASE_URL: String): Retrofit {

        val formatter = FormatterClass()
        val encodedString = formatter.getSharedPref("encodedString", context)

        val basicAuthInterceptor = BasicAuthInterceptor(encodedString.toString())
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

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
            .build() //Doesn't require the adapter
    }

}