package com.jlccaires.marvelguys.data.api

class MarvelRepository(private val api: MarvelAPI) {

    fun listCharacters(offset: Int = 0, name: String? = null) = api.listCharacters(offset, name)
}