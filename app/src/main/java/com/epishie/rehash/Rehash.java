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

package com.epishie.rehash;

import android.app.Application;

import com.epishie.rehash.di.AppComponent;
import com.epishie.rehash.di.DaggerAppComponent;
import com.epishie.rehash.di.HasComponent;
import com.epishie.rehash.store.CommentsStore;
import com.epishie.rehash.store.StoriesStore;

import javax.inject.Inject;

public class Rehash extends Application implements HasComponent<AppComponent> {

    private AppComponent mComponent;
    @Inject
    protected StoriesStore mStoriesStore;
    @Inject
    protected CommentsStore mCommentsStore;

    @Override
    public void onCreate() {
        super.onCreate();

        // SETUP DI
        mComponent = DaggerAppComponent.create();
        mComponent.injectApplication(this);
    }

    @Override
    public AppComponent getComponent() {
        return mComponent;
    }

    public void setComponent(AppComponent component) {
        mComponent = component;
    }
}
