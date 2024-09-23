package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.R

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    // Peta gambar lokal berdasarkan kategori
    private val categoryImages = mapOf(
        "electronics" to R.drawable.img,
        "jewelery" to R.drawable.jewelry,
        "men's clothing" to R.drawable.mens_clothing,
        "women's clothing" to R.drawable.women_clothing
    )

    private var categories = emptyList<String>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.tv_desc)
        private val categoryImage: ImageView = itemView.findViewById(R.id.cat_image)

        fun bind(category: String, imageResId: Int) {
            categoryName.text = category
            categoryImage.setImageResource(imageResId) // Menggunakan ID resource untuk gambar
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentCategory = categories[position]
        // Ubah awalan huruf menjadi huruf kapital
        val capitalizedCategory = currentCategory.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        val imageResId = categoryImages[currentCategory] ?: R.drawable.img
        holder.bind(capitalizedCategory, imageResId)
    }

    // Fungsi untuk mengatur data kategori di adapter
    fun setData(newData: List<String>) {
        categories = newData
        notifyDataSetChanged() // Pemberitahuan ke adapter bahwa data telah berubah
    }
}
