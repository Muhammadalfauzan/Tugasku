package com.example.ecommerce.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
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

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private val productViewModel: ProductViewModel by viewModels()

    private var recyclerViewState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupBanner()
        setupRecyclerViews()
        observeCategories()
        observeProducts()
        readDataProduct()
        onBackPressed()
        setupSearchButton()
        productViewModel.getCategory()
        productViewModel.getListMenu()

        return binding.root
    }
    private fun setupSearchButton() {
        binding.searchView.setOnClickListener {
            /*throw RuntimeException("Test Crash") // Force a crash*/
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }
    private fun setupRecyclerViews() {
        _binding?.let { binding ->
            categoryAdapter = CategoryAdapter()
            binding.rvHorizontal.apply {
                adapter = categoryAdapter
                val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.rvHorizontal.layoutManager = layoutManager
            }

            productAdapter = ProductAdapter(this)
            val layoutManager = GridLayoutManager(context, 2)
            binding.rvVertical.apply {
                this.layoutManager = layoutManager
                adapter = productAdapter
                setHasFixedSize(true)
                setItemViewCacheSize(20)
                itemAnimator = null
            }

            recyclerViewState?.let {
                layoutManager.onRestoreInstanceState(it)
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
                    response.data?.let { products ->
                        productAdapter.setData(products)

                        recyclerViewState?.let {
                            val layoutManager = _binding?.rvVertical?.layoutManager as GridLayoutManager
                            layoutManager.onRestoreInstanceState(it)
                        }
                    }
                }
                is NetworkResult.Error -> {
                    loadMenuFromCache()
                    Log.e("HomeFragment", "Error fetching products: ${response.message}")
                    Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeCategories() {
        _binding?.let {
            productViewModel.categoryResponse.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is NetworkResult.Loading -> {
                        Log.d("HomeFragment", "Loading categories...")
                    }
                    is NetworkResult.Success -> {
                        response.data?.let { categories ->
                            categoryAdapter.setData(categories)
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e("HomeFragment", "Error fetching categories: ${response.message}")
                        Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadMenuFromCache() {
        _binding?.let {
            productViewModel.getConvertedProductItems().observe(viewLifecycleOwner) { products ->
                if (products.isNotEmpty()) {
                    productAdapter.setData(products) // Data sudah dikonversi menjadi ProductItem
                    recyclerViewState?.let {
                        val layoutManager = binding.rvVertical.layoutManager as GridLayoutManager
                        layoutManager.onRestoreInstanceState(it)
                    }
                }
            }
        }
    }

    private fun readDataProduct() {
        _binding?.let {
            lifecycleScope.launch {
                productViewModel.getConvertedProductItems().observe(viewLifecycleOwner) { products ->
                    if (products.isNotEmpty()) {
                        productAdapter.setData(products) // Data sudah dikonversi menjadi ProductItem
                        recyclerViewState?.let {
                            val layoutManager = binding.rvVertical.layoutManager as GridLayoutManager
                            layoutManager.onRestoreInstanceState(it)
                        }
                    }
                }
            }
        }
    }

    private fun setupBanner() {
        Handler(Looper.getMainLooper()).postDelayed({
            _binding?.let { binding ->
                val imgSlider = binding.bannerLayout.imageSlider
                imgSlider?.let {
                    val slides = ArrayList<SlideModel>()
                    slides.add(SlideModel(R.drawable.banner_shoes))
                    slides.add(SlideModel(R.drawable.banner_shoes))
                    slides.add(SlideModel(R.drawable.banner_shoes))
                    it.setImageList(slides, ScaleTypes.FIT)
                    it.visibility = View.VISIBLE
                }
            }
        }, 1000)
    }

    override fun onItemClick(data: ProductItem) {
        recyclerViewState = binding.rvVertical.layoutManager?.onSaveInstanceState()
        Log.d("Item clicked", "Product item clicked")
        val bundle = bundleOf("item" to data)
        findNavController().navigate(R.id.action_homeFragment_to_detailFragment, bundle)
    }

    private fun onBackPressed() {
        val navController = findNavController()
        requireActivity()
            .onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    requireActivity().finish()
                } else {
                    navController.navigateUp()
                }
            }
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = binding.rvVertical.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        recyclerViewState?.let {
            binding.rvVertical.layoutManager?.onRestoreInstanceState(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Set binding to null to prevent memory leaks
    }
}