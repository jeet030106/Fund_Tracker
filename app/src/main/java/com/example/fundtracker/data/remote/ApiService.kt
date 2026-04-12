package com.example.fundtracker.data.remote

import com.example.fundtracker.data.model.FundDetailsResponse
import com.example.fundtracker.data.model.FundSearchResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {


    @GET("mf/search")
    suspend fun searchFunds(
        @Query("q") query: String
    ): List<FundSearchResult>


    @GET("mf/{schemeCode}")
    suspend fun getFundDetails(
        @Path("schemeCode") schemeCode: Int
    ): FundDetailsResponse

    @GET("mf")
    suspend fun getAllFunds(
        @Query("page") page: Int,
        @Query("limit") limit: Int

    ): List<FundSearchResult>

}