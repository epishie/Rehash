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

package com.epishie.rehash.action;

public class GetCommentsAction {

    private final int mStoryId;
    private final boolean mRefresh;
    private final int mCount;

    public GetCommentsAction(int storyId, boolean refresh, int count) {
        mStoryId = storyId;
        mRefresh = refresh;
        mCount = count;
    }

    public int getStoryId() {
        return mStoryId;
    }

    public boolean isRefresh() {
        return mRefresh;
    }

    public int getCount() {
        return mCount;
    }
}
