package com.example.ecommerce.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.ecommerce.R
import com.example.ecommerce.data.apimodel.ProductItem
import com.example.ecommerce.database.cart.Cart
import com.example.ecommerce.databinding.FragmentDetailBinding
import com.example.ecommerce.databinding.FragmentHomeBinding
import com.example.ecommerce.viewmodel.CartViewModel
import com.example.ecommerce.viewmodel.DetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding

    private val detailViewModel: DetailViewModel by viewModels()
    private val cartViewModel : CartViewModel by viewModels()
    private var item: ProductItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        // Ambil item dari arguments
        item = arguments?.getParcelable("item")

        // Inisialisasi item di ViewModel
        item?.let {
            detailViewModel.initSelectedItem(it)
        }

        // Set data produk ke UI
        setDataProduct()

        // Setup tombol "Add to Cart"
        addToCart()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setDataProduct() {
        item?.let {
            Glide.with(requireContext())
                .load(it.image)
                .into(binding.ivImage)

            binding.tvCategory.text = it.category
            binding.tvNameProduct.text = it.title
            binding.tvTotalPrice.text = String.format("%.2f", it.price)
            binding.tvDescription.text = it.description
            it.rating?.let { rating ->
                binding.tvRatting.text = "${rating.rate ?: "N/A"} / 5 (${rating.count ?: 0} reviews)"
            } ?: run {
                binding.tvRatting.text = "N/A"
            }
        }
    }

    private fun addToCart() {
        binding.btCart.setOnClickListener {

            // Membuat objek Cart dari item yang ditampilkan
            item?.let {
                val cartItem = Cart(
                    id = it.id,
                    category = it.category,
                    description = it.description,
                    image = it.image,
                    price = it.price,
                    title = it.title,
                    quantity = 1,  // Mulai dengan kuantitas 1
                    totalPrice = it.price  // Inisialisasi dengan harga item
                )

                // Panggil addToCart dari CartViewModel
                cartViewModel.addToCart(cartItem)
            }
        }
    }
}


