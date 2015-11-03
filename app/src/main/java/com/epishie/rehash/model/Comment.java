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

import java.util.Date;

public class Comment {

    private final int mId;
    private final String mText;
    private final String mAuthor;
    private final Date mTime;
    private final int mLevel;

    public Comment(int id, String text, String author, Date time, int level)  {
        mId = id;
        mText = text;
        mAuthor = author;
        mTime = time;
        mLevel = level;
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

    public Date getTime() {
        return mTime;
    }

    public int getLevel() {
        return mLevel;
    }

    public static class Builder {
        private int mId;
        private String mText = "";
        private String mAuthor = "";
        private Date mTime = new Date();
        private int mLevel = 0;

        public Comment build() {
            return new Comment(mId, mText, mAuthor, mTime, mLevel);
        }

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setText(String text) {
            if (text != null) {
                mText = text;
            }
            return this;
        }

        public Builder setAuthor(String author) {
            if (author != null) {
                mAuthor = author;
            }
            return this;
        }

        public Builder setTime(Date time) {
            if (time != null) {
                mTime = time;
            }
            return this;
        }

        public Builder setLevel(int level) {
            mLevel = level;
            return this;
        }
    }
}
