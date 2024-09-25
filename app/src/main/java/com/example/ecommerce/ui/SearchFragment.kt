package com.example.ecommerce.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.databinding.FragmentSearchBinding
import com.example.ecommerce.utils.NetworkResult
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

        productViewModel.searchResultResponse.observe(viewLifecycleOwner) { products ->
            productAdapter.setData(products)
        }

        binding.buttonSearch.setOnClickListener {
            pickImageFromGallery()
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

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            productViewModel.analyzeImageWithMLKit(bitmap)
        }
    }
}

