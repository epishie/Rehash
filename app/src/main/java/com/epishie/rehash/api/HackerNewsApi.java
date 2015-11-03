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

package com.epishie.rehash.api;

import java.util.List;

public interface HackerNewsApi {

    List<Integer> getTopStories();
    Story getStory(int id);
    Comment getComment(int id);

    class Story {
        public int id;
        public String title;
        public String by;
        public int score;
        public long time;
        public String url;
        public String text;
        public List<Integer> kids;
    }

    class Comment {
        public int id;
        public String text;
        public String by;
        public List<Integer> kids;
        public long time;
    }
}
