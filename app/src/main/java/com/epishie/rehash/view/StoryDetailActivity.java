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
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.epishie.rehash.R;
import com.epishie.rehash.action.ActionCreator;
import com.epishie.rehash.action.DataMarker;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.HasComponent;
import com.epishie.rehash.model.CommentsList;
import com.epishie.rehash.view.adapter.CommentsAdapter;
import com.epishie.rehash.view.widget.DividerItemDecoration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static butterknife.ButterKnife.bind;

public class StoryDetailActivity extends AppCompatActivity {
    private static final String TAG_RETAIN = "retain";
    public static final String EXTRA_STORY_ID = "com.epishie.rehash.EXTRA_STORY_ID";
    public static final String EXTRA_STORY_TEXT = "com.epishie.rehash.EXTRA_STORY_TEXT";
    private static final int FETCH_COUNT = 5;

    @Named("data")
    @Inject
    protected RxEventBus mDataBus;
    @Inject
    protected ActionCreator mActionCreator;
    @Bind(R.id.list)
    protected RecyclerView mList;
    @Bind(R.id.spacer)
    protected View mSpacer;
    private StateFragment mState;
    private CompositeSubscription mSubscriptions;

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

        // SETUP VIEWS
        setupViews();

        // SETUP BUS
        setupBus();

        if (!mState.mIsRelaunched.get()) {
            Intent intent = getIntent();
            mState.mStoryId.set(intent.getIntExtra(EXTRA_STORY_ID, 0));
            mState.mStoryText.set(intent.getStringExtra(EXTRA_STORY_TEXT));
            mActionCreator.createGetCommentsAction(mState.mStoryId.get(), true, FETCH_COUNT);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        mState.mCommentsAdapter.setListener(null);
        mSubscriptions.unsubscribe();
        super.onDestroy();
    }

    private void setupViews() {
        setContentView(R.layout.activity_story_detail);
        bind(this);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        if (mState.mCommentsAdapter == null) {
            mState.mCommentsAdapter = new CommentsAdapter(mState.mStoryText.get(), getResources().getDisplayMetrics());
        }
        mList.setLayoutManager(lm);
        mList.setAdapter(mState.mCommentsAdapter);
        mList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        if (mState.mStoryText.get() == null || mState.mStoryText.get().isEmpty()) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mList.getLayoutParams();
            AppBarLayout.ScrollingViewBehavior scrollingViewBehavior = (AppBarLayout.ScrollingViewBehavior) layoutParams.getBehavior();
            scrollingViewBehavior.setOverlayTop(0);
            mSpacer.setVisibility(View.GONE);
        }

        mState.mCommentsAdapter.setListener(new CommentsAdapter.Listener() {

            @Override
            public void onRequestMoreComments() {
                mActionCreator.createGetCommentsAction(mState.mStoryId.get(), false, FETCH_COUNT);
            }
        });
    }

    private void setupBus() {
        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(mDataBus.events(CommentsList.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CommentsList>() {

                    @Override
                    public void call(CommentsList comments) {
                        mState.mCommentsAdapter.addComments(comments);
                    }
                }));
        mSubscriptions.add(mDataBus.events(DataMarker.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DataMarker>() {

                    @Override
                    public void call(DataMarker dataMarker) {
                        mState.mCommentsAdapter.setDataEnded(true);
                    }
                }));
    }

    public static class StateFragment extends Fragment {

        public CommentsAdapter mCommentsAdapter;
        public AtomicInteger mStoryId = new AtomicInteger();
        public AtomicReference<String> mStoryText = new AtomicReference<>();
        public AtomicBoolean mIsRelaunched = new AtomicBoolean(false);

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
