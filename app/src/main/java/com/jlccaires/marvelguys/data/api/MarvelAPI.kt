package com.jlccaires.marvelguys.data.api

import com.jlccaires.marvelguys.data.api.dto.CharacterResponseDto
import com.jlccaires.marvelguys.data.api.dto.ComicResponseDto
import com.jlccaires.marvelguys.data.api.dto.SerieResponseDto
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MarvelAPI {

    @GET("characters")
    fun listCharacters(@Query("offset") offset: Int, @Query("nameStartsWith") name: String?): Single<CharacterResponseDto>

    @GET("characters/{characterId}")
    fun getCharacter(@Path("characterId") characterId: Int): Single<CharacterResponseDto>

    @GET("characters/{characterId}/series?limit=100")
    fun getSeries(@Path("characterId") characterId: Int, @Query("offset") offset: Int): Single<SerieResponseDto>

    @GET("characters/{characterId}/comics?limit=100")
    fun getComics(@Path("characterId") characterId: Int, @Query("offset") offset: Int): Single<ComicResponseDto>
}