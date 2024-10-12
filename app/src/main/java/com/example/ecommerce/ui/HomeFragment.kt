package com.example.ecommerce.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.ecommerce.viewmodel.HomeViewModel
import com.example.ecommerce.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class HomeFragment : Fragment(), ProductAdapter.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private val productViewModel: ProductViewModel by viewModels()
    private var recyclerViewState: Parcelable? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupBanner()
        setupRecyclerViews()
        observeCategories()
        observeProducts()
        readDataProduct()
        searchButton()


        registerNetworkCallback()
        productViewModel.getCategory()
        productViewModel.getListMenu()
        onBackPressed()
        observeUserDisplayName()

        return binding.root
    }

    private fun observeUserDisplayName() {
        homeViewModel.userDisplayName.observe(viewLifecycleOwner) { displayName ->
            binding.textView2.text = displayName
        }
    }

    private fun searchButton() {
        binding.searchView.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data

            // Kirim URI gambar ke SearchFragment menggunakan Bundle
            val bundle = Bundle().apply {
                putParcelable("imageUri", imageUri)
            }

            // Navigasi ke SearchFragment dan kirim bundle
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }
    }

    private fun setupRecyclerViews() {
        _binding?.let { binding ->
            categoryAdapter = CategoryAdapter()
            binding.rvHorizontal.apply {
                adapter = categoryAdapter
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
        showShimmerCategory()
    }

    private fun observeProducts() {
        productViewModel.listMenuResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Loading -> {
                    productAdapter.showShimmerEffect()
                    Log.d("HomeFragment", "Loading products...")
                }

                is NetworkResult.Success -> {
                    productAdapter.hideShimmerEffect()
                    response.data?.let { products ->
                        productAdapter.setData(products)

                        recyclerViewState?.let {
                            val layoutManager =
                                _binding?.rvVertical?.layoutManager as GridLayoutManager
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
                        showShimmerCategory()
                        Log.d("HomeFragment", "Loading categories...")
                    }

                    is NetworkResult.Success -> {
                        hideShimmerCategory()
                        response.data?.let { categories ->
                            categoryAdapter.setData(categories)
                        }
                    }

                    is NetworkResult.Error -> {
                        Log.e("HomeFragment", "Error fetching categories: ${response.message}")
                        Toast.makeText(context, response.message ?: "Error", Toast.LENGTH_SHORT)
                            .show()
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
                productViewModel.getConvertedProductItems()
                    .observe(viewLifecycleOwner) { products ->
                        if (products.isNotEmpty()) {
                            productAdapter.setData(products) // Data sudah dikonversi menjadi ProductItem
                            recyclerViewState?.let {
                                val layoutManager =
                                    binding.rvVertical.layoutManager as GridLayoutManager
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
                imgSlider.let {
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

    private fun showShimmerCategory() {
        binding.shimmerCategory.visibility = View.VISIBLE
        binding.shimmerCategory.startShimmer()
        binding.rvHorizontal.visibility = View.GONE
    }

    private fun hideShimmerCategory() {
        binding.shimmerCategory.stopShimmer()
        binding.shimmerCategory.visibility = View.GONE
        binding.rvHorizontal.visibility = View.VISIBLE
    }

    // Handle network disconnect
    private fun registerNetworkCallback() {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Fetch ulang data hanya ketika internet kembali
                if (productViewModel.categoryResponse.value is NetworkResult.Error || productViewModel.listMenuResponse.value is NetworkResult.Error) {
                    Log.d("NetworkCallback", "Internet kembali, fetch ulang data.")
                    productViewModel.fetchDataOnConnectionAvailable()
                } else {
                    Log.d("NetworkCallback", "Internet kembali, tetapi data sudah ada.")
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
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
        // Unregister network callback saat fragment dihancurkan (opsional)
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
        _binding = null
    }
}