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

import com.epishie.rehash.action.DataMarker;
import com.epishie.rehash.action.GetStoriesAction;
import com.epishie.rehash.action.OpenStoryAction;
import com.epishie.rehash.api.HackerNewsApi;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.model.Comment;
import com.epishie.rehash.model.Story;
import com.epishie.rehash.model.StoryBundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

public class StoriesStore {

    private static final int COMMENT_SIZE = 10;

    private final RxEventBus mActionBus;
    private final RxEventBus mDataBus;
    private final Scheduler mScheduler;
    private final HackerNewsApi mApi;
    private List<Integer> mStoryIds;
    private int mCurrentIndex;

    public StoriesStore(RxEventBus actionBus, RxEventBus dataBus, Scheduler scheduler, HackerNewsApi api) {
        mActionBus = actionBus;
        mDataBus = dataBus;
        mScheduler = scheduler;
        mApi = api;

        monitorGetStoriesAction();
        monitorOpenStoryAction();
    }

    private void monitorGetStoriesAction() {
        mActionBus.events(GetStoriesAction.class)
                .observeOn(mScheduler)
                .subscribe(new Action1<GetStoriesAction>() {
                    @Override
                    public void call(GetStoriesAction getStoriesAction) {
                        if (mStoryIds == null || getStoriesAction.isRefresh()) {
                            mStoryIds = mApi.getTopStories();
                            mCurrentIndex = 0;
                        }
                        if (mCurrentIndex >= mStoryIds.size()) {
                            mDataBus.post(DataMarker.STORY_END);
                            return;
                        }
                        int end = Math.min(mCurrentIndex + getStoriesAction.getCount(), mStoryIds.size());
                        List<Integer> storyIds = mStoryIds.subList(mCurrentIndex, end);
                        mCurrentIndex = end;
                        final StoryBundle stories = new StoryBundle();
                        Observable.from(storyIds).map(new Func1<Integer, HackerNewsApi.Story>() {

                            @Override
                            public HackerNewsApi.Story call(Integer integer) {
                                return mApi.getStory(integer);
                            }
                        }).map(new StoryMapper(false)).toBlocking().forEach(new Action1<Story>() {

                            @Override
                            public void call(Story story) {
                                stories.add(story);
                            }
                        });
                        mDataBus.post(stories);
                        if (mCurrentIndex == mStoryIds.size()) {
                            mDataBus.post(DataMarker.STORY_END);
                        }
                    }
                });
    }

    private void monitorOpenStoryAction() {
        mActionBus.events(OpenStoryAction.class)
                .observeOn(mScheduler)
                .subscribe(new Action1<OpenStoryAction>() {
                    @Override
                    public void call(OpenStoryAction openStoryAction) {
                        Story story = Observable.just(mApi.getStory(openStoryAction.getId()))
                                .map(new StoryMapper(true))
                                .toBlocking()
                                .first();
                        mDataBus.post(story);
                    }
                });
    }

    private final class StoryMapper implements Func1<HackerNewsApi.Story, Story> {

        private final boolean mInflateComments;

        private StoryMapper(boolean inflateComments) {
            mInflateComments = inflateComments;
        }

        @Override
        public Story call(HackerNewsApi.Story story) {
            final Story.Builder builder = new Story.Builder()
                    .setId(story.id)
                    .setTitle(story.title)
                    .setAuthor(story.by)
                    .setScore(story.score)
                    .setTime(new Date(story.time * 1000))
                    .setUrl(story.url)
                    .setText(story.text);
            if (mInflateComments) {
                Observable.from(story.kids).map(new Func1<Integer, HackerNewsApi.Comment>() {

                    @Override
                    public HackerNewsApi.Comment call(Integer integer) {
                        return mApi.getComment(integer);
                    }
                }).limit(COMMENT_SIZE).concatMap(new CommentMapper()).toBlocking().forEach(new Action1<Comment>() {

                    @Override
                    public void call(Comment comment) {
                        builder.addComment(comment);
                    }
                });
            }

            return builder.build();
        }
    }

    private final class CommentMapper implements Func1<HackerNewsApi.Comment, Observable<Comment>> {

        @Override
        public Observable<Comment> call(HackerNewsApi.Comment comment) {
            List<Comment> comments = new ArrayList<>();
            comments.add(transform(comment, 0));
            if (comment.kids != null && !comment.kids.isEmpty()) {
                HackerNewsApi.Comment reply = mApi.getComment(comment.kids.get(0));
                comments.add(transform(reply, 1));
            }
            return Observable.from(comments);
        }

        private Comment transform(HackerNewsApi.Comment comment, int level) {
            return new Comment.Builder()
                    .setId(comment.id)
                    .setText(comment.text)
                    .setAuthor(comment.by)
                    .setTime(new Date(comment.time * 1000))
                    .setLevel(level)
                    .build();
        }
    }
}
