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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * A Stub implementation of {@link GraphicsEnvironment} for use in tests. By default fails on any
 * call.
 */
public class StubGraphicsEnvironment extends GraphicsEnvironment {

    @Override
    public GraphicsDevice[] getScreenDevices() throws HeadlessException {
        fail();
        return new GraphicsDevice[0];
    }

    @Override
    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
        fail();
        return null;
    }

    @Override
    public Graphics2D createGraphics(BufferedImage img) {
        fail();
        return null;
    }

    @Override
    public Font[] getAllFonts() {
        fail();
        return new Font[0];
    }

    @Override
    public String[] getAvailableFontFamilyNames() {
        fail();
        return new String[0];
    }

    @Override
    public String[] getAvailableFontFamilyNames(Locale l) {
        fail();
        return new String[0];
    }
}
