package com.rei.flowroomdemo

import retrofit2.http.Url

import okhttp3.ResponseBody

import retrofit2.http.GET

import retrofit2.http.Streaming


interface Repository {
    @Streaming
    @GET
    suspend fun downloadFileWithDynamicUrlAsync(@Url fileUrl: String): ResponseBody?
}