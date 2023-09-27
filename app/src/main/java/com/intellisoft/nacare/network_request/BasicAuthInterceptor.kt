package com.intellisoft.nacare.network_request

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor(private val username: String, private val password: String) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val credentials = "$username:$password"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val request = chain.request().newBuilder()
            .header("Authorization", basicAuth)
            .build()

        return chain.proceed(request)
    }
}
