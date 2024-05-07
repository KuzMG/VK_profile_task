package com.example.myapplication.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.Transition
import androidx.transition.TransitionSet
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.R
import com.example.myapplication.api.Product
import com.example.myapplication.databinding.FragmentProductBinding
import com.example.myapplication.getParcelableCompat
import com.example.myapplication.ui.image.ImageFragment


class ProductFragment : Fragment() {
    companion object {
        private const val PRODUCT_KEY = "product"
        fun newInstance(product: Product) = ProductFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PRODUCT_KEY, product)
            }
            sharedElementEnterTransition = getTransition()
        }

        private fun getTransition(): Transition {
            val set = TransitionSet()
            set.ordering = TransitionSet.ORDERING_TOGETHER
            set.addTransition(ChangeBounds())
            set.addTransition(ChangeImageTransform())
            set.addTransition(ChangeTransform())
            return set
        }

    }

    private lateinit var binding: FragmentProductBinding
    private val viewModel: ProductViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.product = arguments?.getParcelableCompat(PRODUCT_KEY)!!
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_product, container, false)
        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.title = viewModel.product.title
        }
        ViewCompat.setTransitionName(binding.thumbnailViewPager,viewModel.product.id.toString())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.product = viewModel.product
        binding.stockTextView.text = getString(R.string.stock, viewModel.product.stock.toString())
        binding.categoryTextView.text = getString(R.string.category, viewModel.product.category)
        binding.priceTextView.text = getString(R.string.price, viewModel.product.price.toString())
        binding.brandTextView.text = getString(R.string.brand, viewModel.product.brand)
        binding.thumbnailViewPager.adapter =
            ViewPagerAdapter(requireActivity(), viewModel.product.images.reversed())
    }

    override fun onStart() {
        super.onStart()
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    class ViewPagerAdapter(fragmentActivity: FragmentActivity, private val list: List<String>) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return ImageFragment.newInstance(list[position])
        }

        override fun getItemCount() = list.size
    }

}