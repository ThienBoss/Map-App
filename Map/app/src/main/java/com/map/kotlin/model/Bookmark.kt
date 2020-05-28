package com.map.kotlin.model

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRouter
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.map.kotlin.util.ImageUtils

@Entity
// 2
data class Bookmark(
// 3
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
// 4
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes : String = "",
    var category: String = ""
) {
    fun setImage(context: Context, image: Bitmap) {
        id?.let {
            ImageUtils.saveBitmapToFile(context, image, generateImageFileName(it))
        }
    }

    companion object {
        fun generateImageFileName(id: Long): String {
            return "bookmark$id.png"

        }
    }
}