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

import com.epishie.rehash.action.GetCommentsAction
import com.epishie.rehash.api.HackerNewsApi
import com.epishie.rehash.bus.RxEventBus
import com.epishie.rehash.model.Comment
import com.epishie.rehash.model.CommentsList
import rx.functions.Action1
import rx.schedulers.TestScheduler
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class CommentsStoreTest extends Specification {

    def actionBus = new RxEventBus()
    def dataBus = new RxEventBus()
    def scheduler = new TestScheduler()
    def api = Mock(HackerNewsApi)
    def itemFetchCount = 0

    def "on GetCommentsAction - emits N Comments from api"() {
        given:
        apiHasStoryComments([1, 2, 3, 4, 5])
        AtomicReference<CommentsList> outputComments = new AtomicReference<>(new CommentsList())
        dataBus.events(CommentsList)
        .observeOn(scheduler)
        .subscribe(new Action1<CommentsList>() {

            @Override
            void call(CommentsList comments) {
                outputComments.get().addAll(comments)
            }
        })
        def _ = new CommentsStore(actionBus, dataBus, scheduler, api)

        when:
        actionBus.post(new GetCommentsAction(0, false, 9))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        outputComments.get() != null
        outputComments.get().size() == 9
        (1..3).eachWithIndex { Integer id, i ->
            Comment comment = outputComments.get()[i * 3]
            assert comment.id == id
            assert comment.text == "Comment #" + id
            assert comment.level == 0
            Comment reply1 = outputComments.get()[i * 3 + 1]
            assert reply1.id == 1000 * id + 1
            assert reply1.text == "Comment #" + reply1.id
            assert reply1.level == 1
            Comment reply2 = outputComments.get()[i * 3 + 2]
            assert reply2.id == 1000 * id + 2
            assert reply2.text == "Comment #" + reply2.id
            assert reply2.level == 1
        }
    }

    def "on GetCommentsAction - emits subsequent Comments from api on multiple calls without re-fetch"() {
        given:
        apiHasStoryComments([1, 2, 3, 4, 5])
        AtomicReference<CommentsList> outputComments = new AtomicReference<>(new CommentsList())
        dataBus.events(CommentsList)
                .observeOn(scheduler)
                .subscribe(new Action1<CommentsList>() {

            @Override
            void call(CommentsList comments) {
                outputComments.get().addAll(comments)
            }
        })
        def _ = new CommentsStore(actionBus, dataBus, scheduler, api)

        when:
        actionBus.post(new GetCommentsAction(0, false, 3))
        actionBus.post(new GetCommentsAction(0, false, 6))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        itemFetchCount == 10 // 1 story + 9 comments
        outputComments.get() != null
        outputComments.get().size() == 9
        (1..3).eachWithIndex { Integer id, i ->
            Comment comment = outputComments.get()[i * 3]
            assert comment.id == id
            assert comment.text == "Comment #" + id
            assert comment.level == 0
            Comment reply1 = outputComments.get()[i * 3 + 1]
            assert reply1.id == 1000 * id + 1
            assert reply1.text == "Comment #" + reply1.id
            assert reply1.level == 1
            Comment reply2 = outputComments.get()[i * 3 + 2]
            assert reply2.id == 1000 * id + 2
            assert reply2.text == "Comment #" + reply2.id
            assert reply2.level == 1
        }
    }

    def "on GetCommentsAction - emits new Comments from api on refresh"() {
        given:
        apiHasStoryComments([1, 2, 3, 4, 5])
        AtomicReference<List<CommentsList>> outputCommentsList = new AtomicReference<>(new ArrayList<CommentsList>())
        dataBus.events(CommentsList)
                .observeOn(scheduler)
                .subscribe(new Action1<CommentsList>() {

            @Override
            void call(CommentsList comments) {
                outputCommentsList.get().add(comments)
            }
        })
        def _ = new CommentsStore(actionBus, dataBus, scheduler, api)

        when:
        actionBus.post(new GetCommentsAction(0, false, 9))
        actionBus.post(new GetCommentsAction(0, true, 12))
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        then:
        itemFetchCount == 23 // 1 story + 9 comment + 1 story + 12 comment
        outputCommentsList.get() != null
        outputCommentsList.get().size() == 2
        outputCommentsList.get().get(0).size() == 9
        (1..3).eachWithIndex { Integer id, i ->
            Comment comment = outputCommentsList.get().get(0)[i * 3]
            assert comment.id == id
            assert comment.text == "Comment #" + id
            assert comment.level == 0
            Comment reply1 = outputCommentsList.get().get(0)[i * 3 + 1]
            assert reply1.id == 1000 * id + 1
            assert reply1.text == "Comment #" + reply1.id
            assert reply1.level == 1
            Comment reply2 = outputCommentsList.get().get(0)[i * 3 + 2]
            assert reply2.id == 1000 * id + 2
            assert reply2.text == "Comment #" + reply2.id
            assert reply2.level == 1
        }
        outputCommentsList.get().get(1).size() == 12
        (1..4).eachWithIndex { Integer id, i ->
            Comment comment = outputCommentsList.get().get(1)[i * 3]
            assert comment.id == id
            assert comment.text == "Comment #" + id
            assert comment.level == 0
            Comment reply1 = outputCommentsList.get().get(1)[i * 3 + 1]
            assert reply1.id == 1000 * id + 1
            assert reply1.text == "Comment #" + reply1.id
            assert reply1.level == 1
            Comment reply2 = outputCommentsList.get().get(1)[i * 3 + 2]
            assert reply2.id == 1000 * id + 2
            assert reply2.text == "Comment #" + reply2.id
            assert reply2.level == 1
        }
    }

    def apiHasStoryComments(List comments) {
        api.getItem(_ as Integer) >> { Integer id ->
            itemFetchCount++
            def item = new HackerNewsApi.Item()
            item.id = id
            item.text = "Comment #" + id
            if (id == 0) {
                item.kids = comments
            } else if (id <= 1000) {
                item.kids = []
                def prefix = id * 1000
                (1..2).each {
                    item.kids = [prefix + 1, prefix + 2]
                }
            }
            return item
        }
        /*
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
        */
    }
}
