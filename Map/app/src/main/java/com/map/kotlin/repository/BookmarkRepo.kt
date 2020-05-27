package com.map.kotlin.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.map.kotlin.db.BookmarkDao
import com.map.kotlin.db.PlaceBookDatabase
import com.map.kotlin.model.Bookmark
import com.map.kotlin.viewmodel.MapsViewModel
import java.lang.ProcessBuilder.Redirect.to

/**
 *
 */
class BookmarkRepo(context: Context) {

    private var db = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()
    public var abc = 1

    /**
     *
     */
    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    /**
     *
     */
    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get()
            return bookmarkDao.loadAll()
        }

    fun getLiveData(bookmarkId: Long): LiveData<Bookmark> {
        val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
        return bookmark
    }

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }
    private fun buildCategoryMap() : HashMap<Int, String>{
        return hashMapOf(Place.abc to "retaurent")
    }
}