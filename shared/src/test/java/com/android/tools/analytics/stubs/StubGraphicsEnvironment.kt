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

import java.awt.*
import java.awt.image.BufferedImage
import java.util.*
import org.junit.Assert.fail

/** A Stub implementation of [GraphicsEnvironment] for use in tests. By default fails on any call. */
open class StubGraphicsEnvironment : GraphicsEnvironment() {

  @Throws(HeadlessException::class)
  override fun getScreenDevices(): Array<GraphicsDevice> {
    fail()
    return arrayOf()
  }

  @Throws(HeadlessException::class)
  override fun getDefaultScreenDevice(): GraphicsDevice? {
    fail()
    return null
  }

  override fun createGraphics(img: BufferedImage): Graphics2D? {
    fail()
    return null
  }

  override fun getAllFonts(): Array<Font> {
    fail()
    return arrayOf()
  }

  override fun getAvailableFontFamilyNames(): Array<String> {
    fail()
    return arrayOf()
  }

  override fun getAvailableFontFamilyNames(l: Locale): Array<String> {
    fail()
    return arrayOf()
  }
}
