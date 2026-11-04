package com.oceanwalls.app.data

import com.oceanwalls.app.data.local.DailyDao
import com.oceanwalls.app.data.local.DailyEntity
import com.oceanwalls.app.data.local.FeedDao
import com.oceanwalls.app.data.local.FeedItemEntity
import com.oceanwalls.app.data.local.RecentDao
import com.oceanwalls.app.data.local.RecentEntity
import com.oceanwalls.app.data.local.WallpaperJson
import com.oceanwalls.app.data.model.Catalog
import com.oceanwalls.app.data.model.FeedPage
import com.oceanwalls.app.data.model.FeedSpec
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.data.remote.CommonsApi
import com.oceanwalls.app.data.remote.CommonsMapper
import com.oceanwalls.app.data.remote.CommonsParams
import com.oceanwalls.app.data.remote.CommonsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CommonsException(message: String) : IOException(message)

/**
 * Single source of truth. Every page is fetched live from the Wikimedia Commons API;
 * Room only mirrors what was already fetched so the UI degrades gracefully if the
 * network drops mid-session.
 */
class WallpaperRepository(
    private val api: CommonsApi,
    private val feedDao: FeedDao,
    private val recentDao: RecentDao,
    private val dailyDao: DailyDao,
    private val index: WallpaperIndex
) {

    private enum class Mode { CATEGORY, SEARCH }

    private data class CursorState(val mode: Mode, val token: String)

    fun cachedFeed(feedKey: String): Flow<List<Wallpaper>> =
        feedDao.observeFeed(feedKey).map { rows -> rows.mapNotNull { WallpaperJson.decode(it.payload) } }

    suspend fun cachedCount(feedKey: String): Int = withContext(Dispatchers.IO) { feedDao.count(feedKey) }

    suspend fun byId(pageId: Long): Wallpaper? = withContext(Dispatchers.IO) {
        index.get(pageId) ?: feedDao.findByPageId(pageId)?.let { WallpaperJson.decode(it.payload) }?.also { index.put(it) }
    }

    suspend fun markViewed(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        index.put(wallpaper)
        recentDao.upsert(
            RecentEntity(wallpaper.pageId, WallpaperJson.encode(wallpaper), System.currentTimeMillis())
        )
        recentDao.trim(60)
    }

    suspend fun loadFeed(spec: FeedSpec, cursor: String?): Result<FeedPage> = withContext(Dispatchers.IO) {
        runCatching { fetchFeed(spec, cursor) }
    }

    private suspend fun fetchFeed(spec: FeedSpec, cursor: String?): FeedPage {
        var state = initialCursor(spec, cursor)
        val batch = LinkedHashMap<Long, Wallpaper>()
        var rounds = 0
        var hasMore = true

        while (rounds < 4 && batch.size < PAGE_SIZE && hasMore) {
            rounds++
            val response = request(spec, state)
            response.error?.let { throw CommonsException(it.info ?: it.code ?: "Wikimedia Commons API error") }
            val pages = response.query?.pages.orEmpty()
            if (pages.isEmpty()) {
                hasMore = false
                break
            }
            pages.forEach { page ->
                val wallpaper = CommonsMapper.toWallpaper(page, spec.label, spec)
                if (wallpaper != null) batch[page.pageId] = wallpaper
            }
            hasMore = advance(spec, response, state) { state = it }
        }

        // Best matches first: photographs whose title names the subject rank above loose hits.
        val ranked = batch.values.sortedByDescending { wallpaper ->
            val title = wallpaper.title.lowercase()
            spec.keywords.count { title.contains(it) }
        }
        val fresh = persist(spec.id, ranked)
        val nextCursor = if (hasMore) encodeCursor(state) else null
        return FeedPage(items = fresh, nextCursor = nextCursor, hasMore = hasMore && fresh.isNotEmpty())
    }

    private fun initialCursor(spec: FeedSpec, cursor: String?): CursorState {
        if (cursor == null) {
            return if (spec.category != null) CursorState(Mode.CATEGORY, "") else CursorState(Mode.SEARCH, "0")
        }
        return if (cursor.startsWith(CURSOR_CATEGORY)) {
            CursorState(Mode.CATEGORY, cursor.removePrefix(CURSOR_CATEGORY))
        } else {
            CursorState(Mode.SEARCH, cursor.removePrefix(CURSOR_SEARCH))
        }
    }

    private fun encodeCursor(state: CursorState): String = when (state.mode) {
        Mode.CATEGORY -> CURSOR_CATEGORY + state.token
        Mode.SEARCH -> CURSOR_SEARCH + state.token
    }

    private suspend fun request(spec: FeedSpec, state: CursorState): CommonsResponse = when (state.mode) {
        Mode.CATEGORY -> {
            val category = spec.category
            if (category.isNullOrBlank()) {
                api.query(CommonsParams.searchFiles(spec.search, PAGE_SIZE * 2, state.token.toIntOrNull() ?: 0))
            } else {
                api.query(CommonsParams.categoryFiles(category, PAGE_SIZE * 2, state.token.ifBlank { null }))
            }
        }
        Mode.SEARCH -> api.query(
            CommonsParams.searchFiles(spec.search, PAGE_SIZE * 2, state.token.toIntOrNull() ?: 0)
        )
    }

    /** True while more results exist; updates [state] to the next cursor. */
    private inline fun advance(
        spec: FeedSpec,
        response: CommonsResponse,
        state: CursorState,
        update: (CursorState) -> Unit
    ): Boolean = when (state.mode) {
        Mode.CATEGORY -> {
            val next = response.continueToken?.gcmContinue
            if (next.isNullOrBlank()) {
                if (spec.search.isNotBlank()) {
                    update(CursorState(Mode.SEARCH, "0"))
                    true
                } else false
            } else {
                update(CursorState(Mode.CATEGORY, next))
                true
            }
        }
        Mode.SEARCH -> {
            val next = response.continueToken?.gsrOffset
            if (next == null) false else {
                update(CursorState(Mode.SEARCH, next.toString()))
                true
            }
        }
    }

    /** Room insert with IGNORE gives duplicate prevention for free. */
    private suspend fun persist(feedKey: String, items: List<Wallpaper>): List<Wallpaper> {
        if (items.isEmpty()) return emptyList()
        val startPosition = feedDao.count(feedKey)
        val now = System.currentTimeMillis()
        val rows = items.mapIndexed { offset, wallpaper ->
            FeedItemEntity(feedKey, wallpaper.pageId, WallpaperJson.encode(wallpaper), startPosition + offset, now)
        }
        val inserted = feedDao.insertAll(rows)
        val fresh = items.filterIndexed { i, _ -> (inserted.getOrNull(i) ?: -1L) != -1L }
        index.putAll(items)
        return fresh
    }

    suspend fun search(query: String, cursor: String?): Result<FeedPage> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmed = query.trim()
            if (trimmed.length < 2) throw CommonsException("Type at least two characters")
            fetchFeed(
                FeedSpec(
                    id = "search:${trimmed.lowercase(Locale.US)}",
                    label = trimmed,
                    search = trimmed,
                    keywords = emptyList(),
                    minWidth = 1000,
                    minHeight = 700
                ),
                cursor
            )
        }
    }

    suspend fun dailyWallpaper(): Result<Wallpaper> = withContext(Dispatchers.IO) {
        runCatching {
            val today = dayKey()
            dailyDao.find(today)?.let { cached -> WallpaperJson.decode(cached.payload)?.let { return@runCatching it } }

            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val query = Catalog.dailyQueries[dayOfYear % Catalog.dailyQueries.size]
            val page = fetchFeed(
                FeedSpec(
                    id = "feed:daily:$today",
                    label = "Daily ocean wallpaper",
                    search = query,
                    keywords = OCEAN_HINTS,
                    mustMatch = Catalog.OCEAN_SUBJECT
                ),
                null
            )
            if (page.items.isEmpty()) throw CommonsException("No ocean wallpaper available today")
            val pick = page.items[dayOfYear % page.items.size]
            dailyDao.put(DailyEntity(today, WallpaperJson.encode(pick), System.currentTimeMillis()))
            pick
        }
    }

    /** Newest ocean uploads on Commons - this feed genuinely changes minute by minute. */
    suspend fun recentUploads(limit: Int = 30): Result<List<Wallpaper>> = withContext(Dispatchers.IO) {
        runCatching {
            val changes = api.query(CommonsParams.recentUploads(500))
            changes.error?.let { throw CommonsException(it.info ?: "recentchanges failed") }
            val titles = changes.query?.recentChanges.orEmpty()
                .mapNotNull { it.title }
                .filter { title -> OCEAN_TITLE.containsMatchIn(title) }
                .distinct()
                .take(limit * 2)

            if (titles.isEmpty()) return@runCatching emptyList()

            val spec = FeedSpec("feed:recent", "Recently added", "", emptyList(), minWidth = 1100, minHeight = 800)
            val collected = LinkedHashMap<Long, Wallpaper>()
            titles.chunked(MAX_TITLES_PER_REQUEST).forEach { chunk ->
                api.query(CommonsParams.byTitles(chunk)).query?.pages.orEmpty().forEach { page ->
                    CommonsMapper.toWallpaper(page, spec.label, spec)?.let { collected[page.pageId] = it }
                }
            }
            persist(spec.id, collected.values.toList()).take(limit)
        }
    }

    private fun dayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    companion object {
        private const val PAGE_SIZE = 18
        private const val MAX_TITLES_PER_REQUEST = 50
        private const val CURSOR_CATEGORY = "c:"
        private const val CURSOR_SEARCH = "s:"

        val OCEAN_HINTS = listOf(
            "ocean", "sea", "marine", "underwater", "coral", "reef", "beach", "coast", "wave",
            "whale", "shark", "dolphin", "turtle", "fish", "island", "lagoon", "deep", "abyss",
            "dive", "diver", "shore", "surf", "bay", "strait", "iceberg", "arctic", "antarctic",
            "polar", "estuary", "mangrove", "seagrass", "kelp", "jellyfish", "octopus", "seal",
            "penguin", "plankton", "tide", "harbour", "harbor", "pier", "nautical", "shipwreck"
        )

        private val OCEAN_TITLE = Regex(
            "(ocean|sea|marine|underwater|coral|reef|beach|coast|wave|whale|shark|dolphin|turtle|" +
                "fish|island|lagoon|abyss|dive|diver|shore|surf|bay|strait|iceberg|arctic|antarctic|" +
                "polar|estuary|mangrove|seagrass|kelp|jellyfish|octopus|seal|penguin|plankton|tide|" +
                "harbour|harbor|pier|shipwreck|nautilus|crustacean|mollusc|anemone|starfish|urchin)",
            RegexOption.IGNORE_CASE
        )
    }
}
