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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.epishie.rehash.R;
import com.epishie.rehash.action.ActionCreator;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.HasComponent;
import com.epishie.rehash.model.Story;
import com.epishie.rehash.view.adapter.CommentsAdapter;
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

public class StoryDetailActivity extends AppCompatActivity {
    private static final String TAG_RETAIN = "retain";
    public static final String EXTRA_STORY_ID = "com.epishie.rehash.EXTRA_STORY_ID";

    @Named("data")
    @Inject
    protected RxEventBus mDataBus;
    @Inject
    protected ActionCreator mActionCreator;
    @Bind(R.id.card)
    protected CardView mCard;
    @Bind(R.id.story_text)
    protected TextView mStoryText;
    @Bind(R.id.comment_list)
    protected RecyclerView mCommentList;
    ProgressDialog mProgressDialog;
    private CommentsAdapter mCommentsAdapter;
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
            int storyId = getIntent().getIntExtra(EXTRA_STORY_ID, 0);
            mActionCreator.createOpenStoryAction(storyId);
        } else {
            showStory(mRetainFragment.mStory);
        }
        if (!relaunched || mRetainFragment.mIsLoading.get()) {
            mRetainFragment.mIsLoading.set(true);
            mProgressDialog = ProgressDialog.show(this, "", "Loading", true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mRetainFragment.mCommentsAdapter = mCommentsAdapter;
        for (Subscription subscription : mSubscriptions) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void setupViews() {
        setContentView(R.layout.activity_story_detail);
        bind(this);

        // SETUP RECYCLER VIEW
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mCommentsAdapter = mRetainFragment.mCommentsAdapter;
        if (mCommentsAdapter == null) {
            mCommentsAdapter = new CommentsAdapter();
        }
        mCommentList.setLayoutManager(lm);
        mCommentList.setAdapter(mCommentsAdapter);
        mCommentList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        // ACTION BAR
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void setupBus() {
        mSubscriptions = new ArrayList<>();
        Subscription subscription = mDataBus.events(Story.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Story>() {

                    @Override
                    public void call(Story story) {
                        mRetainFragment.mStory = story;
                        mRetainFragment.mIsLoading.set(false);
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        showStory(story);
                        mCommentsAdapter.setComments(story.getComments());
                    }
                });
        mSubscriptions.add(subscription);
    }

    private void showStory(Story story) {
        if (story != null && !story.getText().isEmpty()) {
            mStoryText.setText(Html.fromHtml(story.getText()));
            mCard.setVisibility(View.VISIBLE);
        }
    }

    public static class RetainFragment extends Fragment {

        public CommentsAdapter mCommentsAdapter;
        public Story mStory;
        public AtomicBoolean mIsLoading = new AtomicBoolean(false);

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
