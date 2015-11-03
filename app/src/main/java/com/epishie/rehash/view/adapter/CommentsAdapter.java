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

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epishie.rehash.R;
import com.epishie.rehash.model.Comment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

import static butterknife.ButterKnife.bind;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    final List<Comment> mComments;

    public CommentsAdapter() {
        mComments = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = mComments.get(position);
        holder.mCommentText.setText(Html.fromHtml(comment.getText()));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void setComments(List<Comment> comments) {
        mComments.clear();
        mComments.addAll(comments);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.comment_text)
        protected TextView mCommentText;

        public ViewHolder(View itemView) {
            super(itemView);
            bind(this, itemView);
        }
    }
}
