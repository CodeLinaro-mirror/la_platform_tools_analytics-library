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

import com.google.common.truth.Truth;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class GoogleCrashReporterTest {
  // Most of the tests do a future.get(), but since they are uploaded to a local server, they should complete relatively quickly.
  // This rule enforces a shorter timeout for the tests. If you are debugging, you probably want to comment this out.
  @Rule
  public TestRule myTimeout = Timeout.seconds(15);

  private GoogleCrashReporter myReporter;
  private LocalTestServer myTestServer;

  @Before
  public void setup() throws Exception {
    int port = getFreePort();
    assertTrue("Could not obtain free port", port > 0);
    myTestServer = new LocalTestServer(port);
    myTestServer.start();

    double infiniteQps = 1000; // a high enough number for the tests here
    myReporter = new GoogleCrashReporter("http://localhost:" + port + "/submit", UploadRateLimiter.create(infiniteQps), true, false);
  }

  @After
  public void tearDown() {
    myTestServer.stop();
  }

  @Test(expected = ExecutionException.class)
  public void checkServerErrorCaptured() throws Exception {
    myTestServer.setResponseSupplier(httpRequest -> new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
    myReporter.submit(new TestReport()).get();
    fail("The above get call should have failed");
  }

  @Test
  public void checkParameterOverriding() throws Exception {
    myTestServer.setResponseSupplier(
      httpRequest -> new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, httpRequest.content()));
    final String overriddenOsName = "overriddenOsName";
    String response = myReporter.submit(new TestReport() {
      @Override
      protected void overrideDefaultParameters(Map<String, String> parameters) {
        parameters.put("osName", overriddenOsName);
      }
    }).get();

    assertTrue("Request should contain overridden osName. Full request body: " + response,
               response.contains("Content-Disposition: form-data; name=\"osName\"\r\n" +
                                 "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
                                 "Content-Transfer-Encoding: 8bit\r\n" +
                                 "\r\n" +
                                 overriddenOsName));
  }

  @Test
  public void checkRateLimiting() {
    UploadRateLimiter mockLimiter = mock(UploadRateLimiter.class); // defaults to a rate limiter that denies all requests
    // the actual address doesn't matter since we should've stopped long before..
    myReporter = new GoogleCrashReporter("http://404", mockLimiter, true, true);
    CrashReport report = new TestReport();
    try {
      myReporter.submit(report).getNow("123");
      fail("Should not be able to submit a report when the rate of crash upload exceeds the limit");
    }
    catch (CompletionException e) {
      Truth.assertThat(e.getCause().getMessage()).isEqualTo("Exceeded Quota of crashes that can be reported");
    }
  }

  private static int getFreePort() {
    try (ServerSocket s = new ServerSocket(0)) {
      return s.getLocalPort();
    }
    catch (IOException e) {
      return -1;
    }
  }

  // Performs a real upload to staging
  public static void main(String[] args) {
    GoogleCrashReporter crash = new GoogleCrashReporter(true, false);

    CompletableFuture<String> response = crash.submit(new TestReport());
    try {
      String reportId = response.get(20, TimeUnit.SECONDS);
      System.out.println("View report at http://go/crash-staging/" + reportId);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
