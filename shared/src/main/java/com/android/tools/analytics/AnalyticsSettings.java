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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Settings related to analytics reporting. These settings are stored in
 * ~/.android/analytics.settings as a json file.
 */
public class AnalyticsSettings {
    @SerializedName("userId")
    private String mUserId;

    @SerializedName("hasOptedIn")
    private boolean mHasOptedIn;

    @SerializedName("debugDisablePublishing")
    private boolean mDebugDisablePublishing;

    /**
     * Gets a user id used for reporting analytics. This id is pseudo-anonymous.
     */
    public String getUserId() {
        return mUserId;
    }

    /**
     * Indicates whether the user has opted in to sending analytics reporting to Google.
     */
    public boolean hasOptedIn() {
        return mHasOptedIn;
    }

    /**
     * Sets a new user id to be used for reporting analytics. This id should be pseudo-anonymous.
     */
    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    /**
     * Sets the user's choice for opting in to sending analytics reporting to Google or not.
     */
    public void setHasOptedIn(boolean mHasOptedIn) {
        this.mHasOptedIn = mHasOptedIn;
    }

    /** Indicates whether the user has disabled publishing for debugging purposes. */
    public boolean hasDebugDisablePublishing() {
        return mDebugDisablePublishing;
    }
    /**
     * Loads an existing settings file from disk, or creates a new valid settings object if none
     * exists. In case of the latter, will try to load uid.txt for maintaining the same uid with
     * previous metrics reporting.
     *
     * @throws IOException if there are any issues reading the settings file.
     */
    @Nullable
    public static AnalyticsSettings loadSettings() throws IOException {
        File file = getSettingsFile();
        if (!file.exists()) {
            return null;
        }
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        try (FileLock ignored = channel.tryLock()) {
            InputStream inputStream = Channels.newInputStream(channel);
            Gson gson = new GsonBuilder().create();
            AnalyticsSettings settings =
                    gson.fromJson(new InputStreamReader(inputStream), AnalyticsSettings.class);
            return settings;
        } catch (OverlappingFileLockException e) {
            throw new IOException("Unable to lock settings file " + file.toString(), e);
        } catch (JsonParseException e) {
            throw new IOException("Unable to parse settings file " + file.toString(), e);
        }
    }

    /**
     * Creates a new settings object and writes it to disk. Will try to load uid.txt for maintaining
     * the same uid with previous metrics reporting.
     *
     * @throws IOException if there are any issues writing the settings file.
     */
    @NonNull
    public static AnalyticsSettings newAnalyticsSettings() throws IOException {
        AnalyticsSettings settings = new AnalyticsSettings();

        File uidFile = Paths.get(AnalyticsPaths.getAndroidSettingsHome(), "uid.txt").toFile();
        if (uidFile.exists()) {
            try {
                String uid = Files.readFirstLine(uidFile, Charsets.UTF_8);
                settings.setUserId(uid);
            } catch (IOException e) {
                // Ignore and set new UID.
            }
        }
        if (settings.getUserId() == null) {
            settings.setUserId(UUID.randomUUID().toString());
        }
        return settings;
    }

    /**
     * Helper to get the file to read/write settings from based on the configured android settings
     * home.
     */
    private static File getSettingsFile() {
        return Paths.get(AnalyticsPaths.getAndroidSettingsHome(), "analytics.settings").toFile();
    }

    /**
     * Writes this settings object to disk.
     * @throws IOException if there are any issues writing the settings file.
     */
    public void saveSettings() throws IOException {
        File file = getSettingsFile();
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        try (FileLock ignored = channel.tryLock()) {
            OutputStream outputStream = Channels.newOutputStream(channel);
            Gson gson = new GsonBuilder().create();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            gson.toJson(this, writer);
            writer.flush();
            outputStream.flush();

        } catch (OverlappingFileLockException e) {
            throw new IOException("Unable to lock settings file " + file.toString(), e);
        }
    }
}
