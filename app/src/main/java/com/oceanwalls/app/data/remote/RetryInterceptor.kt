package com.oceanwalls.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Backoff for flaky networks, 5xx responses and Wikimedia rate limiting. */
class RetryInterceptor(private val maxAttempts: Int = 3) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastError: IOException? = null

        for (attempt in 0 until maxAttempts) {
            try {
                val response = chain.proceed(request)
                val retryable = response.code == 429 || response.code in 500..599
                if (!retryable) return response
                response.close()
                backoff(attempt)
            } catch (io: IOException) {
                lastError = io
                backoff(attempt)
            }
        }
        throw lastError ?: IOException("Commons request failed after $maxAttempts attempts")
    }

    private fun backoff(attempt: Int) {
        try {
            Thread.sleep(350L * (attempt + 1))
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
