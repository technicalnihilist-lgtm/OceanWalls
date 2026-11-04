package com.oceanwalls.app.data.remote

import retrofit2.http.GET
import retrofit2.http.QueryMap

/** Official Wikimedia Commons MediaWiki API. One explicit, inspectable entry point. */
interface CommonsApi {
    @GET("w/api.php")
    suspend fun query(@QueryMap params: Map<String, String>): CommonsResponse
}
