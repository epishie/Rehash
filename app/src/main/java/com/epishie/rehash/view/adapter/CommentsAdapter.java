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
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.epishie.rehash.R;
import com.epishie.rehash.model.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;

import static butterknife.ButterKnife.bind;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_STORY_TEXT = 0;
    private static final int ITEM_COMMENT = 1;
    private static final int ITEM_MORE = 2;
    private static final int MARKER_WIDTH = 16; // DP

    private final List<Comment> mComments;
    private final String mStoryText;
    private final DisplayMetrics mMetrics;
    private Listener mListener;
    private boolean mDataEnded;

    public CommentsAdapter(String storyText, DisplayMetrics metrics) {
        mComments = new ArrayList<>();
        mComments.add(null);
        mStoryText = storyText;
        mMetrics = metrics;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case ITEM_STORY_TEXT:
                view = inflater.inflate(R.layout.item_story_text, parent, false);
                viewHolder = new StoryTextViewHolder(view);
                break;
            case ITEM_COMMENT:
                view = inflater.inflate(R.layout.item_comment, parent, false);
                viewHolder = new CommentViewHolder(view);
                break;
            case ITEM_MORE:
                view = inflater.inflate(R.layout.item_load_more, parent, false);
                viewHolder = new LoadMoreViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_STORY_TEXT:
                StoryTextViewHolder storyTextViewHolder = (StoryTextViewHolder) holder;
                storyTextViewHolder.mStoryText.setText(mStoryText);
                break;
            case ITEM_COMMENT:
                CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
                int offset = (getItemViewType(0) == ITEM_COMMENT) ? 0 : 1;
                Comment comment = mComments.get(position - offset);

                // By line
                Date now = new Date();
                CharSequence age = DateUtils.getRelativeTimeSpanString(comment.getTime().getTime(),
                        now.getTime(),
                        DateUtils.MINUTE_IN_MILLIS);
                Context context = holder.itemView.getContext();
                String byLine = context.getString(R.string.lbl_comment_by_line,
                        comment.getAuthor(),
                        age);
                commentViewHolder.mCommentByLineText.setText(byLine);

                // Content
                commentViewHolder.mCommentText.setText(Html.fromHtml(comment.getText()));
                int markerWidth = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        comment.getLevel() * MARKER_WIDTH,
                        mMetrics
                );
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(markerWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                commentViewHolder.mMarkerImage.setLayoutParams(layoutParams);
                break;
            case ITEM_MORE:
                return;
        }

        if (!mDataEnded && position == (getItemCount() - 1)) {
            mListener.onRequestMoreComments();
            holder.itemView.post(new Runnable() {
                @Override
                public void run() {
                    mComments.add(null);
                    notifyItemInserted(mComments.size() - 1);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mComments.size() + (mStoryText == null || mStoryText.isEmpty() ? 0 : 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mStoryText != null && !mStoryText.isEmpty()) {
            return ITEM_STORY_TEXT;
        } else if (mComments.get(position) == null) {
            return ITEM_MORE;
        } else {
            return ITEM_COMMENT;
        }
    }

    public void addComments(List<Comment> comments) {
        if (!mComments.isEmpty() && mComments.get(mComments.size() - 1) == null) {
            mComments.remove(mComments.size() - 1);
        }
        mComments.addAll(comments);
        notifyDataSetChanged();
    }

    public void setDataEnded(boolean dataEnded) {
        mDataEnded = dataEnded;
        if (!mComments.isEmpty() && mComments.get(mComments.size() - 1) == null) {
            mComments.remove(mComments.size() - 1);
            notifyItemRemoved(mComments.size() - 1);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.comment_by_line)
        protected TextView mCommentByLineText;
        @Bind(R.id.comment_text)
        protected TextView mCommentText;
        @Bind(R.id.marker)
        protected View mMarkerImage;

        public CommentViewHolder(View itemView) {
            super(itemView);
            bind(this, itemView);
        }
    }

    public static class StoryTextViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text)
        protected TextView mStoryText;

        public StoryTextViewHolder(View itemView) {
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

        void onRequestMoreComments();
    }
}
