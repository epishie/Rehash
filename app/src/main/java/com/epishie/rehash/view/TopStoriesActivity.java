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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static butterknife.ButterKnife.bind;

public class TopStoriesActivity extends AppCompatActivity {

    @Named("data")
    @Inject
    protected RxEventBus mDataBus;
    @Inject
    protected ActionCreator mActionCreator;
    @Bind(R.id.list)
    protected RecyclerView mList;
    @Bind(R.id.refresher)
    protected SwipeRefreshLayout mRefresher;
    protected TopStoriesAdapter mAdapter;
    protected List<Subscription> mSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETUP DI
        @SuppressWarnings("unchecked")
        HasComponent<AppComponent> appComponentSource = (HasComponent<AppComponent>) getApplication();
        appComponentSource.getComponent().injectActivity(this);

        setupViews();
        setupBus();

        /*mRefresher.post(new Runnable() {

            @Override
            public void run() {
                mRefresher.setRefreshing(true);
            }
        });*/
        mActionCreator.createGetStoriesAction(false);
    }

    @Override
    protected void onDestroy() {
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
        mAdapter = new TopStoriesAdapter(mActionCreator);
        mList.setLayoutManager(lm);
        mList.setAdapter(mAdapter);

        // SETUP ADAPTER LISTENER
        mAdapter.setListener(new TopStoriesAdapter.Listener() {

            @Override
            public void onRequestMoreStories() {
                mActionCreator.createGetStoriesAction(false);
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
                        mRefresher.setRefreshing(false);
                    }
                });
        mSubscriptions.add(storySubscription);
    }
}
