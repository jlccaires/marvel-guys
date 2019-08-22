package com.jlccaires.marvelguys.data.api

import com.jlccaires.marvelguys.data.api.dto.ResponseDto
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface MarvelAPI {
    @GET("characters")
    fun listCharacters(@Query("offset") offset: Int, @Query("nameStartsWith") name: String?): Single<ResponseDto>
}