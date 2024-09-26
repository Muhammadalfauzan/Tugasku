package com.example.ecommerce.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.databinding.FragmentSearchBinding
import com.example.ecommerce.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()

        val imageUri = arguments?.getParcelable<Uri>("imageUri")

        imageUri?.let {
            // Jika ada gambar, tampilkan atau proses gambar
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            productViewModel.analyzeImageWithMLKit(bitmap)
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()  // Kembali ke fragment sebelumnya
        }
        // Observe hasil pencarian produk
        productViewModel.searchResultResponse.observe(viewLifecycleOwner) { products ->
            productAdapter.setData(products)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.rvSearch.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}

