package com.example.ecommerce.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerce.database.cart.Cart
import com.example.ecommerce.databinding.ItemCartBinding
import com.example.ecommerce.viewmodel.CartViewModel

class CartAdapter(
    private val cartViewModel: CartViewModel
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems: List<Cart> = emptyList()

    fun getCartItemAt(position: Int): Cart {
        return cartItems[position]
    }

    fun deleteCartItem(cartItem: Cart) {
        cartViewModel.deleteCartItemById(cartItem.id.toLong())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int = cartItems.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItems[position]
        holder.bind(currentItem)
/*
        // Set up delete button click listener
        holder.ivDelete.setOnClickListener {
            cartViewModel.deleteCartItemById(currentItem.id.toLong())
            notifyItemRemoved(position)
        }*/

    holder.btPlus.setOnClickListener {
            val newAmount = currentItem.quantity + 1
            currentItem.quantity = newAmount
            cartViewModel.updateCart(currentItem)
            holder.tvNumber.text = newAmount.toString()
        }


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
     /*   val ivDelete: ImageView = binding.iconDelete*/
        val btPlus: Button = binding.btnIncrement
        val btMin: Button = binding.btnDecrement
        val tvNumber: TextView = binding.tvQuantity


        @SuppressLint("DefaultLocale")
        fun bind(cartItem: Cart) {
            Glide.with(itemView.context)
                .load(cartItem.image)
                .into(binding.imgProduct)

            binding.tvProductName.text = cartItem.title
            binding.tvProductPrice.text = String.format("$ %.2f", cartItem.price ?: 0.0)
            binding.tvQuantity.text = cartItem.quantity.toString()  // Show the current quantity
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(cartItems: List<Cart>) {
        this.cartItems = cartItems
        notifyDataSetChanged()
    }

}


