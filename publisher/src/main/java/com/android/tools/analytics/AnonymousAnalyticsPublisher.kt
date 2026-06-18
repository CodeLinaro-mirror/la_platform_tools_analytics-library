/*
 * Copyright (C) 2026 The Android Open Source Project
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
package com.android.tools.analytics

import com.google.wireless.android.play.playlog.proto.ClientAnalytics.LogRequest.LogSource
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.ScheduledExecutorService

class AnonymousAnalyticsPublisher(scheduler: ScheduledExecutorService, spoolLocation: Path, applicationBuild: String) :
  GoogleAnalyticsPublisher(scheduler, spoolLocation, applicationBuild, LogSource.ANDROID_STUDIO, defaultServerUrl) {
  companion object {
    /** A helper to set the default server URL in the constructor, removes exception from the signature that we know cannot be thrown. */
    private val defaultServerUrl: URL
      get() {
        try {
          return URI("https://play.google.com/log?format=raw").toURL()
        }
        // NoOp, url is well-formed.
        catch (e: MalformedURLException) {
          throw RuntimeException(e)
        }
      }
  }
}
