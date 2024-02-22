package com.capture.app.network
import okhttp3.Interceptor
import okhttp3.Response


class BasicAuthInterceptor(private val encodedString: String) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val basicAuth = "Basic $encodedString"

        val request = chain.request().newBuilder()
            .header("Authorization", basicAuth)
            .build()

        return chain.proceed(request)
    }
}