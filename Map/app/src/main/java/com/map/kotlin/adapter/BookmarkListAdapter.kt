package com.map.kotlin.adapter

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.map.kotlin.R
import com.map.kotlin.model.Bookmark
import com.map.kotlin.ui.MapsActivity
import com.map.kotlin.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.bookmark_items.view.*

class BookmarkListAdapter(
    private var bookmarkData: List<MapsViewModel.BookmarkView>?,
    private var mapsActivity: MapsActivity
) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {
    class ViewHolder(
        v: View,
        private val mapsActivity: MapsActivity
    ) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener{
                val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }
        val nameTextView: TextView =
            v.findViewById(R.id.bookmarkNameTextView) as TextView
        val categoryImageView: ImageView =
            v.findViewById(R.id.bookmarkIcon) as ImageView
    }

    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>) {
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookmarkListAdapter.ViewHolder {
        val viewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.bookmark_items, parent, false),
            mapsActivity
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkData = bookmarkData?: return
        val bookmarkViewData = bookmarkData[position]
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        holder.categoryImageView.setImageResource(R.drawable.common_google_signin_btn_icon_dark)
    }

    override fun getItemCount(): Int {
        return bookmarkData?.size ?: 0
    }

}
