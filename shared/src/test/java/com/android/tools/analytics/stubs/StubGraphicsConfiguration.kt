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

import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.ColorModel
import org.junit.Assert.fail

/** A Stub implementation of [StubGraphicsConfiguration] for use in tests. By default fails on any call. */
open class StubGraphicsConfiguration : GraphicsConfiguration() {

  override fun getDevice(): GraphicsDevice? {
    fail()
    return null
  }

  override fun getColorModel(): ColorModel? {
    fail()
    return null
  }

  override fun getColorModel(transparency: Int): ColorModel? {
    fail()
    return null
  }

  override fun getDefaultTransform(): AffineTransform? {
    fail()
    return null
  }

  override fun getNormalizingTransform(): AffineTransform? {
    fail()
    return null
  }

  override fun getBounds(): Rectangle? {
    fail()
    return null
  }
}
