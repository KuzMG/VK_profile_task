package com.example.myapplication.ui.product_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.myapplication.api.Product
import com.example.myapplication.api.ProductService

class ProductListViewModel : ViewModel() {
    private val mutableSearchQuery = MutableLiveData<Pair<String, String>>()
    private val productService: ProductService = ProductService()
    val productItemLiveData: LiveData<PagingData<Product>>
    var listCategory = listOf<String>()
    var category: String = "uncategorized"
        private set
    var query: String = ""
        private set

    init {
        mutableSearchQuery.value = "" to category
        productItemLiveData = mutableSearchQuery.switchMap { query ->
            if (query.first.isBlank()) {
                productService.getProducts(query.second).cachedIn(this)
            } else {
                productService.getProductsQuery(query.first).cachedIn(this)
            }
        }

    }

    fun getCategories() = productService.getCategories()

    fun setQuery(query: String = "", category: String = "uncategorized") {
        this.category = category
        this.query = query
        mutableSearchQuery.value = query to category
    }
}