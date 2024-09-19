package com.example.ecommerce.adapter

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
) : RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    private var products = emptyList<ProductItem>()

    // ViewHolder to bind data to the view
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tv_descProduct)
        private val productImage: ImageView = itemView.findViewById(R.id.iv_product)
        private val productPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val productRating: TextView = itemView.findViewById(R.id.tv_ratting)

        // Bind the product data to the views
        fun bind(product: ProductItem) {
            productName.text = product.title.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }

            // Load product image using Glide or Picasso (Glide used here)
            Glide.with(itemView.context)
                .load(product.image) // Use the image URL from the product
                /* .placeholder(R.drawable.placeholder)*/ // Optionally, add a placeholder image
                .into(productImage)

            // Format and display price
            productPrice.text = "${product.price.let { String.format("%.2f", it) } ?: "N/A"}"

            // Display product rating
            productRating.text = "${product.rating.rate ?: "N/A"}"
        }
    }

    // Create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return MyViewHolder(view)
    }

    // Return the item count
    override fun getItemCount(): Int = products.size

    // Bind the ViewHolder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentProduct = products[position]
        holder.bind(currentProduct)

        // Set the click listener for each item
        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentProduct)
        }
    }

    // Interface for item click handling
    interface OnItemClickListener {
        fun onItemClick(data: ProductItem)
    }

    // Function to set new data using DiffUtil
    fun setData(newData: List<ProductItem>) {
        val productDiffUtil = ProductDiffutil(products, newData)
        val diffUtilResult = DiffUtil.calculateDiff(productDiffUtil)
        products = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }

    // Function to clear data using DiffUtil
    fun clearData() {
        val diffCallback = ProductDiffutil(products, emptyList())
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        products = emptyList()
        diffResult.dispatchUpdatesTo(this)
    }
}
