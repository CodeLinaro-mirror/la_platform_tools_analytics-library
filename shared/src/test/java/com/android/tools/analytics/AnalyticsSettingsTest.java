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

package com.android.tools.analytics;

import com.google.common.base.Charsets;
import com.google.gson.JsonParseException;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Tests for {@link AnalyticsSettings}.
 */
public class AnalyticsSettingsTest {
    @Rule public TemporaryFolder testConfigDir = new TemporaryFolder();

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void loadExistingSettingsTest() throws Exception {
        // Configure the paths to use a temp directory for reading from and writing to.
        EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(testConfigDir.getRoot().toString());
        try {

            // Write a json settings file.
            String json = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", hasOptedIn: true }";
            Files.write(
                    testConfigDir.getRoot().toPath().resolve("analytics.settings"),
                    json.getBytes(Charsets.UTF_8));

            // read settings just written.
            AnalyticsSettings settings = AnalyticsSettings.loadSettings();
            assertNotNull(settings);

            // verify read settings.
            assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", settings.getUserId());
            assertTrue(settings.hasOptedIn());

            // Write another json settings file
            String json2 =
                    "{ userId: \"06120264-c9e7-492f-a39c-89c3cbee57c5\", hasOptedIn: false }";
            Files.write(
                    testConfigDir.getRoot().toPath().resolve("analytics.settings"),
                    json2.getBytes(Charsets.UTF_8));
            AnalyticsSettings settings2 = AnalyticsSettings.loadSettings();
            assertNotNull(settings2);

            // verify read settings are updated.
            assertEquals("06120264-c9e7-492f-a39c-89c3cbee57c5", settings2.getUserId());
            assertFalse(settings2.hasOptedIn());
        } finally {
            EnvironmentFakes.setSystemEnvironment();
        }
    }

    @Test
    public void loadBadSettingsTest() throws Exception {
        // Configure the paths to use a temp directory for reading from and writing to.
        EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
                testConfigDir.getRoot().toPath().toString());
        try {
            // Write non-valid json file content.
            String json = "BADFILE";
            Files.write(
                    testConfigDir.getRoot().toPath().resolve("analytics.settings"),
                    json.getBytes(Charsets.UTF_8));

            // try reading the settings file and verify that it fails.
            thrown.expect(IOException.class);
            thrown.expectCause(IsInstanceOf.instanceOf(JsonParseException.class));
            AnalyticsSettings.loadSettings();
        } finally {
            EnvironmentFakes.setSystemEnvironment();
        }
    }

    @Test
    public void loadNewSettingsTest() throws Exception {
        // Configure the paths to use a temp directory for reading from and writing to.
        EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
                testConfigDir.getRoot().toPath().toString());
        try {
            // load settings while there is no settings file present.
            AnalyticsSettings settings = AnalyticsSettings.loadSettings();
            assertNotNull(settings);

            // The generated user id should be a valid UUID.
            UUID.fromString(settings.getUserId());

            // Default setting should be to not be opted in.
            assertFalse(settings.hasOptedIn());

            // The settings file should now be created.
            assertTrue(
                    testConfigDir
                            .getRoot()
                            .toPath()
                            .resolve("analytics.settings")
                            .toFile()
                            .exists());

            // Reading the settings again should lead to the same data being read.
            AnalyticsSettings settings2 = AnalyticsSettings.loadSettings();
            assertNotNull(settings2);

            assertEquals(settings.getUserId(), settings2.getUserId());
            assertFalse(settings2.hasOptedIn());
        } finally {
            EnvironmentFakes.setSystemEnvironment();
        }
    }

    @Test
    public void loadNewSettingsWithExistingUserIdTest() throws Exception {
        // Configure the paths to use a temp directory for reading from and writing to.
        EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
                testConfigDir.getRoot().toPath().toString());

        try {
            // create a 'uid.txt' file, used by previous metrics reporting systems.
            String uid = "db3dd15b-053a-4066-ac93-04c50585edc2";
            Files.write(
                    testConfigDir.getRoot().toPath().resolve("uid.txt"),
                    uid.getBytes(Charsets.UTF_8));

            // load settings while there is no settings file present.
            AnalyticsSettings settings = AnalyticsSettings.loadSettings();
            assertNotNull(settings);

            // Ensure the settings are using the user id from the 'uid.txt' file.
            assertEquals(uid, settings.getUserId());

            // Default setting should be to not be opted in.
            assertFalse(settings.hasOptedIn());
        } finally {
            EnvironmentFakes.setSystemEnvironment();
        }
    }

    @Test
    public void changeSettingsTest() throws Exception {
        // Configure the paths to use a temp directory for reading from and writing to.
        EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
                testConfigDir.getRoot().toPath().toString());

        try {
            // Start with an existing config on disk.
            String json = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", hasOptedIn: true }";
            Files.write(
                    testConfigDir.getRoot().toPath().resolve("analytics.settings"),
                    json.getBytes(Charsets.UTF_8));

            AnalyticsSettings settings = AnalyticsSettings.loadSettings();
            assertNotNull(settings);

            assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", settings.getUserId());
            assertTrue(settings.hasOptedIn());

            // Update properties in the settings.
            String newUserId = "79d30adf-c901-4608-83ca-6dc850068316";
            settings.setUserId(newUserId);
            settings.setHasOptedIn(false);

            // Write updated settings to disk
            settings.saveSettings();

            // Read settings and verify that changes have persisted.
            AnalyticsSettings settings2 = AnalyticsSettings.loadSettings();
            assertEquals(newUserId, settings2.getUserId());
            assertFalse(settings2.hasOptedIn());
        } finally {
            EnvironmentFakes.setSystemEnvironment();
        }
    }
}
