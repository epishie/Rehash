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

import com.epishie.rehash.Rehash;
import com.epishie.rehash.bus.RxEventBus;
import com.epishie.rehash.view.StoryDetailActivity;
import com.epishie.rehash.view.TopStoriesActivity;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void injectApplication(Rehash app);
    void injectActivity(TopStoriesActivity activity);
    void injectActivity(StoryDetailActivity activity);
    @Named("action")
    RxEventBus getActionBus();
}
