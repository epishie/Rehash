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

import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.http.GET;
import retrofit.http.Path;

public class RetrofitHackerNewsApi implements HackerNewsApi {

    private static final String ENDPOINT = "https://hacker-news.firebaseio.com";

    private final HackerNews mHackerNews;

    public RetrofitHackerNewsApi(Client client) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(client)
                .build();
        mHackerNews = restAdapter.create(HackerNews.class);
    }

    @Override
    public List<Integer> getTopStories() {
        return mHackerNews.topStories();
    }

    @Override
    public Story getStory(int id) {
        return mHackerNews.story(id);
    }

    @Override
    public Comment getComment(int id) {
        return mHackerNews.comment(id);
    }

    public interface HackerNews {

        @GET("/v0/topstories.json")
        List<Integer> topStories();

        @GET("/v0/item/{id}.json")
        Story story(@Path("id")int id);

        @GET("/v0/item/{id}.json")
        Comment comment(@Path("id")int id);
    }
}
