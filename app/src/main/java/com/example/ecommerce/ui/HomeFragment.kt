package com.example.ecommerce.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerce.ItemDecorationRv
import com.example.ecommerce.R
import com.example.ecommerce.adapter.CategoryAdapter
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.data.apimodel.ProductItem
import com.example.ecommerce.databinding.FragmentHomeBinding
import com.example.ecommerce.utils.NetworkResult
import com.example.ecommerce.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), ProductAdapter.OnItemClickListener {

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
        observeProducts()
        readDataProduct()
        productViewModel.getCategory() // Call function to fetch categories
        productViewModel.getListMenu() // Call function to fetch product list

        setupBanner()
        return binding.root
    }

    private fun setupRecyclerViews() {
        // Setup for RecyclerView categories
        categoryAdapter = CategoryAdapter()
        binding.rvHorizontal.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        productAdapter = ProductAdapter(this) // Pass the fragment itself to handle clicks
        // Setup for RecyclerView products
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_16dp)
        binding.rvVertical.apply {
            layoutManager = GridLayoutManager(context, 2) // Set Grid layout with 2 columns
            adapter = productAdapter
            addItemDecoration(ItemDecorationRv(2, spacingInPixels, true)) // Add item decoration
        }

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
                        categoryAdapter.setData(categories) // Pass category list to adapter
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
                        productAdapter.setData(products) // Pass product list to adapter
                    }
                }
                is NetworkResult.Error -> {
                    Log.e("HomeFragment", "Error fetching products: ${response.message}")
                    Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun readDataProduct() {
        Log.d("Read database", "Read menu database called")
        lifecycleScope.launch {
            productViewModel.readProduct.observe(viewLifecycleOwner) { database ->
                if (database.isNotEmpty()) {
                    productAdapter.setData(database.first().listProductResponse)
                } else {
                    // Optionally request data from API if database is empty
                    // requestMenuFromApi()
                }
            }
        }
    }

    private fun setupBanner() {
        Handler(Looper.getMainLooper()).postDelayed({
            // Akses ImageSlider langsung melalui binding
            val imgSlider = binding.bannerLayout.imageSlider // Akses langsung melalui ViewBinding
            imgSlider?.let {
                val slides = ArrayList<SlideModel>()
                slides.add(SlideModel(R.drawable.banner_shoes))
                slides.add(SlideModel(R.drawable.banner_shoes))
                slides.add(SlideModel(R.drawable.banner_shoes))
                it.setImageList(slides, ScaleTypes.FIT)
                it.visibility = View.VISIBLE
            }
        }, 1000)
    }
    override fun onItemClick(data: ProductItem) {
        Log.d("Item clicked", "Product item clicked")
        val bundle = bundleOf("item" to data)
        findNavController().navigate(R.id.action_homeFragment_to_detailFragment, bundle)
    }
}
