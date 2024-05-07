package com.example.myapplication.data_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.api.Product
import com.example.myapplication.api.ProductApi

class ProductDataSource(private val productApi: ProductApi) : PagingSource<Int, Product>() {
    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try{
            val position = params.key ?: 0
            val response = productApi.getProducts(position*20,20)
            val products = response.body()?.products ?: emptyList()
            LoadResult.Page(
                data = products,
                prevKey = if (position == 0) null else position - 1,
                nextKey = position + 1
            )
        } catch (e: Exception){
            LoadResult.Error(e)
        }
    }
}