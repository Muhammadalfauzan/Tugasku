package com.example.ecommerce.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.ecommerce.R
import com.example.ecommerce.data.apimodel.ProductItem
import com.example.ecommerce.database.cart.Cart
import com.example.ecommerce.databinding.FragmentDetailBinding
import com.example.ecommerce.viewmodel.CartViewModel
import com.example.ecommerce.viewmodel.DetailViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding

    private val detailViewModel: DetailViewModel by viewModels()
    private val cartViewModel : CartViewModel by viewModels()
    private var item: ProductItem? = null
    private lateinit var snackBar: Snackbar

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
        iconBackClicked()
        return binding.root
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setDataProduct() {
        item?.let {
            // Menggunakan Glide untuk memuat gambar produk ke dalam ImageView
            Glide.with(requireContext())
                .load(it.image)
                .into(binding.ivImage)

            binding.tvCategory.text = it.category
            binding.tvNameProduct.text = it.title
            binding.tvTotalPrice.text = String.format("%.2f", it.price)
            binding.tvDescription.text = it.description
            it.rating.let { rating ->
                binding.tvRatting.text = "${rating.rate ?: "N/A"} / 5 (${rating.count ?: 0} reviews)"
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
                    quantity = 1,
                    totalPrice = it.price
                )

                // Panggil addToCart dari CartViewModel
                cartViewModel.addToCart(cartItem)

                showItemAddedSnackBar()
            }
        }
    }

    private fun iconBackClicked() {
        binding.ivBack.setOnClickListener {
            // Menggunakan NavController untuk kembali ke fragment sebelumnya
            findNavController().navigateUp()
        }
    }

  /*  @Suppress("DEPRECATION")
    private fun iconBackClicked() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }*/

    @SuppressLint("InflateParams")
    private fun showItemAddedSnackBar() {
        val inflater = LayoutInflater.from(requireContext())
        val customView = inflater.inflate(R.layout.item_added_snackbar, null)

        snackBar = Snackbar.make(binding.btCart, "", Snackbar.LENGTH_SHORT)
        val snackBarView = snackBar.view

        val snackBarLayout = snackBarView as ViewGroup
        snackBarLayout.removeAllViews() // Clear existing views
        snackBarLayout.addView(customView) // Add custom view

        customView.setOnClickListener {
            val action = DetailFragmentDirections.actionDetailFragmentToCartFragment()
            findNavController().navigate(action)
            snackBar.dismiss()
        }

        snackBar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this::snackBar.isInitialized && snackBar.isShown) {
            snackBar.dismiss()  // Jika SnackBar aktif, maka disembunyikan saat fragment dihancurkan
        }
        detailViewModel.setCurrentAmount(1) // Mengatur jumlah item ke 1 saat tampilan hancur
        item?.let { detailViewModel.clearTotalPrice(it.price) }  // Menghapus total harga untuk item yang ditampilkan
    }
}


