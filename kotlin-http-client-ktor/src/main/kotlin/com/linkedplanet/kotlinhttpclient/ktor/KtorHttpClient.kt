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
package com.linkedplanet.kotlinhttpclient.ktor

import arrow.core.*
import com.google.gson.JsonParser
import com.linkedplanet.kotlinhttpclient.api.http.BaseHttpClient
import com.linkedplanet.kotlinhttpclient.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.*

fun httpClient(username: String, password: String) =
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(BasicAuth) {
            this.username = username
            this.password = password
        }
    }

class KtorHttpClient(
    private val baseUrl: String,
    username: String,
    password: String
) : BaseHttpClient() {

    private var httpClient: HttpClient = httpClient(username, password)

    private fun prepareRequest(
        requestBuilder: HttpRequestBuilder,
        path: String,
        params: Map<String, String>,
        bodyIn: String?,
        contentType: String?
    ) {
        val parsedContentType = contentType
            ?.let { ContentType.parse(it) }
            ?: ContentType.Application.Json
        val parameterString = params
            .takeIf { it.isNotEmpty() }
            ?.let { "?${encodeParams(params)}" }
            ?: ""
        requestBuilder.url("$baseUrl/$path$parameterString")
        requestBuilder.contentType(parsedContentType)
        if (bodyIn != null) {
            requestBuilder.body = JsonParser().parse(bodyIn)
        }
    }

    override suspend fun executeRestCall(
        method: String,
        path: String,
        params: Map<String, String>,
        bodyIn: String?,
        contentType: String?,
        headers: Map<String, String>
    ): Either<DomainError, String> {
        return when (method) {
            "GET" -> {
                httpClient.get<String> {
                    prepareRequest(this, path, params, bodyIn, contentType)
                }.right()
            }
            "POST" -> {
                httpClient.post<String> {
                    prepareRequest(this, path, params, bodyIn, contentType)
                }.right()
            }
            "PUT" -> {
                httpClient.put<String> {
                    prepareRequest(this, path, params, bodyIn, contentType)
                }.right()
            }
            "DELETE" -> {
                httpClient.delete<String> {
                    prepareRequest(this, path, params, bodyIn, contentType)
                }.right()
            }
            else -> {
                DomainError("HTTP-ERROR", "Method '$method' not available").left()
            }
        }
    }

    override suspend fun executeDownload(
        method: String,
        url: String,
        params: Map<String, String>,
        body: String?,
        contentType: String?
    ): Either<DomainError, ByteArray> {
        return httpClient.get<ByteArray> {
            url(url)
        }.right()
    }

    override suspend fun executeUpload(
        method: String,
        url: String,
        params: Map<String, String>,
        mimeType: String,
        filename: String,
        byteArray: ByteArray
    ): Either<DomainError, ByteArray> =
        httpClient.post<ByteArray> {
            url("$baseUrl$url")
            header("Connection", "keep-alive")
            header("Cache-Control", "no-cache")
            body = MultiPartFormDataContent(
                formData {
                    this.append(
                        "file",
                        byteArray,
                        Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=$filename")
                        })
                }
            )
        }.right()


}