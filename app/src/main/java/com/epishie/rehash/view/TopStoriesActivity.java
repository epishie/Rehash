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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.epishie.rehash.R;
import com.epishie.rehash.action.ActionCreator;
import com.epishie.rehash.action.DataMarker;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.HasComponent;
import com.epishie.rehash.model.StoryBundle;
import com.epishie.rehash.view.adapter.TopStoriesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static butterknife.ButterKnife.bind;

public class TopStoriesActivity extends AppCompatActivity {

    private static final String TAG_RETAIN = "retain";
    private static final int FETCH_COUNT = 5;

    @Named("data")
    @Inject
    protected RxEventBus mDataBus;
    @Inject
    protected ActionCreator mActionCreator;
    @Bind(R.id.list)
    protected RecyclerView mList;
    @Bind(R.id.refresher)
    protected SwipeRefreshLayout mRefresher;
    private TopStoriesAdapter mAdapter;
    private List<Subscription> mSubscriptions;
    private StateFragment mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETUP RETENTION
        FragmentManager fm = getSupportFragmentManager();
        mState = (StateFragment) fm.findFragmentByTag(TAG_RETAIN);
        if (mState == null) {
            mState = new StateFragment();
            fm.beginTransaction()
                    .add(mState, TAG_RETAIN)
                    .commit();
        } else {
            mState.mIsRelaunched.set(true);
        }

        // SETUP DI
        @SuppressWarnings("unchecked")
        HasComponent<AppComponent> appComponentSource = (HasComponent<AppComponent>) getApplication();
        appComponentSource.getComponent().injectActivity(this);

        setupViews();
        setupBus();

        if (!mState.mIsRelaunched.get()) {
            mActionCreator.createGetStoriesAction(false, FETCH_COUNT);
        }
        // Show refresh on
        if (mState.mIsRefreshing.get()) {
            mRefresher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRefresher.setRefreshing(true);
                }
            }, 10);
        }
    }

    @Override
    protected void onDestroy() {
        mAdapter.setListener(null);
        mState.mAdapter = mAdapter;
        for (Subscription subscription : mSubscriptions) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void setupViews() {
        setContentView(R.layout.activity_top_stories);
        bind(this);

        // SETUP RECYCLER VIEW
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mAdapter = mState.mAdapter;
        if (mAdapter == null) {
            mAdapter = new TopStoriesAdapter();
        }
        mList.setLayoutManager(lm);
        mList.setAdapter(mAdapter);

        // SETUP ADAPTER LISTENER
        mAdapter.setListener(new TopStoriesAdapter.Listener() {

            @Override
            public void onRequestMoreStories() {
                mActionCreator.createGetStoriesAction(false, FETCH_COUNT);
            }

            @Override
            public void onSelectStory(int id) {
                Intent intent = new Intent(TopStoriesActivity.this, StoryDetailActivity.class);
                intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, id);
                startActivity(intent);
            }

            @Override
            public void onOpenStoryLink(Uri uri) {
                Intent intent = new Intent();
                intent.setData(uri);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });

        // SETUP REFRESH LAYOUT
        mRefresher.setColorSchemeResources(R.color.colorAccent);
        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mState.mIsRefreshing.set(true);
                mActionCreator.createGetStoriesAction(true, FETCH_COUNT);
            }
        });
    }

    private void setupBus() {
        mSubscriptions = new ArrayList<>();
        Subscription storySubscription = mDataBus.events(StoryBundle.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StoryBundle>() {

                    @Override
                    public void call(StoryBundle stories) {
                        if (mState.mIsRefreshing.get()) {
                            mState.mIsRefreshing.set(false);
                            mRefresher.setRefreshing(false);
                            mAdapter.refreshStories(stories);
                        } else {
                            mAdapter.addStories(stories);
                        }
                    }
                });
        Subscription storiesEndedSubscription = mDataBus.events(DataMarker.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DataMarker>() {

                    @Override
                    public void call(DataMarker dataMarker) {
                        if (mState.mIsRefreshing.get()) {
                            mState.mIsRefreshing.set(false);
                            mRefresher.setRefreshing(false);
                        }
                        mAdapter.setDataEnded(true);
                    }
                });
        mSubscriptions.add(storySubscription);
        mSubscriptions.add(storiesEndedSubscription);
    }

    public static class StateFragment extends Fragment {

        public AtomicBoolean mIsRefreshing = new AtomicBoolean(false);
        public AtomicBoolean mIsRelaunched = new AtomicBoolean(false);
        public TopStoriesAdapter mAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
