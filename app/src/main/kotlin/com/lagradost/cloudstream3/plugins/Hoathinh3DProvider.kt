package com.lagradost.cloudstream3.plugins

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element

class Hoathinh3DProvider : MainAPI() {
    override var mainUrl = "https://hoathinh3d.ee"
    override var name = "Hoathinh3D"
    override val hasMainPage = true
    override var lang = "vi"
    override val supportedTypes = setOf(TvType.Anime, TvType.Movie)

    override val mainPage = mainPageOf(
        "$mainUrl/page/" to "Mới Cập Nhật",
        "$mainUrl/hot/page/" to "Phim Hot",
        "$mainUrl/hoan-thanh/page/" to "Hoàn Thành",
        "$mainUrl/genre/huyen-huyen/page/" to "Huyền Huyễn",
        "$mainUrl/genre/tien-hiep/page/" to "Tiên Hiệp",
        "$mainUrl/genre/xuyen-khong/page/" to "Xuyên Không"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = request.data + page
        val document = app.get(url).document
        val home = document.select("article.post, .halim-item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst(".entry-title, .halim-post-title")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src")
        val episode = this.selectFirst(".episode")?.text()

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
            addSub(episode?.filter { it.isDigit() }?.toIntOrNull())
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        return document.select("article.post, .halim-item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst(".entry-title")?.text()?.trim() ?: ""
        val poster = document.selectFirst(".movie-poster img")?.attr("src")
        val description = document.selectFirst("#entry-content")?.text()
        val originalTitle = document.selectFirst(".org_title")?.text()

        val episodes = document.select(".halim-list-eps li a").map {
            val href = it.attr("href")
            val name = it.text()
            newEpisode(href) {
                this.name = name
            }
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            this.tags = listOfNotNull(originalTitle)
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        // Extract required parameters from the active episode element
        val activeEpisode = document.selectFirst(".halim-episode.active a")
        var postId = activeEpisode?.attr("data-post-id")
        val chapterSt = activeEpisode?.attr("data-ep")
        val sv = activeEpisode?.attr("data-sv")

        // Fallback: Try to get post_id from Javascript variable if missing
        if (postId == null) {
            val script = document.select("script").find { it.html().contains("DoPostInfo") }?.html()
            postId = script?.substringAfter("id:")?.substringBefore(",")?.trim()
        }

        if (postId == null || chapterSt == null || sv == null) {
            return false
        }

        // Find available server types (e.g., VIP, Pro, 4K)
        val serverTypes = document.select(".get-eps[data-type]").mapNotNull { it.attr("data-type") }.distinct()
        // If no specific types found, try with empty type (default)
        val typesToTry = if (serverTypes.isNotEmpty()) serverTypes else listOf("")

        typesToTry.forEach { type ->
            try {
                val ajaxUrl = "$mainUrl/player/player.php"
                val ajaxDoc = app.get(
                    ajaxUrl,
                    params = mapOf(
                        "action" to "dox_ajax_player",
                        "post_id" to postId!!,
                        "chapter_st" to chapterSt,
                        "type" to type,
                        "sv" to sv
                    ),
                    headers = mapOf(
                        "X-Requested-With" to "XMLHttpRequest",
                        "Referer" to data
                    )
                ).document

                val iframeSrc = ajaxDoc.selectFirst("iframe")?.attr("abs:src")
                if (!iframeSrc.isNullOrBlank()) {
                    loadExtractor(iframeSrc, subtitleCallback, callback)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return true
    }
}
