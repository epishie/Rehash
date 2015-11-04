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

package com.epishie.rehash.di;

import android.os.Build;

import com.epishie.rehash.action.ActionCreator;
import com.epishie.rehash.api.HackerNewsApi;
import com.epishie.rehash.api.RetrofitHackerNewsApi;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.store.CommentsStore;
import com.epishie.rehash.store.StoriesStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import retrofit.client.UrlConnectionClient;
import rx.schedulers.Schedulers;

@Module
public class AppModule {

    @Named("data")
    @Singleton
    @Provides
    public RxEventBus provideDataBus() {
        return new RxEventBus();
    }

    @Named("action")
    @Singleton
    @Provides
    public RxEventBus provideActionBus() {
        return new RxEventBus();
    }

    @Singleton
    @Provides
    public ActionCreator provideActionCreator(@Named("action") RxEventBus actionBus) {
        return new ActionCreator(actionBus);
    }

    @Singleton
    @Provides
    public StoriesStore providedStoriesStore(@Named("action") RxEventBus actionBus,
                                             @Named("data") RxEventBus dataBus,
                                             HackerNewsApi api) {
        return new StoriesStore(actionBus, dataBus, Schedulers.newThread(), api);
    }

    @Singleton
    @Provides
    public CommentsStore providedCommentsStore(@Named("action") RxEventBus actionBus,
                                             @Named("data") RxEventBus dataBus,
                                             HackerNewsApi api) {
        return new CommentsStore(actionBus, dataBus, Schedulers.newThread(), api);
    }

    @Singleton
    @Provides
    public HackerNewsApi provideHackerNewsApi() {
        Client client;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            client = new AndroidApacheClient();
        } else {
            client = new UrlConnectionClient();
        }
        return new RetrofitHackerNewsApi(client);
    }
}
