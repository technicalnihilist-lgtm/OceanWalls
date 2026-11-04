package com.oceanwalls.app.core

import android.content.Context
import androidx.room.Room
import com.oceanwalls.app.data.DownloadRepository
import com.oceanwalls.app.data.FavoritesRepository
import com.oceanwalls.app.data.RecentRepository
import com.oceanwalls.app.data.SearchHistoryRepository
import com.oceanwalls.app.data.WallpaperIndex
import com.oceanwalls.app.data.WallpaperRepository
import com.oceanwalls.app.data.local.OceanDatabase
import com.oceanwalls.app.data.local.SettingsStore
import com.oceanwalls.app.data.remote.CommonsApi
import com.oceanwalls.app.data.remote.CommonsParams
import com.oceanwalls.app.data.remote.RetryInterceptor
import com.oceanwalls.app.util.WallpaperSetter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Hand-rolled dependency container: small, explicit, no annotation processing needed. */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val settings = SettingsStore(appContext)

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(RetryInterceptor())
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", CommonsParams.USER_AGENT)
                    .build()
            )
        }
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val api: CommonsApi = Retrofit.Builder()
        .baseUrl(CommonsParams.API_BASE)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CommonsApi::class.java)

    private val database: OceanDatabase = Room.databaseBuilder(
        appContext, OceanDatabase::class.java, OceanDatabase.NAME
    ).fallbackToDestructiveMigration().build()

    val index = WallpaperIndex()

    val wallpapers = WallpaperRepository(
        api = api,
        feedDao = database.feedDao(),
        recentDao = database.recentDao(),
        dailyDao = database.dailyDao(),
        index = index
    )

    val downloads = DownloadRepository(appContext, httpClient, database.downloadDao())
    val favorites = FavoritesRepository(database.favoriteDao())
    val recents = RecentRepository(database.recentDao())
    val searchHistory = SearchHistoryRepository(database.searchDao())
    val wallpaperSetter = WallpaperSetter(appContext)
}
