package com.example.myapplication.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("/products")
    suspend fun getProducts(@Query("skip") skip: Int, @Query("limit") limit: Int): Response<ProductResponse>

    @GET("/products/search")
    suspend fun getProductsQuery(@Query("q") query: String,@Query("skip") skip: Int, @Query("limit") limit: Int): Response<ProductResponse>

    @GET("/products/category/{category}")
    suspend fun getProductsCategory(@Path("category") category: String, @Query("skip") skip: Int, @Query("limit") limit: Int): Response<ProductResponse>


    @GET("/products/categories")
    fun getCategories(): Call<List<String>>
}