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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.widget.TextView;

import com.epishie.rehash.R;
import com.epishie.rehash.action.ActionCreator;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.HasComponent;
import com.epishie.rehash.model.Story;
import com.epishie.rehash.view.adapter.CommentsAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static butterknife.ButterKnife.bind;

public class StoryDetailActivity extends AppCompatActivity {
    public static final String EXTRA_STORY_ID = "com.epishie.rehash.EXTRA_STORY_ID";

    @Named("data")
    @Inject
    protected RxEventBus mDataBus;
    @Inject
    protected ActionCreator mActionCreator;
    @Bind(R.id.story_text)
    protected TextView mStoryText;
    @Bind(R.id.comment_list)
    protected RecyclerView mCommentList;
    private CommentsAdapter mCommentsAdapter;
    private List<Subscription> mSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SETUP DI
        @SuppressWarnings("unchecked")
        HasComponent<AppComponent> appComponentSource = (HasComponent<AppComponent>) getApplication();
        appComponentSource.getComponent().injectActivity(this);

        setupViews();
        setupBus();

        int storyId = getIntent().getIntExtra(EXTRA_STORY_ID, 0);
        mActionCreator.createOpenStoryAction(storyId);
    }

    @Override
    protected void onDestroy() {
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
        mCommentsAdapter = new CommentsAdapter();
        mCommentList.setLayoutManager(lm);
        mCommentList.setAdapter(mCommentsAdapter);
    }

    private void setupBus() {
        mSubscriptions = new ArrayList<>();
        Subscription subscription = mDataBus.events(Story.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Story>() {

                    @Override
                    public void call(Story story) {
                        mStoryText.setText(Html.fromHtml(story.getText()));
                        mCommentsAdapter.setComments(story.getComments());
                    }
                });
        mSubscriptions.add(subscription);
    }
}
