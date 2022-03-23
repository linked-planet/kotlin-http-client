package com.linkedplanet.kotlinhttpclient.error

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.gson.Gson
import com.google.gson.GsonBuilder

val GSON: Gson = GsonBuilder().create()

fun <T> recursiveRestCall(start: Int = 0, max: Int? = null, call: (Int, Int) -> Either<DomainError, List<T>>): Either<DomainError, List<T>> {
    var index = start
    val maxResults = 1
    val elements = mutableListOf<T>()
    var nextPage = false
    do {
        val tmpElements: List<T> = call(index, maxResults).getOrHandle {
            return@recursiveRestCall it.left()
        }
        elements.addAll(tmpElements)
        nextPage = tmpElements.size >= maxResults
        index = index + tmpElements.size
    } while (nextPage && (max == null || index <= max))
    return elements.right()
}