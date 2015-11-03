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
import java.util.Date;
import java.util.List;

public class Story {

    private final int mId;
    private final String mTitle;
    private final String mAuthor;
    private final int mScore;
    private final Date mTime;
    private final String mUrl;
    private final String mText;
    private final List<Comment> mComments;

    public Story(int id, String title, String author, int score, Date time, String url, String text, List<Comment> comments) {
        mId = id;
        mTitle = title;
        mAuthor = author;
        mScore = score;
        mTime = time;
        mUrl = url;
        mText = text;
        mComments = comments;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public int getScore() {
        return mScore;
    }

    public Date getTime() {
        return mTime;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getText() {
        return mText;
    }

    public List<Comment> getComments() {
        return mComments;
    }

    public static class Builder {
        private int mId = 0;
        private String mTitle = "";
        private String mAuthor = "";
        private int mScore = 0;
        private Date mTime = new Date();
        private String mUrl = "";
        private String mText = "";
        private List<Comment> comments = new ArrayList<>();

        public Story build() {
            return new Story(mId, mTitle, mAuthor, mScore, mTime, mUrl, mText, comments);
        }

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setTitle(String title) {
            if (title != null) {
                mTitle = title;
            }
            return this;
        }

        public Builder setAuthor(String author) {
            if (author != null) {
                mAuthor = author;
            }
            return this;
        }

        public Builder setScore(int score) {
            mScore = score;
            return this;
        }

        public Builder setTime(Date time) {
            if (time != null) {
                mTime = time;
            }
            return this;
        }

        public Builder setUrl(String url) {
            if (url != null) {
                mUrl = url;
            }
            return this;
        }

        public Builder setText(String text) {
            if (text != null) {
                mText = text;
            }
            return this;
        }

        public Builder addComment(Comment comment) {
            if (comment != null) {
                comments.add(comment);
            }
            return this;
        }
    }
}
