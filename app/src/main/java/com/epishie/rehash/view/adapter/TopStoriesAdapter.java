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

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epishie.rehash.R;
import com.epishie.rehash.model.Story;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;

import static butterknife.ButterKnife.bind;

public class TopStoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int STORY_ITEM = 0;
    private static final int MORE_ITEM = 1;
    private static final int FETCH_OFFSET = 3;

    private final List<Story> mStories;
    private Listener mListener;
    private boolean mDataEnded;

    public TopStoriesAdapter() {
        mStories = new ArrayList<>();
        mStories.add(null);
        mListener = null;
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
            return;
        }

        StoryViewHolder viewHolder = (StoryViewHolder) holder;
        final Story story = mStories.get(position);

        // Title
        viewHolder.mTitleText.setText(story.getTitle());

        // By line
        Date now = new Date();
        CharSequence age = DateUtils.getRelativeTimeSpanString(story.getTime().getTime(),
                now.getTime(),
                DateUtils.MINUTE_IN_MILLIS);
        Context context = viewHolder.itemView.getContext();
        String byLine = context.getString(R.string.lbl_by_line,
                story.getScore(),
                story.getAuthor(),
                age);
        viewHolder.mByLineText.setText(byLine);

        // Link
        final Uri uri = Uri.parse(story.getUrl());
        if (uri != null && !(uri.toString().isEmpty())) {
            SpannableString hostString = new SpannableString(uri.getHost());
            hostString.setSpan(new UnderlineSpan(), 0, uri.getHost().length(), 0);
            viewHolder.mUrlText.setText(hostString);
            viewHolder.mUrlText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onOpenStoryLink(uri);
                    }
                }
            });
        }

        // Item link
        viewHolder.mCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectStory(story.getId());
                }
            }
        });

        if (!mDataEnded && position <= (getItemCount() - FETCH_OFFSET) && mStories.get(mStories.size() - 1) != null) {
            mStories.add(null);
            mListener.onRequestMoreStories();
            viewHolder.itemView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(mStories.size() - 1);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (mStories.get(position) == null) ? MORE_ITEM : STORY_ITEM;
    }

    @Override
    public int getItemCount() {
        return mStories.size();
    }

    public void setDataEnded(boolean dataEnded) {
        mDataEnded = dataEnded;
        if (!mStories.isEmpty() && mStories.get(mStories.size() - 1) == null) {
            mStories.remove(mStories.size() - 1);
            notifyItemRemoved(mStories.size() - 1);
        }
    }

    public void addStories(List<Story> stories) {
        if (!mStories.isEmpty() && mStories.get(mStories.size() - 1) == null) {
            mStories.remove(mStories.size() - 1);
        }
        mStories.addAll(stories);
        notifyDataSetChanged();
    }

    public void refreshStories(List<Story> stories) {
        mStories.clear();
        mStories.addAll(stories);
        mDataEnded = false;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.card)
        protected View mCard;
        @Bind(R.id.title)
        protected TextView mTitleText;
        @Bind(R.id.by_line)
        protected TextView mByLineText;
        @Bind(R.id.url)
        protected TextView mUrlText;

        public StoryViewHolder(View itemView) {
            super(itemView);
            bind(this, itemView);
        }
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

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
