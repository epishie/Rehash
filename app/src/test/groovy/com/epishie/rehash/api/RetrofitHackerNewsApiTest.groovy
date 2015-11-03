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

package com.epishie.rehash.api

import retrofit.client.Client
import retrofit.client.Request
import retrofit.client.Response
import retrofit.mime.TypedByteArray
import spock.lang.Specification

class RetrofitHackerNewsApiTest extends Specification {

    def client = new MockClient()

    def "getTopStories() retrieves stories from /v0/topstories.json"() {
        given:
        clientRespondsWith("[ 10486541, 10486062, 10486268, 10486230, 10485405, 10485442]")
        def api = new RetrofitHackerNewsApi(client)

        when:
        def topStories = api.topStories

        then:
        client.requests.size() == 1
        client.requests[0].url == "https://hacker-news.firebaseio.com/v0/topstories.json"
        topStories != null
        topStories.size() == 6
        topStories[0] == 10486541
        topStories[1] == 10486062
        topStories[2] == 10486268
        topStories[3] == 10486230
        topStories[4] == 10485405
        topStories[5] == 10485442
    }

    def "getStory() retrieves story from /v0/item/{id}.json"() {
        given:
        clientRespondsWith "{\n" +
                "  \"by\" : \"Sir_Cmpwn\",\n" +
                "  \"descendants\" : 25,\n" +
                "  \"id\" : 10486541,\n" +
                "  \"kids\" : [ 10486604, 10486688, 10486596, 10486622, 10486629 ],\n" +
                "  \"score\" : 87,\n" +
                "  \"time\" : 1446389835,\n" +
                "  \"title\" : \"Please don't use Slack for FOSS projects\",\n" +
                "  \"type\" : \"story\",\n" +
                "  \"url\" : \"https://drewdevault.com/2015/11/01/Please-stop-using-slack.html\"\n" +
                "}"
        def api = new RetrofitHackerNewsApi(client)

        when:
        def story = api.getStory(10486541);

        then:
        client.requests.size() == 1
        client.requests[0].url == "https://hacker-news.firebaseio.com/v0/item/10486541.json"
        story.id == 10486541
        story.title == "Please don't use Slack for FOSS projects"
        story.by == "Sir_Cmpwn"
        story.score == 87
        story.time == 1446389835
        story.kids.size() == 5
        story.kids[0] == 10486604
        story.kids[1] == 10486688
        story.kids[2] == 10486596
        story.kids[3] == 10486622
        story.kids[4] == 10486629
    }

    def ""() {
        given:
        clientRespondsWith("{\n" +
                "  \"by\" : \"munchor\",\n" +
                "  \"id\" : 10486604,\n" +
                "  \"parent\" : 10486541,\n" +
                "  \"text\" : \"Completely agreed. I love Slack and it makes sense for a lot of things.\",\n" +
                "  \"time\" : 1446391211,\n" +
                "  \"type\" : \"comment\"\n" +
                "}")

        def api = new RetrofitHackerNewsApi(client)

        when:
        def comment = api.getComment(10486604)

        then:
        client.requests.size() == 1
        client.requests[0].url == "https://hacker-news.firebaseio.com/v0/item/10486604.json"
        comment.id == 10486604
        comment.text == "Completely agreed. I love Slack and it makes sense for a lot of things."
        comment.by == "munchor"
    }

    def clientRespondsWith(String response) {
        client.responses.add(response)
    }

    class MockClient implements Client {

        List<String> responses = []
        List<Request> requests = []
        def index = 0

        @Override
        Response execute(Request request) throws IOException {
            requests.add(request)
            return new Response(request.url,
                    200,
                    "",
                    Collections.EMPTY_LIST,
                    new TypedByteArray("application/json", responses[index++].getBytes()))
        }
    }
}
