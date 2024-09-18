package com.example.ecommerce

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorationRv(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // Posisi item di adapter
        val column = position % spanCount // Kolom item

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // Jika item berada di baris pertama, tambahkan margin atas
                outRect.top = spacing
            }
            outRect.bottom = spacing // Set margin bawah untuk semua item
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // Tambahkan margin atas kecuali untuk baris pertama
            }
        }
    }
}