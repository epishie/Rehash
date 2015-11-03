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
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.epishie.rehash.Rehash;
import com.epishie.rehash.action.OpenStoryAction;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.AppModule;
import com.epishie.rehash.di.DaggerAppComponent;
import com.epishie.rehash.model.Story;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.epishie.rehash.test.ViewMatchers.withId;
import static org.junit.Assert.*;

public class StoryDetailActivityTest {

    private RxEventBus mActionBus;

    @Rule
    public ActivityTestRule<StoryDetailActivity> mActivityTestRule = new ActivityTestRule<StoryDetailActivity>(
            StoryDetailActivity.class,
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
    public void sendsOpenStoryAction() {
        // GIVEN
        final AtomicBoolean received = new AtomicBoolean();
        mActionBus.events(OpenStoryAction.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<OpenStoryAction>() {
                    @Override
                    public void call(OpenStoryAction openStoryAction) {
                        received.set(openStoryAction.getId() == 1);
                    }
                });

        // WHEN
        Intent intent = new Intent();
        intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, 1);
        mActivityTestRule.launchActivity(intent);

        // THEN
        assertTrue(received.get());
    }

    @Test
    public void showsText() {
        // GIVEN
        Story story = new Story.Builder()
                .setId(1)
                .setText("This is a sample text")
                .build();
        Intent intent = new Intent();
        intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, 1);
        mActivityTestRule.launchActivity(intent);

        // WHEN
        mActivityTestRule.getActivity().mDataBus.post(story);

        // THEN
        onView(withId("story_text")).check(matches(withText(story.getText())));
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