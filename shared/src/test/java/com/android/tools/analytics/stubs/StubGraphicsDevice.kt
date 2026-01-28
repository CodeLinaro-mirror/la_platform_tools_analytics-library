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
import org.junit.Assert.fail

/** A Stub implementation of [GraphicsDevice] for use in tests. By default fails on any call. */
open class StubGraphicsDevice : GraphicsDevice() {

  override fun getType(): Int {
    fail()
    return 0
  }

  override fun getIDstring(): String? {
    fail()
    return null
  }

  override fun getConfigurations(): Array<GraphicsConfiguration> {
    fail()
    return arrayOf()
  }

  override fun getDefaultConfiguration(): GraphicsConfiguration? {
    fail()
    return null
  }

  companion object {

    @JvmStatic
    /** Creates a GraphicsDevice with specified width & height. */
    fun withBounds(width: Int, height: Int): GraphicsDevice = StubGraphicsDeviceWithBounds(width, height)

    private class StubGraphicsDeviceWithBounds constructor(val width: Int, val height: Int) : StubGraphicsDevice() {
      override fun getDefaultConfiguration(): GraphicsConfiguration {
        return object : StubGraphicsConfiguration() {
          override fun getBounds() = Rectangle(width, height)

          override fun getDevice() = this@StubGraphicsDeviceWithBounds

          override fun getDefaultTransform() = AffineTransform()
        }
      }

      override fun getType(): Int = TYPE_RASTER_SCREEN
    }
  }
}
