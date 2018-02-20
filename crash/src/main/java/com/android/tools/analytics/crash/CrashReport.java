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
package com.android.tools.analytics.crash;

import static com.android.tools.analytics.crash.GoogleCrashReporter.KEY_EXCEPTION_INFO;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.analytics.crash.exception.NoPiiException;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public abstract class CrashReport {
  public static final String PRODUCT_ANDROID_STUDIO = "AndroidStudio"; // must stay in sync with backend registration

  /** {@link Throwable} classes with messages expected to be useful for debugging and not to contain PII. */
  private static final ImmutableSet<Class<? extends Throwable>> THROWABLE_CLASSES_TO_TRACK_MESSAGES =
    ImmutableSet
      .of(
        ArrayIndexOutOfBoundsException.class,
        ClassCastException.class,
        ClassNotFoundException.class,
        IndexOutOfBoundsException.class,
        NoPiiException.class);

  private enum Type {
    Crash,
    Exception,
    Performance,
  }

  @NonNull public final String productId;
  @Nullable public final String version;
  @Nullable public final Map<String, String> productData;
  @NonNull private final Type type;

  private CrashReport(@NonNull String productId, @Nullable String version, @Nullable Map<String, String> productData, @NonNull Type type) {
    this.productId = productId;
    this.version = version;
    this.productData = productData;
    this.type = type;
  }

  public void serialize(@NonNull MultipartEntityBuilder builder) {
    builder.addTextBody("type", type.toString());

    if (productData != null) {
      productData.forEach(builder::addTextBody);
    }

    serializeTo(builder);
  }

  protected abstract void serializeTo(@NonNull MultipartEntityBuilder builder);

  private static class ExceptionReport extends CrashReport {
    @NonNull private final String myExceptionInfo;

    private ExceptionReport(@NonNull String productId,
                            @Nullable String version,
                            @NonNull String exceptionInfo,
                            @Nullable Map<String, String> productData) {
      super(productId, version, productData, Type.Exception);
      myExceptionInfo = exceptionInfo;
    }

    @Override
    protected void serializeTo(@NonNull MultipartEntityBuilder builder) {
      builder.addTextBody(KEY_EXCEPTION_INFO, myExceptionInfo);
    }
  }

  private static class StudioCrashReport extends CrashReport {
    private final List<String> myDescriptions;

    private StudioCrashReport(@NonNull String productId,
                              @Nullable String version,
                              @NonNull List<String> descriptions,
                              @Nullable Map<String, String> productData) {
      super(productId, version, productData, Type.Crash);
      myDescriptions = descriptions;
    }

    @Override
    protected void serializeTo(@NonNull MultipartEntityBuilder builder) {
      builder.addTextBody("numCrashes", Integer.toString(myDescriptions.size()));
      builder.addTextBody("crashDesc", Joiner.on("\n\n").join(myDescriptions));
    }
  }

  private static class StudioPerformanceWatcherReport extends CrashReport {
    private final String myFileName;
    private final String myThreadDump;

    private StudioPerformanceWatcherReport(@NonNull String productId,
                                           @Nullable String version,
                                           @NonNull String fileName,
                                           @NonNull String threadDump,
                                           @Nullable Map<String, String> productData) {
      super(productId, version, productData, Type.Performance);
      myFileName = fileName;
      myThreadDump = threadDump;
    }

    @Override
    protected void serializeTo(@NonNull MultipartEntityBuilder builder) {
      builder.addTextBody(myFileName,
                          myThreadDump,
                          ContentType.create("text/plain", Charsets.UTF_8));
    }
  }

  public static class Builder {
    private String myProductId = PRODUCT_ANDROID_STUDIO;
    private String myVersion;
    private Type myType = Type.Exception;
    private String myExceptionInfo = "<unknown>";
    private List<String> myCrashDescriptions;
    private String myThreadDump;
    private String myFileName;
    private Map<String,String> myProductData;

    private Builder() {
    }

    @NonNull
    public Builder setProduct(@NonNull String productId) {
      myProductId = productId;
      return this;
    }

    @NonNull
    public Builder setVersion(@NonNull String version) {
      myVersion = version;
      return this;
    }

    @NonNull
    public Builder addProductData(@NonNull Map<String,String> kv) {
      if (myProductData == null) {
        myProductData = new HashMap<>();
      }

      myProductData.putAll(kv);
      return this;
    }

    @NonNull
    private Builder setType(@NonNull Type type) {
      myType = type;
      return this;
    }

    @NonNull
    private Builder setThrowable(@NonNull Throwable t) {
      //noinspection ThrowableResultOfMethodCallIgnored
      myExceptionInfo = getDescription(getRootCause(t));
      return this;
    }

    @NonNull
    private Builder setDescriptions(@NonNull List<String> descriptions) {
      myCrashDescriptions = descriptions;
      return this;
    }

    @NonNull
    private Builder setThreadDump(@NonNull String fileName, @NonNull String threadDump) {
      myFileName = fileName;
      myThreadDump = threadDump;
      return this;
    }

    @NonNull
    public CrashReport build() {
      switch (myType) {
        case Crash:
          return new StudioCrashReport(myProductId, myVersion, myCrashDescriptions, myProductData);
        case Performance:
          return new StudioPerformanceWatcherReport(myProductId, myVersion, myFileName, myThreadDump, myProductData);
        default:
        case Exception:
          return new ExceptionReport(myProductId, myVersion, myExceptionInfo, myProductData);
      }
    }

    @NonNull
    public static Builder createForException(@NonNull Throwable t) {
      return new Builder()
        .setType(Type.Exception)
        .setThrowable(t);
    }

    @NonNull
    public static Builder createForCrashes(@NonNull List<String> descriptions) {
      return new Builder()
        .setType(Type.Crash)
        .setDescriptions(descriptions);
    }

    @NonNull
    public static Builder createForPerfReport(@NonNull String fileName, @NonNull String threadDump) {
      return new Builder()
        .setType(Type.Performance)
        .setThreadDump(fileName, threadDump);
    }
  }

  // Similar to ExceptionUntil.getRootCause, but attempts to avoid infinite recursion
  @NonNull
  public static Throwable getRootCause(@NonNull Throwable t) {
    int depth = 0;
    while (depth++ < 20) {
      if (t.getCause() == null) return t;
      t = t.getCause();
    }
    return t;
  }

  /**
   * Returns an exception description (similar to {@link Throwables#getStackTraceAsString(Throwable)}} with the exception message
   * removed in order to strip off any PII. The exception message is include for some specific exceptions where we know that the
   * message will not have any PII.
   */
  @NonNull
  public static String getDescription(@NonNull Throwable t) {
    if (THROWABLE_CLASSES_TO_TRACK_MESSAGES.contains(t.getClass())) {
      return Throwables.getStackTraceAsString(t);
    }

    StringBuilder sb = new StringBuilder(256);

    sb.append(t.getClass().getName());
    sb.append(": <elided>\n"); // note: some message is needed for the backend to parse the report properly

    for (StackTraceElement el : t.getStackTrace()) {
      sb.append("\tat ");
      sb.append(el);
      sb.append('\n');
    }

    return sb.toString();
  }
}
