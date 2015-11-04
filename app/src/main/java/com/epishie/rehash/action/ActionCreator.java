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

import com.epishie.rehash.bus.RxEventBus;

public class ActionCreator {

    private final RxEventBus mActionBus;

    public ActionCreator(RxEventBus actionBus) {
        mActionBus = actionBus;
    }

    public void createGetStoriesAction(boolean refresh, int count) {
        mActionBus.post(new GetStoriesAction(refresh, count));
    }

    public void createOpenStoryAction(int id) {
        mActionBus.post(new OpenStoryAction(id));
    }

    public void createGetCommentsAction(int storyId, boolean refresh, int count) {
        mActionBus.post(new GetCommentsAction(storyId, refresh, count));

    }
}
