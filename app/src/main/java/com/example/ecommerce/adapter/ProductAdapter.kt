package com.example.ecommerce.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerce.R
import com.example.ecommerce.data.apimodel.ProductItem
import com.example.ecommerce.utils.ProductDiffutil

class ProductAdapter(
    private val listener: OnItemClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var products = emptyList<ProductItem>()
    private var isLoading = true // Menandai apakah shimmer sedang ditampilkan

    companion object {
        private const val VIEW_TYPE_SHIMMER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    // ViewHolder untuk item asli
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tv_descProduct)
        private val productImage: ImageView = itemView.findViewById(R.id.iv_product)
        private val productPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val productRating: TextView = itemView.findViewById(R.id.tv_ratting)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(product: ProductItem) {
            productName.text = product.title.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }

            Glide.with(itemView.context)
                .load(product.image)
                .into(productImage)

            productPrice.text = String.format("%.2f", product.price)

            val ratingText = if (product.rating.rate != 0.0) {
                String.format("%.1f", product.rating.rate) + " / 5"
            } else {
                "No rating available"
            }
            productRating.text = ratingText
        }
    }

    // ViewHolder untuk shimmer loading
    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerFrameLayout: com.facebook.shimmer.ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmerFrame)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.placeholder_product, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
            MyViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else products.size // Misal menampilkan 10 shimmer item saat loading
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShimmerViewHolder) {
            // Memulai shimmer effect
            holder.shimmerFrameLayout.startShimmer()
        } else if (holder is MyViewHolder) {
            val currentProduct = products[position]
            holder.bind(currentProduct)
            holder.itemView.setOnClickListener {
                listener?.onItemClick(currentProduct)
            }
        }
    }

    fun setData(newData: List<ProductItem>) {
        val productDiffUtil = ProductDiffutil(products, newData)
        val diffUtilResult = DiffUtil.calculateDiff(productDiffUtil)
        products = newData
        isLoading = false // Data sudah tersedia, sembunyikan shimmer
        diffUtilResult.dispatchUpdatesTo(this)
    }

    fun showShimmerEffect() {
        isLoading = true // Mengaktifkan shimmer
        notifyDataSetChanged()
    }

    fun hideShimmerEffect() {
        isLoading = false // Menonaktifkan shimmer
        notifyDataSetChanged()
    }

    fun clearData() {
        val diffCallback = ProductDiffutil(products, emptyList())
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        products = emptyList()
        isLoading = true // Kembali ke shimmer mode
        diffResult.dispatchUpdatesTo(this)
    }

    interface OnItemClickListener {
        fun onItemClick(data: ProductItem)
    }
}

