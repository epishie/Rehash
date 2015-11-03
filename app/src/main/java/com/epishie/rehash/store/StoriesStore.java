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

package com.epishie.rehash.store;

import com.epishie.rehash.action.GetStoriesAction;
import com.epishie.rehash.api.HackerNewsApi;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.model.Story;
import com.epishie.rehash.model.StoryBundle;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

public class StoriesStore {

    private static final int PAGE_SIZE = 15;

    private final RxEventBus mActionBus;
    private final RxEventBus mDataBus;
    private final Scheduler mScheduler;
    private final HackerNewsApi mApi;
    private List<Integer> mStoryIds;
    private int mPage;

    public StoriesStore(RxEventBus actionBus, RxEventBus dataBus, Scheduler scheduler, HackerNewsApi api) {
        mActionBus = actionBus;
        mDataBus = dataBus;
        mScheduler = scheduler;
        mApi = api;

        monitorGetStoriesAction();
    }

    void monitorGetStoriesAction() {
        mActionBus.events(GetStoriesAction.class)
                .observeOn(mScheduler)
                .subscribe(new Action1<GetStoriesAction>() {
                    @Override
                    public void call(GetStoriesAction getStoriesAction) {
                        if (mStoryIds == null || getStoriesAction.isRefresh()) {
                            mStoryIds = mApi.getTopStories();
                            mPage = 0;
                        }
                        int start = mPage++ * PAGE_SIZE;
                        if (start >= mStoryIds.size()) {
                            return;
                        }
                        int end = Math.min(start + PAGE_SIZE, mStoryIds.size());
                        List<Integer> storyIds = mStoryIds.subList(start, end);
                        final StoryBundle stories = new StoryBundle();
                        Observable.from(storyIds)
                            .map(new Func1<Integer, HackerNewsApi.Story>() {

                                @Override
                                public HackerNewsApi.Story call(Integer integer) {
                                    return mApi.getStory(integer);
                                }
                            }).map(new Func1<HackerNewsApi.Story, Story>() {

                                @Override
                                public Story call(HackerNewsApi.Story story) {
                                    return new Story(story.id,
                                            story.title,
                                            story.by,
                                            story.score,
                                            new Date(story.time * 1000),
                                            story.url);
                                }
                        }).toBlocking().forEach(new Action1<Story>() {
                            @Override
                            public void call(Story story) {
                                stories.add(story);
                            }
                        });
                        mDataBus.post(stories);
                    }
                });
    }
}
