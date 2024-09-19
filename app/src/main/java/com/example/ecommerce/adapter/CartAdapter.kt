package com.example.ecommerce.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerce.R
import com.example.ecommerce.database.cart.Cart
import com.example.ecommerce.databinding.ItemCartBinding
import com.example.ecommerce.viewmodel.CartViewModel
import com.google.android.material.snackbar.Snackbar

class CartAdapter(
    private val cartViewModel: CartViewModel
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems: List<Cart> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding =
            ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int = cartItems.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItems[position]
        holder.bind(currentItem)

        // Handle delete action
        holder.ivDelete.setOnClickListener {
            cartViewModel.deleteCartItemById(currentItem.id.toLong())
            showSnackBar(holder.itemView)
        }

        // Handle increase quantity
        holder.btPlus.setOnClickListener {
            val newAmount = currentItem.quantity + 1
            currentItem.quantity = newAmount
            cartViewModel.updateCart(currentItem)
            holder.tvNumber.text = newAmount.toString()
        }

        // Handle decrease quantity
        holder.btMin.setOnClickListener {
            if (currentItem.quantity > 1) {
                val newAmount = currentItem.quantity - 1
                currentItem.quantity = newAmount
                cartViewModel.updateCart(currentItem)
                holder.tvNumber.text = newAmount.toString()
            }
        }
    }

    class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val ivDelete: ImageView = binding.btnDelete
        val btPlus: Button = binding.btnIncrease
        val btMin: Button = binding.btnDecrease
        val tvNumber: TextView = binding.tvQuantity

        fun bind(cartItem: Cart) {
            Glide.with(itemView.context)
                .load(cartItem.image)
                .into(binding.imgProduct)

            binding.tvProductName.text = cartItem.title
            binding.tvProductPrice.text = "Rp ${cartItem.price ?: 0}"
            binding.tvQuantity.text = cartItem.quantity.toString()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(cartItems: List<Cart>) {
        this.cartItems = cartItems
        notifyDataSetChanged()
    }

    private fun showSnackBar(view: View) {
        Snackbar.make(view, "Item removed from the cart", Snackbar.LENGTH_SHORT).show()
    }
}