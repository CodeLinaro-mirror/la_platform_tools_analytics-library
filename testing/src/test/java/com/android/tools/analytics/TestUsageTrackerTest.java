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

import static org.junit.Assert.*;

import com.android.testutils.VirtualTimeScheduler;
import com.google.wireless.android.sdk.stats.AndroidStudioEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link TestUsageTracker}. */
public class TestUsageTrackerTest {

    protected VirtualTimeScheduler scheduler;
    protected TestUsageTracker testUsageTracker;

    @Before
    public void before() {
        // first ensure our default is the NullUsageTracker.
        UsageTracker tracker = UsageTracker.getInstanceForTest();
        assertEquals(NullUsageTracker.class, tracker.getClass());

        // create settings & scheduler
        AnalyticsSettings settings = new AnalyticsSettings();
        scheduler = new VirtualTimeScheduler();

        // advance time to ensure we have a uptime different from reported time.
        scheduler.advanceBy(1, TimeUnit.MILLISECONDS);

        // create the test usage tracker and set the global instance.
        testUsageTracker = new TestUsageTracker(settings, scheduler);
        UsageTracker.setInstanceForTest(testUsageTracker);

        // ensure the global instance is the one we just set.
        tracker = UsageTracker.getInstanceForTest();
        assertEquals(testUsageTracker, tracker);
    }

    @After
    public void after() {
        // ensure that cleaning the instance puts us back in the initial state.
        UsageTracker.cleanAfterTesting();
        UsageTracker tracker = UsageTracker.getInstanceForTest();
        assertEquals(NullUsageTracker.class, tracker.getClass());
    }

    @Test
    public void testUsageTrackerTest() {
        // move time forward to ensure the report time is different from start time.
        scheduler.advanceBy(1, TimeUnit.MILLISECONDS);

        // log an event
        testUsageTracker.log(
                AndroidStudioEvent.newBuilder().setKind(AndroidStudioEvent.EventKind.META_METRICS));

        // ensure that that event is what our test usage tracker reports.
        assertEquals(1, testUsageTracker.getUsages().size());
        LoggedUsage usage = testUsageTracker.getUsages().get(0);
        assertEquals(AndroidStudioEvent.EventKind.META_METRICS, usage.getStudioEvent().getKind());

        // ensure that virtual time has moved as we instructed.
        assertEquals(TimeUnit.MILLISECONDS.toNanos(2), usage.getTimestamp());
        assertEquals(2, usage.getLogEvent().getEventTimeMs());
        assertEquals(1, usage.getLogEvent().getEventUptimeMs());
    }

    @Test
    public void testLogWithCustomTime() {
        scheduler.advanceBy(1100, TimeUnit.MILLISECONDS);
        // log first event
        testUsageTracker.log(
          AndroidStudioEvent.newBuilder().setKind(AndroidStudioEvent.EventKind.META_METRICS));
        scheduler.advanceBy(1000, TimeUnit.MILLISECONDS);

        // log second event with timestamp before the first event
        testUsageTracker.log(101, AndroidStudioEvent.newBuilder().setKind(AndroidStudioEvent.EventKind.STUDIO_CRASH));
        scheduler.advanceBy(1000, TimeUnit.MILLISECONDS);

        final List<LoggedUsage> usages = testUsageTracker.getUsages();
        assertEquals(2, usages.size());
        assertEquals(1101, usages.get(0).getLogEvent().getEventTimeMs());
        assertEquals(101, usages.get(1).getLogEvent().getEventTimeMs());
    }
}
