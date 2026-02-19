/*
 * Copyright (C) 2026 The Android Open Source Project
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
package com.android.tools.analytics

import com.google.wireless.android.sdk.stats.AndroidStudioEvent
import com.google.wireless.android.sdk.stats.AndroidStudioEvent.EventKind
import com.google.wireless.android.sdk.stats.AndroidStudioEventLoggedIn
import com.google.wireless.android.sdk.stats.AppLinksAssistantEvent
import com.google.wireless.android.sdk.stats.AppLinksAssistantEventLoggedIn
import com.google.wireless.android.sdk.stats.AppQualityInsightsUsageEvent
import com.google.wireless.android.sdk.stats.AppQualityInsightsUsageEvent.AiInsightSource
import com.google.wireless.android.sdk.stats.AppQualityInsightsUsageEventLoggedIn
import com.google.wireless.android.sdk.stats.AppQualityInsightsUsageEventLoggedIn.AiInsightSource as AiInsightSourceLoggedIn
import com.google.wireless.android.sdk.stats.DirectAccessUsageEvent
import com.google.wireless.android.sdk.stats.DirectAccessUsageEventLoggedIn
import com.google.wireless.android.sdk.stats.PlayPolicyInsightsUsageEvent
import com.google.wireless.android.sdk.stats.PlayPolicyInsightsUsageEventLoggedIn
import com.google.wireless.android.sdk.stats.PromptLibraryEvent
import com.google.wireless.android.sdk.stats.PromptLibraryEventLoggedIn
import com.google.wireless.android.sdk.stats.SmlChatBotEvent
import com.google.wireless.android.sdk.stats.SmlChatBotEventLoggedIn
import com.google.wireless.android.sdk.stats.SmlCompletionEvent
import com.google.wireless.android.sdk.stats.SmlCompletionEventLoggedIn
import com.google.wireless.android.sdk.stats.SmlConfigurationEvent
import com.google.wireless.android.sdk.stats.SmlConfigurationEventLoggedIn
import com.google.wireless.android.sdk.stats.SmlTransformEvent
import com.google.wireless.android.sdk.stats.SmlTransformEventLoggedIn
import com.google.wireless.android.sdk.stats.StudioCoreGeminiActionsEvent
import com.google.wireless.android.sdk.stats.StudioCoreGeminiActionsEventLoggedIn
import com.google.wireless.android.sdk.stats.StudioLabsEvent
import com.google.wireless.android.sdk.stats.StudioLabsEventLoggedIn
import com.google.wireless.android.sdk.stats.TestScenarioEvent
import com.google.wireless.android.sdk.stats.TestScenarioEventLoggedIn
import com.google.wireless.android.sdk.stats.UIActionStats
import com.google.wireless.android.sdk.stats.UIActionStatsLoggedIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EventTranslatorTest {

  @Test
  fun testTranslateAppLinksAssistantEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.APP_LINKS_ASSISTANT_STATS)
        .setAppLinksAssistantEvent(
          AppLinksAssistantEvent.newBuilder()
            .setEventSource(AppLinksAssistantEvent.EventSource.NEW_LINK_CREATION_SIDE_PANEL)
            .setValidationSummary(AppLinksAssistantEvent.ValidationSummary.getDefaultInstance())
            .setIntentFilterFix(AppLinksAssistantEvent.IntentFilterFix.getDefaultInstance())
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setAppLinksAssistantEvent(
          AppLinksAssistantEventLoggedIn.newBuilder()
            .setEventSource(AppLinksAssistantEventLoggedIn.EventSource.NEW_LINK_CREATION_SIDE_PANEL)
            .setValidationSummary(
              AppLinksAssistantEventLoggedIn.ValidationSummary.getDefaultInstance()
            )
            .setIntentFilterFix(AppLinksAssistantEventLoggedIn.IntentFilterFix.getDefaultInstance())
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateAppQualityInsightUsageEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.APP_QUALITY_INSIGHTS_USAGE)
        .setAppQualityInsightsUsageEvent(
          AppQualityInsightsUsageEvent.newBuilder()
            .setInsightFetchDetails(
              AppQualityInsightsUsageEvent.InsightFetchDetails.newBuilder()
                .setSource(AiInsightSource.AI_INSIGHT_SOURCE_STUDIO_BOT)
                .build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setAppQualityInsightsUsageEvent(
          AppQualityInsightsUsageEventLoggedIn.newBuilder()
            .setInsightFetchDetails(
              AppQualityInsightsUsageEventLoggedIn.InsightFetchDetails.newBuilder()
                .setSource(AiInsightSourceLoggedIn.AI_INSIGHT_SOURCE_STUDIO_BOT)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateDirectAccessUsageEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.DIRECT_ACCESS_USAGE_EVENT)
        .setDirectAccessUsageEvent(
          DirectAccessUsageEvent.newBuilder()
            .setType(DirectAccessUsageEvent.DirectAccessUsageEventType.RESERVE_DEVICE)
            .setReserveDeviceDetails(
              DirectAccessUsageEvent.ReserveDeviceDetails.newBuilder().setSuccess(true).build()
            )
            .setConnectDeviceDetails(
              DirectAccessUsageEvent.ConnectDeviceDetails.newBuilder().setSuccess(false).build()
            )
            .setStreamStartedDetails(
              DirectAccessUsageEvent.StreamStartedDetails.newBuilder().setSuccess(true).build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setDirectAccessUsageEvent(
          DirectAccessUsageEventLoggedIn.newBuilder()
            .setType(DirectAccessUsageEventLoggedIn.DirectAccessUsageEventType.RESERVE_DEVICE)
            .setReserveDeviceDetails(
              DirectAccessUsageEventLoggedIn.ReserveDeviceDetails.newBuilder()
                .setSuccess(true)
                .build()
            )
            .setConnectDeviceDetails(
              DirectAccessUsageEventLoggedIn.ConnectDeviceDetails.newBuilder()
                .setSuccess(false)
                .build()
            )
            .setStreamStartedDetails(
              DirectAccessUsageEventLoggedIn.StreamStartedDetails.newBuilder()
                .setSuccess(true)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateSmlCompletionEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.SML_COMPLETION_EVENT)
        .setSmlCompletionEvent(
          SmlCompletionEvent.newBuilder()
            .setAggregate(
              SmlCompletionEvent.CompletionAggregateEvent.newBuilder()
                .setCompletionsShown(10)
                .setCompletionsAccepted(2)
                .build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setSmlCompletionEvent(
          SmlCompletionEventLoggedIn.newBuilder()
            .setAggregate(
              SmlCompletionEventLoggedIn.CompletionAggregateEvent.newBuilder()
                .setCompletionsShown(10)
                .setCompletionsAccepted(2)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateSmlTransformEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.SML_CODE_TRANSFORMATION_EVENT)
        .setSmlTransformEvent(
          SmlTransformEvent.newBuilder()
            .setRequest(SmlTransformEvent.TransformRequest.getDefaultInstance())
            .setTransformKind(SmlTransformEvent.TransformKind.CUSTOM)
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setSmlTransformEvent(
          SmlTransformEventLoggedIn.newBuilder()
            .setRequest(SmlTransformEventLoggedIn.TransformRequest.getDefaultInstance())
            .setTransformKind(SmlTransformEventLoggedIn.TransformKind.CUSTOM)
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateSmlChatBotEvent_Response() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.SML_CHATBOT_EVENT)
        .setSmlChatBotEvent(
          SmlChatBotEvent.newBuilder()
            .setResponse(
              SmlChatBotEvent.BotResponse.newBuilder()
                .setChatMode(SmlChatBotEvent.ChatMode.AGENT_MODE)
                .build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setSmlChatBotEvent(
          SmlChatBotEventLoggedIn.newBuilder()
            .setResponse(
              SmlChatBotEventLoggedIn.BotResponse.newBuilder()
                .setChatMode(SmlChatBotEventLoggedIn.ChatMode.AGENT_MODE)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateSmlChatBotEvent_ActionInvoked() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.SML_CHATBOT_EVENT)
        .setSmlChatBotEvent(
          SmlChatBotEvent.newBuilder()
            .setActionInvoked(
              SmlChatBotEvent.ActionInvoked.newBuilder()
                .setAction(SmlChatBotEvent.Action.MOVE_TO_EDITOR)
                .build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setSmlChatBotEvent(
          SmlChatBotEventLoggedIn.newBuilder()
            .setActionInvoked(
              SmlChatBotEventLoggedIn.ActionInvoked.newBuilder()
                .setAction(SmlChatBotEventLoggedIn.Action.MOVE_TO_EDITOR)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateSmlConfigurationEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.SML_CONFIGURATION_EVENT)
        .setSmlConfigurationEvent(
          SmlConfigurationEvent.newBuilder()
            .setSmlAvailable(true)
            .setBotOnboardingStarted(true)
            .setBotOnboardingCompleted(false)
            .setCompletionEnabled(true)
            .setTransformEnabled(false)
            .setProjectContextEnabled(true)
            .setAgentAutoAcceptEnabled(false)
            .setProductVariant(SmlConfigurationEvent.SmlProductVariant.PRODUCT_VARIANT_BUSINESS)
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setSmlConfigurationEvent(
          SmlConfigurationEventLoggedIn.newBuilder()
            .setSmlAvailable(true)
            .setBotOnboardingStarted(true)
            .setBotOnboardingCompleted(false)
            .setCompletionEnabled(true)
            .setTransformEnabled(false)
            .setProjectContextEnabled(true)
            .setAgentAutoAcceptEnabled(false)
            .setProductVariant(
              SmlConfigurationEventLoggedIn.SmlProductVariant.PRODUCT_VARIANT_BUSINESS
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateTestScenarioEvent_Request() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.TEST_SCENARIO_EVENT)
        .setTestScenarioEvent(
          TestScenarioEvent.newBuilder()
            .setRequest(TestScenarioEvent.TestScenarioRequest.getDefaultInstance())
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setTestScenarioEvent(
          TestScenarioEventLoggedIn.newBuilder()
            .setRequest(TestScenarioEventLoggedIn.TestScenarioRequest.getDefaultInstance())
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateTestScenarioEvent_Result() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.TEST_SCENARIO_EVENT)
        .setTestScenarioEvent(
          TestScenarioEvent.newBuilder()
            .setTestScenarioResult(
              TestScenarioEvent.TestScenarioResult.newBuilder()
                .setMisformattedResponseCount(1)
                .setGenerationType(TestScenarioEvent.GenerationType.NEW_FILE)
                .setNumAccept(2)
                .setNumDecline(3)
                .build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setTestScenarioEvent(
          TestScenarioEventLoggedIn.newBuilder()
            .setTestScenarioResult(
              TestScenarioEventLoggedIn.TestScenarioResult.newBuilder()
                .setMisformattedResponseCount(1)
                .setGenerationType(TestScenarioEventLoggedIn.GenerationType.NEW_FILE)
                .setNumAccept(2)
                .setNumDecline(3)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateStudioCoreGeminiActionsEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.STUDIO_CORE_GEMINI_ACTIONS)
        .setAndroidStudioCoreGeminiActionsEvent(
          StudioCoreGeminiActionsEvent.newBuilder()
            .setAction(StudioCoreGeminiActionsEvent.Action.RENAME_VARIABLE)
            .setResultsCount(5)
            .setResultsTaken(1)
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setAndroidStudioCoreGeminiActionsEvent(
          StudioCoreGeminiActionsEventLoggedIn.newBuilder()
            .setAction(StudioCoreGeminiActionsEventLoggedIn.Action.RENAME_VARIABLE)
            .setResultsCount(5)
            .setResultsTaken(1)
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateStudioLabsEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.STUDIO_LABS_EVENT)
        .setStudioLabsEvent(
          StudioLabsEvent.newBuilder()
            .setPageInteraction(StudioLabsEvent.PageInteraction.APPLY_BUTTON_CLICKED)
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setStudioLabsEvent(
          StudioLabsEventLoggedIn.newBuilder()
            .setPageInteraction(StudioLabsEventLoggedIn.PageInteraction.APPLY_BUTTON_CLICKED)
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslatePromptLibraryEvent_Update() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.PROMPT_LIBRARY_EVENT)
        .setPromptLibraryEvent(
          PromptLibraryEvent.newBuilder()
            .setUpdate(
              PromptLibraryEvent.Update.newBuilder()
                .setPromptsInLibrary(10)
                .setRulesCount(2)
                .setBuiltinsOverridesCount(1)
                .setUserPromptsCount(7)
                .build()
            )
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setPromptLibraryEvent(
          PromptLibraryEventLoggedIn.newBuilder()
            .setUpdate(
              PromptLibraryEventLoggedIn.Update.newBuilder()
                .setPromptsInLibrary(10)
                .setRulesCount(2)
                .setBuiltinsOverridesCount(1)
                .setUserPromptsCount(7)
                .build()
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslatePromptLibraryEvent_Invoke() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.PROMPT_LIBRARY_EVENT)
        .setPromptLibraryEvent(
          PromptLibraryEvent.newBuilder()
            .setInvoke(PromptLibraryEvent.Invoke.getDefaultInstance())
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setPromptLibraryEvent(
          PromptLibraryEventLoggedIn.newBuilder()
            .setInvoke(PromptLibraryEventLoggedIn.Invoke.getDefaultInstance())
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslatePlayPolicyInsightsUsageEvent() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.PLAY_POLICY_INSIGHTS_USAGE_EVENT)
        .setPlayPolicyInsightsUsageEvent(
          PlayPolicyInsightsUsageEvent.newBuilder()
            .setType(PlayPolicyInsightsUsageEvent.PlayPolicyInsightsUsageEventType.BATCH_INSPECTION)
            .build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setPlayPolicyInsightsUsageEvent(
          PlayPolicyInsightsUsageEventLoggedIn.newBuilder()
            .setType(
              PlayPolicyInsightsUsageEventLoggedIn.PlayPolicyInsightsUsageEventType.BATCH_INSPECTION
            )
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateUIActionStats() {
    val event =
      AndroidStudioEvent.newBuilder()
        .setKind(EventKind.STUDIO_UI_ACTION_STATS)
        .setUiActionStats(
          UIActionStats.newBuilder().setActionClassName("GenerateComposePreviewAction").build()
        )
    val actual = EventTranslator.translate(event)
    assertNotNull(actual)
    val expected =
      AndroidStudioEventLoggedIn.newBuilder()
        .setUiActionStats(
          UIActionStatsLoggedIn.newBuilder()
            .setActionClassName("GenerateComposePreviewAction")
            .build()
        )
    assertEquals(expected.build(), actual?.build())
  }

  @Test
  fun testTranslateUnhandledEventKind() {
    val event = AndroidStudioEvent.newBuilder().setKind(EventKind.UNKNOWN_EVENT_KIND)
    val actual = EventTranslator.translate(event)
    assertNull(actual)
  }
}
