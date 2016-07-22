/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.tools.analytics.stubs;

import static org.junit.Assert.fail;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

/**
 * A Stub implementation of {@link GraphicsDevice} for use in tests. By default fails on any call.
 */
public class StubGraphicsDevice extends GraphicsDevice {

    @Override
    public int getType() {
        fail();
        return 0;
    }

    @Override
    public String getIDstring() {
        fail();
        return null;
    }

    @Override
    public GraphicsConfiguration[] getConfigurations() {
        fail();
        return new GraphicsConfiguration[0];
    }

    @Override
    public GraphicsConfiguration getDefaultConfiguration() {
        fail();
        return null;
    }

    /** Creates a GraphicsDevice with specified width & height. */
    public static GraphicsDevice withBounds(int width, int height) {
        return new StubGraphicsDevice() {
            @Override
            public GraphicsConfiguration getDefaultConfiguration() {
                return new StubGraphicsConfiguration() {
                    @Override
                    public Rectangle getBounds() {
                        return new Rectangle(width, height);
                    }
                };
            }
        };
    }
}
