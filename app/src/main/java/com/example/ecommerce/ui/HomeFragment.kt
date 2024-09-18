package com.example.ecommerce.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.ItemDecorationRv
import com.example.ecommerce.R
import com.example.ecommerce.adapter.CategoryAdapter
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.databinding.FragmentHomeBinding
import com.example.ecommerce.utils.NetworkResult
import com.example.ecommerce.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerViews()
        observeCategories()
        observeProducts() // Tambahkan observasi untuk produk
        productViewModel.getCategory() // Memanggil fungsi untuk mengambil kategori
        productViewModel.getListMenu() // Memanggil fungsi untuk mengambil daftar produk

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Setup untuk RecyclerView kategori
        categoryAdapter = CategoryAdapter()
        binding.rvHorizontal.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        productAdapter = ProductAdapter()
        // Setup untuk RecyclerView produk
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_16dp)
        binding.rvVertical.apply {
            layoutManager = GridLayoutManager(context, 2) // Mengatur Grid dengan 2 kolom
            adapter = productAdapter
            addItemDecoration(ItemDecorationRv(2, spacingInPixels, true)) // Menambahkan dekorasi jarak antar item
        }
     /*
        binding.rvVertical.apply { // Pastikan rvProducts ada di layout XML
            adapter = productAdapter
            layoutManager = GridLayoutManager(context, 2) // Layout vertikal untuk produk
        }*/

        Log.d("HomeFragment", "RecyclerView Adapters attached: ${binding.rvHorizontal.adapter != null} ${binding.rvVertical.adapter != null}")
    }

    private fun observeCategories() {
        productViewModel.categoryResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Loading -> {
                    Log.d("HomeFragment", "Loading categories...")
                }
                is NetworkResult.Success -> {
                    Log.d("HomeFragment", "Categories received successfully")
                    response.data?.let { categories ->
                        Log.d("HomeFragment", "Categories: $categories")
                        categoryAdapter.setData(categories) // Kirim daftar kategori ke adapter
                    }
                }
                is NetworkResult.Error -> {
                    Log.e("HomeFragment", "Error fetching categories: ${response.message}")
                    Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeProducts() {
        productViewModel.listMenuResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Loading -> {
                    Log.d("HomeFragment", "Loading products...")
                }
                is NetworkResult.Success -> {
                    Log.d("HomeFragment", "Products received successfully")
                    response.data?.let { products ->
                        Log.d("HomeFragment", "Products: $products")
                        productAdapter.setData(products) // Kirim daftar produk ke adapter
                    }
                }
                is NetworkResult.Error -> {
                    Log.e("HomeFragment", "Error fetching products: ${response.message}")
                    Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
