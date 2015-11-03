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

package com.epishie.rehash.view.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.epishie.rehash.R;
import com.epishie.rehash.action.ActionCreator;
import com.epishie.rehash.model.Story;
import com.epishie.rehash.model.StoryBundle;

import java.lang.ref.WeakReference;
import java.util.Date;

import butterknife.Bind;

import static butterknife.ButterKnife.bind;

public class TopStoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int STORY_ITEM = 0;
    private static final int MORE_ITEM = 1;

    private final StoryBundle mStories;
    private final ActionCreator mActionCreator;
    private WeakReference<Listener> mListenerRef;

    public TopStoriesAdapter(ActionCreator actionCreator) {
        mStories = new StoryBundle();
        mActionCreator = actionCreator;
        mListenerRef = new WeakReference<>(null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;
        if (viewType == STORY_ITEM) {
            View view = inflater.inflate(R.layout.item_top_story, parent, false);
            viewHolder = new StoryViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_load_more, parent, false);
            viewHolder = new LoadMoreViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == MORE_ITEM) {
            final LoadMoreViewHolder viewHolder = (LoadMoreViewHolder) holder;
            viewHolder.mMoreButton.setVisibility(View.VISIBLE);
            viewHolder.mProgress.setVisibility(View.GONE);
            viewHolder.mMoreButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mListenerRef.get() != null) {
                        mListenerRef.get().onRequestMoreStories();
                        viewHolder.mMoreButton.setVisibility(View.GONE);
                        viewHolder.mProgress.setVisibility(View.VISIBLE);
                    }
                }
            });

            return;
        }

        StoryViewHolder viewHolder = (StoryViewHolder) holder;

        final Story story = mStories.get(position);
        viewHolder.mTitleText.setText(story.getTitle());
        viewHolder.mAuthorText.setText(story.getAuthor());
        viewHolder.mScoreText.setText(String.valueOf(story.getScore()));
        Date now = new Date();
        CharSequence age = DateUtils.getRelativeTimeSpanString(story.getTime().getTime(),
                now.getTime(),
                DateUtils.MINUTE_IN_MILLIS);
        viewHolder.mAgeText.setText(age);
        viewHolder.mOpenButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mListenerRef.get() != null) {
                    mListenerRef.get().onOpenStoryLink(Uri.parse(story.getUrl()));
                }
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mListenerRef.get() != null) {
                    mListenerRef.get().onSelectStory(story.getId());
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mStories.size()) {
            return MORE_ITEM;
        }
        return STORY_ITEM;
    }

    @Override
    public int getItemCount() {
        if (mStories.size() == 0) {
            return mStories.size();
        }
        return mStories.size() + 1;
    }

    public void addStories(StoryBundle stories) {
        mStories.addAll(stories);
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        mListenerRef = new WeakReference<>(listener);
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        protected TextView mTitleText;
        @Bind(R.id.author)
        protected TextView mAuthorText;
        @Bind(R.id.score)
        protected TextView mScoreText;
        @Bind(R.id.age)
        protected TextView mAgeText;
        @Bind(R.id.open)
        protected ImageButton mOpenButton;

        public StoryViewHolder(View itemView) {
            super(itemView);
            bind(this, itemView);
        }
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.more)
        protected Button mMoreButton;
        @Bind(R.id.progress)
        protected ProgressBar mProgress;

        public LoadMoreViewHolder(View itemView) {
            super(itemView);
            bind(this, itemView);
        }
    }

    public interface Listener {

        void onRequestMoreStories();
        void onSelectStory(int id);
        void onOpenStoryLink(Uri uri);
    }
}