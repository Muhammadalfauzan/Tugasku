package com.example.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.CartAdapter
import com.example.ecommerce.databinding.FragmentCartBinding
import com.example.ecommerce.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter

    private val cartViewModel: CartViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        cartAdapter = CartAdapter(cartViewModel)
        binding.rvCart.setHasFixedSize(true)
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter

        observeCartItems()
        observeTotalPrice()

        return binding.root
    }

    private fun observeTotalPrice() {
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            if (totalPrice != null) {
                binding.tvSumTotal.text = "Rp %.2f".format(totalPrice)
            } else {
                binding.tvSumTotal.text = "Rp 0.00"
            }
        }
    }

    private fun observeCartItems() {
        cartViewModel.allCartItems.observe(viewLifecycleOwner) { cartItems ->
            cartItems?.let {
                cartAdapter.setData(it)
            }
        }
    }

}