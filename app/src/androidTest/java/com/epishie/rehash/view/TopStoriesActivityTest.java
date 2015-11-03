/*
 * Copyright 2015 Epishie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epishie.rehash.view;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.text.format.DateUtils;

import com.epishie.rehash.Rehash;
import com.epishie.rehash.action.GetStoriesAction;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.AppModule;
import com.epishie.rehash.di.DaggerAppComponent;
import com.epishie.rehash.model.Story;
import com.epishie.rehash.model.StoryBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.epishie.rehash.test.ViewMatchers.withId;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;

public class TopStoriesActivityTest {

    private StoryBundle mStories;
    private RxEventBus mActionBus;

    @Rule
    public ActivityTestRule<TopStoriesActivity> mActivityTestRule = new ActivityTestRule<>(
            TopStoriesActivity.class,
            true,
            false);

    @Before
    public void setUp() {
        mActionBus = new RxEventBus();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Rehash app = (Rehash) instrumentation.getTargetContext().getApplicationContext();
        AppComponent component = DaggerAppComponent.builder()
                .appModule(new TestAppModule(mActionBus))
                .build();
        app.setComponent(component);
    }

    @Test
    public void sendsGetStoriesAction() {
        // GIVEN
        final AtomicBoolean received = new AtomicBoolean();
        mActionBus.events(GetStoriesAction.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GetStoriesAction>() {
                    @Override
                    public void call(GetStoriesAction getStoriesAction) {
                        received.set(!getStoriesAction.isRefresh());
                    }
                });

        // WHEN
        mActivityTestRule.launchActivity(new Intent());

        // THEN
        assertTrue(received.get());
    }

    @Test
    public void showsStoriesFromDataBus() {
        // GIVEN
        mActivityTestRule.launchActivity(new Intent());

        // WHEN
        dataBusHasStories(mActivityTestRule.getActivity().mDataBus);

        // THEN
        onView(withId("list")).check(matches(isDisplayed()));
        int i = 0;
        for (Story story : mStories) {
            onView(withId("list")).perform(scrollToPosition(i++));
            onView(allOf(isDescendantOfA(withId("list")), withText(story.getTitle())))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void showsAuthor() {
        // GIVEN
        mActivityTestRule.launchActivity(new Intent());
        Story story = new Story(1, "Title", "sample_author", 0, new Date(), "");
        mStories = new StoryBundle();
        mStories.add(story);

        // WHEN
        mActivityTestRule.getActivity().mDataBus.post(mStories);

        // THEN
        onView(allOf(isDescendantOfA(withId("list")), withText(story.getAuthor())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsScore() {
        // GIVEN
        mActivityTestRule.launchActivity(new Intent());
        Story story = new Story(1, "Title", "", 12, new Date(), "");
        mStories = new StoryBundle();
        mStories.add(story);

        // WHEN
        mActivityTestRule.getActivity().mDataBus.post(mStories);

        // THEN
        onView(allOf(isDescendantOfA(withId("list")), withText(String.valueOf(story.getScore()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsAge() {
        // GIVEN
        mActivityTestRule.launchActivity(new Intent());
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        Story story = new Story(1, "Title", "", 0, calendar.getTime(), "");
        mStories = new StoryBundle();
        mStories.add(story);

        // WHEN
        mActivityTestRule.getActivity().mDataBus.post(mStories);

        // THEN
        CharSequence age = DateUtils.getRelativeTimeSpanString(calendar.getTime().getTime(),
                now.getTime(),
                DateUtils.MINUTE_IN_MILLIS);
        onView(allOf(isDescendantOfA(withId("list")), withText(age.toString())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void launchesUrl() {
        // SETUP
        Intents.init();

        // GIVEN
        mActivityTestRule.launchActivity(new Intent());
        Story story = new Story(1, "Title", "", 0, new Date(), "https://google.com");
        mStories = new StoryBundle();
        mStories.add(story);
        mActivityTestRule.getActivity().mDataBus.post(mStories);

        // WHEN
        onView(withContentDescription("Open URL")).perform(click());

        // THEN
        intended(allOf(hasData(Uri.parse(story.getUrl())),
                hasAction(Intent.ACTION_VIEW)));

        // CLEANUP
        Intents.release();
    }

    @Test
    public void launchesStoryDetailActivity() {
        // SETUP
        Intents.init();

        // GIVEN
        mActivityTestRule.launchActivity(new Intent());
        Story story = new Story(1, "Title", "", 0, new Date(), "https://google.com");
        mStories = new StoryBundle();
        mStories.add(story);
        mActivityTestRule.getActivity().mDataBus.post(mStories);

        // WHEN
        onView(withId("list")).perform(actionOnItemAtPosition(0, click()));

        // THEN
        intended(hasComponent(StoryDetailActivity.class.getName()));

        // CLEANUP
        Intents.release();
    }

    private void dataBusHasStories(RxEventBus dataBus) {
        mStories = new StoryBundle();
        for (int i = 0; i < 15; i++) {
            int id = i + 1;
            Story story = new Story(id, "Story #" + id, "Author #" + id, 10, new Date(), "");
            mStories.add(story);
        }
        dataBus.post(mStories);
    }

    private static class TestAppModule extends AppModule {
        private final RxEventBus mActionBus;

        private TestAppModule(RxEventBus actionBus) {
            mActionBus = actionBus;
        }

        @Override
        public RxEventBus provideActionBus() {
            return mActionBus;
        }
    }
}