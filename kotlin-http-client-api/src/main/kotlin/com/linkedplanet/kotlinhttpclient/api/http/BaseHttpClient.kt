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
import com.linkedplanet.kotlinhttpclient.error.HttpDomainError
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
    ): Either<HttpDomainError, HttpResponse<String>>

    abstract suspend fun executeDownload(
        method: String,
        url: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?
    ): Either<DomainError, HttpResponse<ByteArray>>

    abstract suspend fun executeUpload(
        method: String,
        url: String,
        params: Map<String, String>,
        mimeType: String,
        filename: String,
        byteArray: ByteArray
    ): Either<HttpDomainError, HttpResponse<ByteArray>>

    suspend fun <T> executeRest(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        returnType: Type
    ): Either<HttpDomainError, HttpResponse<T?>> =
        executeRestCall(method, path, params, body, contentType).map {
            HttpResponse(
                it.statusCode,
                GSON.fromJson<T>(it.body, returnType)
            )
        }

    suspend fun <T> executeRestList(
        method: String,
        path: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?,
        returnType: Type
    ): Either<HttpDomainError, HttpResponse<List<T>>> =
        executeRestCall(method, path, params, body, contentType).map {
            HttpResponse(
                it.statusCode,
                GSON.fromJson(it.body, returnType) as List<T>
            )
        }

    suspend fun <T> executeGet(
        path: String,
        params: Map<String, String>,
        returnType: Type
    ): Either<HttpDomainError, HttpResponse<T?>> =
        executeGetCall(path, params).map {
            HttpResponse(
                it.statusCode,
                GSON.fromJson<T>(it.body, returnType)
            )
        }

    suspend fun <T> executeGetReturnList(
        path: String,
        params: Map<String, String>,
        returnType: Type
    ): Either<HttpDomainError, HttpResponse<List<T>?>> =
        executeGetCall(path, params).map {
            HttpResponse(
                it.statusCode,
                GSON.fromJson(it.body, returnType) as List<T>?
            )
        }

    suspend fun executeGetCall(
        path: String,
        params: Map<String, String>
    ): Either<HttpDomainError, HttpResponse<String>> =
        executeRestCall("GET", path, params, null, null)

    fun encodeParams(map: Map<String, String>): String {
        return map.map { it.key + "=" + doEncoding(it.value) }.joinToString("&")
    }

    fun doEncoding(str: String): String =
        URLEncoder.encode(str, StandardCharsets.UTF_8.toString())
}