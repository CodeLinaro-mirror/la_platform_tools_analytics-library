/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.analytics

import com.google.common.base.Charsets
import com.google.common.util.concurrent.SettableFuture
import com.google.wireless.android.play.playlog.proto.ClientAnalytics
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream

/** A tiny webserver used to stub out the Google Analytics server in tests. */
class ServerStub
/**
 * Creates an instance of the webserver and starts listening on an unused port in the ephemeral
 * range.
 */
@Throws(IOException::class)
constructor() : HttpHandler, AutoCloseable {
  private val results_ = ArrayList<Future<ClientAnalytics.LogRequest>>()
  private val address: InetSocketAddress
  private val server: HttpServer = HttpServer.create(InetSocketAddress(0), 0)
  private val nextResponseServerError = AtomicBoolean(false)

  /** Builds a url for the server stub that can be used in testing [AnalyticsPublisher]. */
  val url: URL
    @Throws(MalformedURLException::class)
    get() = URL(String.format("http://localhost:%d/log?format=raw", address.port))

  /**
   * Gets results for calls to this webserver since it was started. The future represents successful
   * (a [ClientAnalytics.LogRequest]) and failed (an exception) requests.
   */
  // Synchronized to ensure no results are in flight to avoid test flakeyness.
  val results: List<Future<ClientAnalytics.LogRequest>>
    get() =
      synchronized(server) {
        return results_
      }

  init {
    server.createContext("/log", this)
    server.executor = Executors.newScheduledThreadPool(10)
    server.start()

    this.address = server.address
  }

  /**
   * iff true, instructs the webserver to send an internal server error as the response to the next
   * request made to this server.
   */
  fun makeNextResponseServerError(nextRequestBad: Boolean) {
    this.nextResponseServerError.set(nextRequestBad)
  }

  override fun close() {
    server.stop(0)
  }

  @Throws(IOException::class)
  override fun handle(httpExchange: HttpExchange) {
    // Synchronized to ensure no results are in flight to avoid test flakeyness.
    synchronized(server) {
      if (nextResponseServerError.get()) {
        val response = "Internal Server Error".toByteArray(Charsets.UTF_8)
        httpExchange.sendResponseHeaders(HTTP_INTERNAL_SERVER_ERROR, response.size.toLong())
        val body = httpExchange.responseBody
        body.write(response)
        nextResponseServerError.set(false)
      } else {
        val data = SettableFuture.create<ClientAnalytics.LogRequest>()
        try {
          var body = httpExchange.requestBody
          if (isZipped(httpExchange)) {
            body = GZIPInputStream(body)
          }
          data.set(ClientAnalytics.LogRequest.parseFrom(body))
          httpExchange.sendResponseHeaders(HTTP_OK, 0)
        } catch (e: IOException) {
          val response = "Bad Request".toByteArray(Charsets.UTF_8)
          httpExchange.sendResponseHeaders(HTTP_BAD_REQUEST, response.size.toLong())
          val body = httpExchange.responseBody
          body.write(response)
          data.setException(e)
        }

        results_.add(data)
      }
    }
  }

  /** Checks if the request body is gzipped. */
  private fun isZipped(httpExchange: HttpExchange): Boolean {
    if (!httpExchange.requestHeaders.containsKey("Content-Encoding")) {
      return false
    }
    val values = httpExchange.requestHeaders["Content-Encoding"]!!
    return if (values.size == 0) {
      false
    } else values[values.size - 1] == "gzip"
  }

  companion object {
    const val HTTP_OK = 200
    const val HTTP_BAD_REQUEST = 404
    const val HTTP_INTERNAL_SERVER_ERROR = 500
  }
}
