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
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.HasComponent;
import com.epishie.rehash.model.StoryBundle;
import com.epishie.rehash.view.adapter.TopStoriesAdapter;
import com.epishie.rehash.view.widget.DividerItemDecoration;

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
    private RetainFragment mRetainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETUP RETENTION
        boolean relaunched = true;
        FragmentManager fm = getSupportFragmentManager();
        mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG_RETAIN);
        if (mRetainFragment == null) {
            relaunched = false;
            mRetainFragment = new RetainFragment();
            fm.beginTransaction()
                    .add(mRetainFragment, TAG_RETAIN)
                    .commit();
        }

        // SETUP DI
        @SuppressWarnings("unchecked")
        HasComponent<AppComponent> appComponentSource = (HasComponent<AppComponent>) getApplication();
        appComponentSource.getComponent().injectActivity(this);

        setupViews();
        setupBus();

        if (!relaunched) {
            mActionCreator.createGetStoriesAction(false);
        }
        if (!relaunched || mRetainFragment.mIsRefreshing.get()) {
            mRefresher.post(new Runnable() {
                @Override
                public void run() {
                    mRetainFragment.mIsRefreshing.set(true);
                    mRefresher.setRefreshing(true); // TODO: Issue on espresso if SwipeRefreshLayout is refreshing on load
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        mAdapter.setListener(null);
        mRetainFragment.mAdapter = mAdapter;
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
        mAdapter = mRetainFragment.mAdapter;
        if (mAdapter == null) {
            mAdapter = new TopStoriesAdapter();
        }
        mList.setLayoutManager(lm);
        mList.setAdapter(mAdapter);

        // SETUP ADAPTER LISTENER
        mAdapter.setListener(new TopStoriesAdapter.Listener() {

            @Override
            public void onRequestMoreStories() {
                mActionCreator.createGetStoriesAction(false);
                mRetainFragment.mIsRefreshing.set(true);
                mRefresher.setRefreshing(true);
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
        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mRetainFragment.mIsRefreshing.set(true);
                mActionCreator.createGetStoriesAction(true);
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
                        mAdapter.addStories(stories);
                        mRetainFragment.mIsRefreshing.set(false);
                        mRefresher.setRefreshing(false);
                    }
                });
        mSubscriptions.add(storySubscription);
    }

    public static class RetainFragment extends Fragment {

        public AtomicBoolean mIsRefreshing = new AtomicBoolean(false);
        public TopStoriesAdapter mAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
