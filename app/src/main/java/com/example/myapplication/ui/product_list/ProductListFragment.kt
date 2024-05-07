package com.example.myapplication.ui.product_list

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.api.Product
import com.example.myapplication.databinding.FragmentProductListBinding
import com.example.myapplication.ui.product.ProductFragment
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductListFragment : Fragment() {

    private lateinit var binding: FragmentProductListBinding
    private val viewModel: ProductListViewModel by viewModels()
    private val adapter = Adapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_product_list,
            container,
            false
        )
        binding.recyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.listCategory
        )
        binding.spinner.setSelection(viewModel.listCategory.indexOf(viewModel.category))
        viewModel.productItemLiveData.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
        viewModel.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(p0: Call<List<String>>, p1: Response<List<String>>) {
                val list = p1.body()?.toMutableList() ?: mutableListOf()
                list.add(0, "uncategorized")
                binding.spinner.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    list
                )
                viewModel.listCategory = list.toList()
                binding.spinner.setSelection(list.indexOf(viewModel.category))
            }

            override fun onFailure(p0: Call<List<String>>, p1: Throwable) {

            }

        })

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.product_list_fragment, menu)
                val searchItem = menu.findItem(R.id.menu_item_search)
                val searchView = searchItem.actionView as SearchView
                searchView.setQuery(viewModel.query, false)
                searchView.apply {
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(p0: String): Boolean {
                            return true
                        }

                        override fun onQueryTextChange(p0: String): Boolean {
                            if (viewModel.query != p0) {
                                viewModel.setQuery(p0)
                                binding.spinner.setSelection(0)
                            }
                            return true
                        }
                    })
                }


            }

            override fun onMenuItemSelected(menuItem: MenuItem) = false

        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    override fun onStart() {
        super.onStart()
        adapter.addLoadStateListener { loadState ->
            binding.loadState = loadState
            val errorState = when {
                loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                else -> null
            }
            errorState?.let {
                Toast.makeText(context, it.error.toString(), Toast.LENGTH_LONG).show()
            }
        }

        binding.refreshButton.setOnClickListener {
            adapter.refresh()
        }

        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val cat = parent?.getItemAtPosition(position) as String
                if (viewModel.category != cat)
                    viewModel.setQuery(category = cat)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        val cm =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    adapter.retry()
                }
            }
        )
    }


    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        OnClickListener {
        lateinit var item: Product
        private val imageView = itemView.findViewById<ImageView>(R.id.thumbnail_image_view)
        private val cardView = itemView.findViewById<CardView>(R.id.thumbnail_card_view)
        private val titleTextView = itemView.findViewById<TextView>(R.id.title_text_view)
        private val descriptionTextView =
            itemView.findViewById<TextView>(R.id.description_text_view)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Product) {
            ViewCompat.setTransitionName(cardView, item.id.toString())
            this.item = item
            titleTextView.text = item.title
            descriptionTextView.text = item.description
            Picasso.get().load(item.thumbnail).into(imageView)
        }

        override fun onClick(v: View) {
            parentFragmentManager.beginTransaction()
                .addSharedElement(cardView, item.id.toString())
                .replace(R.id.fragment_container, ProductFragment.newInstance(item))
                .addToBackStack(null)
                .commit()
        }
    }

    private inner class Adapter :
        PagingDataAdapter<Product, ViewHolder>(object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Product, newItem: Product) =
                oldItem == newItem

        }) {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position)!!)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(layoutInflater.inflate(R.layout.list_item, parent, false))


    }
}