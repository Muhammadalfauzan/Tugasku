package com.example.ecommerce.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.R
import com.example.ecommerce.adapter.CartAdapter
import com.example.ecommerce.databinding.FragmentCartBinding
import com.example.ecommerce.viewmodel.CartViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter

    private val cartViewModel: CartViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)

        cartAdapter = CartAdapter(cartViewModel)
        binding.rvCart.setHasFixedSize(true)
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter

        checkCart()
        observeCartItems()
        observeTotalPrice()
        handleBackNavigation()
        enableSwipeToDelete(binding.rvCart, cartAdapter)
        return binding.root
    }

    private fun checkCart() {
        cartViewModel.allCartItems.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvAwe.visibility = View.VISIBLE
                binding.tvNoItem.visibility = View.VISIBLE
                binding.ivEmpty.visibility = View.VISIBLE
                binding.rvCart.visibility = View.GONE
            } else {
                binding.tvAwe.visibility = View.GONE
                binding.tvNoItem.visibility = View.GONE
                binding.ivEmpty.visibility = View.GONE
                binding.rvCart.visibility = View.VISIBLE
                cartAdapter.setData(it)
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeTotalPrice() {
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            if (totalPrice != null) {
                binding.tvSumTotal.text = "%.2f".format(totalPrice)
            } else {
                binding.tvSumTotal.text = "0.00"
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

    private fun enableSwipeToDelete(recyclerView: RecyclerView, adapter: CartAdapter) {
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val cartItem = adapter.getCartItemAt(position)

                // Remove the item from the cart
                cartViewModel.deleteCartItemById(cartItem.id.toLong())
                adapter.notifyItemRemoved(position)

                Snackbar.make(recyclerView, "Item removed from the cart", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        cartViewModel.addToCart(cartItem)
                        adapter.notifyItemInserted(position)
                    }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    private fun handleBackNavigation() {
        val navController = findNavController()
        requireActivity()
            .onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                navController.navigate(R.id.action_cartFragment_to_homeFragment)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}