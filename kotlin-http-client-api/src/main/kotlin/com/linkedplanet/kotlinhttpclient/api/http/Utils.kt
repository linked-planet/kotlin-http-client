/**
 * Copyright 2022 linked-planet GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedplanet.kotlinhttpclient.api.http

import arrow.core.*
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError

suspend fun <T> recursiveRestCall(
    start: Int = 0,
    max: Int? = null,
    call: suspend (Int, Int) -> Either<HttpDomainError, List<T>>
): Either<HttpDomainError, List<T>> {
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
        index += tmpElements.size
    } while (nextPage && (max == null || index <= max))
    return elements.right()
}