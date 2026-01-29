/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.analytics.stubs

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * A tiny webserver used to test [WebServerDateProvider] Cannot use the [com.sun.net.httpserver.HttpServer] API as that autogenerates the
 * Date header.
 */
class StubDateWebServer {
  private val pattern = "EEE, dd MMM yyyy HH:mm:ss zzz"
  private val gmtTZ = TimeZone.getTimeZone("GMT")
  private val dateFormat = SimpleDateFormat(pattern, Locale.US).apply { timeZone = gmtTZ }

  private val server = ServerSocket(0)
  private val executor = Executors.newSingleThreadExecutor()

  val url = URL("http://localhost:${server.localPort}")

  private var reply =
    """
    HTTP/1.1 200 OK
    Date: Fri, 03 Aug 2018 19:59:10 GMT

    Hello World
    """
      .trimIndent()

  init {

    executor.submit {
      while (true) {
        val client = server.accept()
        val reader = BufferedReader(InputStreamReader(client.inputStream))
        loop@ while (true) {
          val line = reader.readLine()
          println("JVG: '$line'")
          when {
            line == "" -> break@loop
          }
        }
        var writer = OutputStreamWriter(client.outputStream)
        writer.write(reply)
        writer.close()
        client.close()
      }
    }
  }

  fun replyForDate(date: Date) {
    val formatted = dateFormat.format(date)
    reply =
      """
        HTTP/1.1 200 OK
        Date: $formatted

        Hello World
        """
        .trimIndent()
  }

  fun replyFreeFormDate(date: String) {
    reply =
      """
        HTTP/1.1 200 OK
        Date: $date

        Hello World
        """
        .trimIndent()
  }

  fun replyNoDate() {
    reply =
      """
      HTTP/1.1 200 OK

      Hello World
      """
        .trimIndent()
  }

  fun close() {
    server.close()
  }
}
