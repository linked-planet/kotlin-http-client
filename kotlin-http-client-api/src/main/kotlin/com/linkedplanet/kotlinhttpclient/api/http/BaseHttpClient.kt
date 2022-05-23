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

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.linkedplanet.kotlinhttpclient.error.DomainError
import java.lang.reflect.Type
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val GSON: Gson = GsonBuilder().create()

abstract class BaseHttpClient {

    abstract suspend fun executeRestCall(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        headers: Map<String, String> = emptyMap()
    ): Either<DomainError, String>

    abstract suspend fun executeDownload(
        method: String,
        url: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?
    ): Either<DomainError, ByteArray>

    abstract suspend fun executeUpload(
        method: String,
        url: String,
        params: Map<String, String>,
        mimeType: String,
        filename: String,
        byteArray: ByteArray
    ): Either<DomainError, ByteArray>

    suspend fun <T> executeRest(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        returnType: Type
    ): Either<DomainError, T?> =
        executeRestCall(method, path, params, body, contentType).map {
            GSON.fromJson<T>(it, returnType)
        }

    suspend fun <T> executeRestList(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        returnType: Type
    ): Either<DomainError, List<T>> =
        executeRestCall(method, path, params, body, contentType).map { GSON.fromJson(it, returnType) as List<T> }

    suspend fun <T> executeGet(path: String, params: Map<String, String>, returnType: Type): Either<DomainError, T?> =
        executeGetCall(path, params).map { GSON.fromJson<T>(it, returnType) }

    suspend fun <T> executeGetReturnList(
        path: String,
        params: Map<String, String>,
        returnType: Type
    ): Either<DomainError, List<T>?> =
        executeGetCall(path, params).map { GSON.fromJson(it, returnType) as List<T>? }

    suspend fun executeGetCall(path: String, params: Map<String, String>): Either<DomainError, String> =
        executeRestCall("GET", path, params, null, null)

    fun encodeParams(map: Map<String, String>): String {
        return map.map { it.key + "=" + doEncoding(it.value) }.joinToString("&")
    }

    fun doEncoding(str: String): String =
        URLEncoder.encode(str, StandardCharsets.UTF_8.toString())
}