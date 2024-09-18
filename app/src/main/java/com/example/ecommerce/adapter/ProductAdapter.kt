package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerce.R
import com.example.ecommerce.data.apimodel.ProductItem

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    private var products = emptyList<ProductItem>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tv_descProduct)
        private val productImage: ImageView = itemView.findViewById(R.id.iv_product)
        private val productPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val productRating: TextView = itemView.findViewById(R.id.tv_ratting)

        fun bind(product: ProductItem) {
            productName.text = product.title?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            // Jika menggunakan URL gambar, gunakan Glide atau Picasso
            Glide.with(itemView.context)
                .load(product.image) // Menggunakan URL gambar dari model
               /* .placeholder(R.drawable.placeholder) // Gambar placeholder*/
                .into(productImage)

            productPrice.text = "${product.price?.let { String.format("%.2f", it) } ?: "N/A"}" // Format harga
            productRating.text = " ${product.rating?.rate ?: "N/A"}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentProduct = products[position]
        holder.bind(currentProduct)
    }

    // Fungsi untuk mengatur data produk di adapter
    fun setData(newData: List<ProductItem>) {
        products = newData
        notifyDataSetChanged() // Pemberitahuan ke adapter bahwa data telah berubah
    }
}
