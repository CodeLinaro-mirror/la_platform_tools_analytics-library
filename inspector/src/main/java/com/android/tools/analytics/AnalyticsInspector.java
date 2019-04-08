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
package com.android.tools.analytics;

import com.google.wireless.android.play.playlog.proto.ClientAnalytics;
import com.google.wireless.android.sdk.stats.AndroidStudioEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/** Tool to inspect the contents of .trk files used for usage analytics reporting. */
public class AnalyticsInspector {
    public static void main(String[] args) throws IOException {
        String filePath = readFilePath(args);

        // This logic allows specifying wildcards and ~ in paths w/o using the shell
        // (so it can be launched from within the IDE).
        Path pattern = new File(filePath.replace("~", System.getProperty("user.home"))).toPath();
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(pattern.getParent(), pattern.getFileName().toString())) {
            for (Path path : stream) {
                System.out.println(path.toString());
                System.out.println("===");
                try (FileChannel channel = new RandomAccessFile(path.toFile(), "rw").getChannel()) {
                    InputStream inputStream = Channels.newInputStream(channel);
                    ClientAnalytics.LogEvent event = null;

                    // read all LogEvents from the trackFile.
                    while ((event = ClientAnalytics.LogEvent.parseDelimitedFrom(inputStream))
                            != null) {
                        AndroidStudioEvent studioEvent =
                                AndroidStudioEvent.parseFrom(event.getSourceExtension());
                        System.out.println(studioEvent);
                        System.out.println("---");
                    }
                }
            }
        }
    }

    /**
     * Returns value from command line argument if provided.
     * Asks user for file path in other case.
     */
    private static String readFilePath(String[] args) {
        if (args.length == 1) {
            return args[0];
        }
        System.out.println("Enter path to file <.trk-file>:");
        Scanner input = new Scanner(System.in);
        return input.nextLine().trim();
    }
}
