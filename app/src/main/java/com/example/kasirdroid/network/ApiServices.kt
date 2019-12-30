package com.example.kasirdroid.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiServices {
    @FormUrlEncoded
    @POST("api.php")
    fun verifikasi(
        @Field("verifikasi") status: String,
        @Field("pin") pin : String,
        @Field("id") id : String) : Call<String>

    companion object {
        val URL = "http://apipos.miebledek.com/"
    }
}