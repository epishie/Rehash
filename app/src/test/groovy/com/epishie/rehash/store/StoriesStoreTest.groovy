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

package com.epishie.rehash.store

import com.epishie.rehash.action.GetStoriesAction
import com.epishie.rehash.api.HackerNewsApi
import com.epishie.rehash.bus.RxEventBus
import com.epishie.rehash.model.Story
import com.epishie.rehash.model.StoryBundle
import rx.functions.Action1
import rx.schedulers.TestScheduler
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class StoriesStoreTest extends Specification {

    def actionBus = new RxEventBus()
    def dataBus = new RxEventBus()
    def scheduler = new TestScheduler()
    def api = Mock(HackerNewsApi)

    def "on GetStoriesAction - emits Stories from api, 15 stories per call"() {
        given:
        apiHasStoriesOfCount 500
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<StoryBundle> storyBundle = new AtomicReference<>();
        dataBus.events(StoryBundle)
            .observeOn(scheduler)
            .subscribe(new Action1<StoryBundle>() {

                @Override
                void call(StoryBundle stories) {
                    storyBundle.set(stories)
                }
            })

        when:
        actionBus.post(new GetStoriesAction(false))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 15
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "Story #" + (i + 1)
        }
    }

    def "on GetStoriesAction - emits subsequent Stories from api on multiple calls"() {
        given:
        apiHasStoriesOfCount 500
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<StoryBundle> storyBundle = new AtomicReference<>(new StoryBundle());
        dataBus.events(StoryBundle)
                .observeOn(scheduler)
                .subscribe(new Action1<StoryBundle>() {

            @Override
            void call(StoryBundle stories) {
                storyBundle.get().addAll(stories)
            }
        })

        when:
        actionBus.post(new GetStoriesAction(false))
        actionBus.post(new GetStoriesAction(false))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 30
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "Story #" + (i + 1)
        }
    }

    def "on GetStoriesAction - emits subsequent Stories from api up to end"() {
        given:
        apiHasStoriesOfCount 20
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<StoryBundle> storyBundle = new AtomicReference<>(new StoryBundle());
        dataBus.events(StoryBundle)
                .observeOn(scheduler)
                .subscribe(new Action1<StoryBundle>() {

            @Override
            void call(StoryBundle stories) {
                storyBundle.get().addAll(stories)
            }
        })

        when:
        actionBus.post(new GetStoriesAction(false))
        actionBus.post(new GetStoriesAction(false))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 20
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "Story #" + (i + 1)
        }
    }

    def "on GetStoriesAction - emits new Stories from api if refresh is true "() {
        given:
        apiHasStoriesOfCount 500
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<StoryBundle> storyBundle = new AtomicReference<>(new StoryBundle());
        dataBus.events(StoryBundle)
                .observeOn(scheduler)
                .subscribe(new Action1<StoryBundle>() {

            @Override
            void call(StoryBundle stories) {
                storyBundle.get().addAll(stories)
            }
        })

        when:
        actionBus.post(new GetStoriesAction(false))
        actionBus.post(new GetStoriesAction(true))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 30
        (1..15).eachWithIndex { Integer id, i ->
            Story story = storyBundle.get().get(i)
            assert story.id == id
            assert story.title == "Story #" + id
        }
        (1..15).eachWithIndex { Integer id, i ->
            Story story = storyBundle.get().get(i + 15)
            assert story.id == id
            assert story.title == "Story #" + id
        }
    }

    def apiHasStoriesOfCount(int count) {
        def stories = []
        (1..count).each {
            stories.add(it)
        }
        api.topStories >> stories
        api.getStory(_ as Integer) >> { Integer id ->
            HackerNewsApi.Story story = new HackerNewsApi.Story()
            story.id = id
            story.title = "Story #" + id;
            return story
        }
    }
}
