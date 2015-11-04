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
import com.epishie.rehash.action.GetCommentsAction;
import com.epishie.rehash.api.HackerNewsApi;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.model.Comment;
import com.epishie.rehash.model.CommentsList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

public class CommentsStore {

    private final RxEventBus mActionBus;
    private final RxEventBus mDataBus;
    private final Scheduler mScheduler;
    private final HackerNewsApi mApi;
    private final Map<Integer, HackerNewsApi.Item> mCache;
    private int mCurrentStoryId;
    private int mCurrentIndex;

    public CommentsStore(RxEventBus actionBus, RxEventBus dataBus, Scheduler scheduler, HackerNewsApi api) {
        mActionBus = actionBus;
        mDataBus = dataBus;
        mScheduler = scheduler;
        mApi = api;
        mCache = new HashMap<>();
        mCurrentStoryId = Integer.MIN_VALUE;

        monitorGetCommentsAction();
    }

    private void monitorGetCommentsAction() {
        mActionBus.events(GetCommentsAction.class)
                .observeOn(mScheduler)
                .subscribe(new Action1<GetCommentsAction>() {

                    @Override
                    public void call(GetCommentsAction getCommentsAction) {
                        if (mCache.isEmpty() || mCurrentStoryId != getCommentsAction.getStoryId()
                                || getCommentsAction.isRefresh()) {
                            mCurrentIndex = 0;
                            mCache.clear();
                            mCurrentStoryId = getCommentsAction.getStoryId();
                        }
                        final CommentsList comments = new CommentsList();
                        getComments(getCommentsAction.getStoryId()).map(new CommentMapper())
                                .skip(mCurrentIndex)
                                .limit(getCommentsAction.getCount())
                                .forEach(new Action1<Comment>() {

                                    @Override
                                    public void call(Comment comment) {
                                        comments.add(comment);
                                    }
                                });
                        mCurrentIndex += comments.size();
                        mDataBus.post(comments);
                        if (comments.size() == 0 || comments.size() < getCommentsAction.getCount()) {
                            mDataBus.post(DataMarker.COMMENTS_END);
                        }
                    }
                });
    }

    private Observable<HackerNewsApi.Item> getComments(int storyId) {
        HackerNewsApi.Item item = fetchItem(storyId);
        return Observable.from(item.kids)
                .flatMap(new Func1<Integer, Observable<HackerNewsApi.Item>>() {

                    @Override
                    public Observable<HackerNewsApi.Item> call(Integer id) {
                        return getItems(id, 0);
                    }
                });
    }

    private Observable<HackerNewsApi.Item> getItems(int id, final int level) {
        HackerNewsApi.Item item = fetchItem(id);
        item.tag = level;
        return Observable.just(item)
                .concatMap(new Func1<HackerNewsApi.Item, Observable<? extends HackerNewsApi.Item>>() {

                    @Override
                    public Observable<? extends HackerNewsApi.Item> call(HackerNewsApi.Item item) {
                        Observable<HackerNewsApi.Item> items = Observable.just(item);
                        if (item.kids != null) {
                            for (int kid : item.kids) {
                                items = items.mergeWith(getItems(kid, level + 1));
                            }
                        }
                        return items;
                    }
                });
    }

    private HackerNewsApi.Item fetchItem(int id) {
        HackerNewsApi.Item item = mCache.get(id);
        if (item == null) {
            item = mApi.getItem(id);
            mCache.put(id, item);
        }
        return item;
    }

    private class CommentMapper implements Func1<HackerNewsApi.Item, Comment> {

        @Override
        public Comment call(HackerNewsApi.Item item) {
            return new Comment.Builder()
                    .setId(item.id)
                    .setText(item.text)
                    .setAuthor(item.by)
                    .setTime(new Date(item.time * 1000))
                    .setLevel(item.tag)
                    .build();
        }
    }
}
