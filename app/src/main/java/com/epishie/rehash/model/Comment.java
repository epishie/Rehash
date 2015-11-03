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

package com.epishie.rehash.model;

import java.util.ArrayList;
import java.util.List;

public class Comment {

    private final int mId;
    private final String mText;
    private final String mAuthor;
    private final List<Comment> mReplies;

    public Comment(int id, String text, String author, List<Comment> replies) {
        mId = id;
        mText = text;
        mAuthor = author;
        mReplies = replies;
    }

    public int getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public List<Comment> getReplies() {
        return mReplies;
    }

    public static class Builder {
        private int mId;
        private String mText = "";
        private String mAuthor = "";
        private List<Comment> mReplies = new ArrayList<>();

        public Comment build() {
            return new Comment(mId, mText, mAuthor, mReplies);
        }

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setText(String text) {
            mText = text;
            return this;
        }

        public Builder setAuthor(String author) {
            mAuthor = author;
            return this;
        }

        public Builder addReply(Comment reply) {
            mReplies.add(reply);
            return this;
        }
    }
}
