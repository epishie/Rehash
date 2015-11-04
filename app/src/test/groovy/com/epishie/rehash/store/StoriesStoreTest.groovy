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

import com.epishie.rehash.action.DataMarker
import com.epishie.rehash.action.GetStoriesAction
import com.epishie.rehash.action.OpenStoryAction
import com.epishie.rehash.api.HackerNewsApi
import com.epishie.rehash.bus.RxEventBus
import com.epishie.rehash.model.Comment
import com.epishie.rehash.model.Story
import com.epishie.rehash.model.StoryBundle
import rx.functions.Action1
import rx.schedulers.TestScheduler
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class StoriesStoreTest extends Specification {

    def actionBus = new RxEventBus()
    def dataBus = new RxEventBus()
    def scheduler = new TestScheduler()
    def api = Mock(HackerNewsApi)

    def "on GetStoriesAction - emits N Stories from api"() {
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
        actionBus.post(new GetStoriesAction(false, 10))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 10
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "STORY_END #" + (i + 1)
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
        actionBus.post(new GetStoriesAction(false, 5))
        actionBus.post(new GetStoriesAction(false, 10))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 15
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "STORY_END #" + (i + 1)
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
        AtomicBoolean endReceived = new AtomicBoolean()
        dataBus.events(DataMarker)
                .observeOn(scheduler)
                .subscribe(new Action1<DataMarker>() {
            @Override
            void call(DataMarker dataMarker) {
                if (dataMarker == DataMarker.STORY_END) {
                    endReceived.set(true)
                }
            }
        })

        when:
        actionBus.post(new GetStoriesAction(false, 15))
        actionBus.post(new GetStoriesAction(false, 10))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 20
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "STORY_END #" + (i + 1)
        }
        endReceived.get()
    }

    def "on GetStoriesAction - emits empty Stories when there is nothing to be fetched"() {
        given:
        apiHasStoriesOfCount 20
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<StoryBundle> storyBundle = new AtomicReference<>(new StoryBundle())
        dataBus.events(StoryBundle)
                .observeOn(scheduler)
                .subscribe(new Action1<StoryBundle>() {

            @Override
            void call(StoryBundle stories) {
                storyBundle.get().addAll(stories)
            }
        })
        AtomicBoolean endReceived = new AtomicBoolean()
        dataBus.events(DataMarker)
                .observeOn(scheduler)
                .subscribe(new Action1<DataMarker>() {
            @Override
            void call(DataMarker dataMarker) {
                if (dataMarker == DataMarker.STORY_END) {
                    endReceived.set(true)
                }
            }
        })

        when:
        actionBus.post(new GetStoriesAction(false, 20))
        actionBus.post(new GetStoriesAction(false, 10))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 20
        storyBundle.get().eachWithIndex { Story story, i ->
            assert story.id == i + 1
            assert story.title == "STORY_END #" + (i + 1)
        }
        endReceived.get()
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
        actionBus.post(new GetStoriesAction(false, 15))
        actionBus.post(new GetStoriesAction(true, 15))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        storyBundle.get() != null
        storyBundle.get().size() == 30
        (1..15).eachWithIndex { Integer id, i ->
            Story story = storyBundle.get().get(i)
            assert story.id == id
            assert story.title == "STORY_END #" + id
        }
        (1..15).eachWithIndex { Integer id, i ->
            Story story = storyBundle.get().get(i + 15)
            assert story.id == id
            assert story.title == "STORY_END #" + id
        }
    }

    def "on OpenStoryAction - emit Story"() {
        given:
        def testStory = new HackerNewsApi.Story()
        testStory.id = 1
        testStory.title = "Test"
        testStory.kids = [1, 2, 3, 4, 5]
        apiHasStory testStory
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<Story> outputStory = new AtomicReference<>()
        dataBus.events(Story)
            .observeOn(scheduler)
            .subscribe(new Action1<Story>() {

                @Override
                void call(Story story) {
                    outputStory.set(story)
                }
        });

        when:
        actionBus.post(new OpenStoryAction(1))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        outputStory.get() != null
        outputStory.get().id == 1
        outputStory.get().title == "Test"
        outputStory.get().comments.size() == 10 // Each comment has 1 reply 5 * 2
        (1..5).eachWithIndex { Integer id, i ->
            Comment comment = outputStory.get().comments[i * 2]
            assert comment.id == id
            assert comment.text == "Comment #" + id
            assert comment.level == 0
            Comment reply = outputStory.get().comments[i * 2 + 1]
            assert reply.id == 1000 * id + 1
            assert reply.text == "Comment #" + reply.id
            assert reply.level == 1
        }
    }

    def "on OpenStoryAction - emit Story, limits latest 10 comments"() {
        given:
        def testStory = new HackerNewsApi.Story()
        testStory.id = 1
        testStory.title = "Test"
        testStory.kids = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
        apiHasStory testStory // Each comment has a reply
        def _ = new StoriesStore(actionBus, dataBus, scheduler, api)
        AtomicReference<Story> outputStory = new AtomicReference<>()
        dataBus.events(Story)
                .observeOn(scheduler)
                .subscribe(new Action1<Story>() {

            @Override
            void call(Story story) {
                outputStory.set(story)
            }
        });

        when:
        actionBus.post(new OpenStoryAction(1))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        outputStory.get() != null
        outputStory.get().id == 1
        outputStory.get().title == "Test"
        outputStory.get().comments.size() == 20 // Each comment has 1 reply 5 * 2
        (1..10).eachWithIndex { Integer id, i ->
            Comment comment = outputStory.get().comments[i * 2]
            assert comment.id == id
            assert comment.text == "Comment #" + id
            assert comment.level == 0
            Comment reply = outputStory.get().comments[i * 2 + 1]
            assert reply.id == 1000 * id + 1
            assert reply.text == "Comment #" + reply.id
            assert reply.level == 1
        }
    }

    def apiHasStory(HackerNewsApi.Story story) {
        api.getStory(_ as Integer) >> story
        api.getComment(_ as Integer) >> { Integer id ->
            HackerNewsApi.Comment comment = new HackerNewsApi.Comment()
            comment.id = id
            comment.text = "Comment #" + id
            if (id <= 1000) {
                def prefix = id * 1000
                comment.kids = [prefix + 1, prefix + 2]
            }
            return comment
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
            story.title = "STORY_END #" + id
            return story
        }
    }
}
