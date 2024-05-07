package com.example.myapplication.api

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.myapplication.data_source.ProductCategoryDataSource
import com.example.myapplication.data_source.ProductDataSource
import com.example.myapplication.data_source.ProductQueryDataSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ProductService {
    private val productApi: ProductApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        productApi = retrofit.create()
    }

    fun getCategories() = productApi.getCategories()

    fun getProducts(category: String): LiveData<PagingData<Product>> {
            return Pager(config = PagingConfig(pageSize = 1)) {
                if(category=="uncategorized"){
                    ProductDataSource(productApi)
                } else{
                    ProductCategoryDataSource(productApi,category)
                }

    }.liveData
}

fun getProductsQuery(query: String): LiveData<PagingData<Product>> {
    return Pager(config = PagingConfig(pageSize = 1)) {
        ProductQueryDataSource(productApi, query)
    }.liveData
}
}