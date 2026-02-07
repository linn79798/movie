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
            addSub(episode)
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
            Episode(href, name)
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            this.tags = listOf(originalTitle ?: "")
            addEpisodes(episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        // Halim theme usually stores sources in scripts or iframes
        // Check for common servers in Halim themes
        document.select("script").forEach { script ->
            val html = script.html()
            if (html.contains("halim_cfg")) {
                // Extract player logic if needed, often standard halim handling
            }
        }

        // Attempt to find iframes or sources directly
        val source = document.selectFirst("#halim-player iframe")?.attr("src")
        if (source != null) {
            loadExtractor(source, data, subtitleCallback, callback)
        }

        return true
    }
}
