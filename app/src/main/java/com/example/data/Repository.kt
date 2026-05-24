package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val browserDao: BrowserDao) {
    val bookmarks: Flow<List<BookmarkItem>> = browserDao.getAllBookmarks()
    val history: Flow<List<HistoryItem>> = browserDao.getAllHistory()
    val savedResearch: Flow<List<SavedResearch>> = browserDao.getAllSavedResearch()

    suspend fun insertBookmark(item: BookmarkItem) = browserDao.insertBookmark(item)
    suspend fun deleteBookmark(id: Int) = browserDao.deleteBookmark(id)
    suspend fun deleteBookmarkByUrl(url: String) = browserDao.deleteBookmarkByUrl(url)
    suspend fun isBookmarked(url: String): Boolean = browserDao.isBookmarked(url)

    suspend fun insertHistory(item: HistoryItem) = browserDao.insertHistory(item)
    suspend fun deleteHistory(id: Int) = browserDao.deleteHistory(id)
    suspend fun clearHistory() = browserDao.clearHistory()

    suspend fun insertResearch(item: SavedResearch) = browserDao.insertResearch(item)
    suspend fun deleteResearch(id: Int) = browserDao.deleteResearch(id)
}
