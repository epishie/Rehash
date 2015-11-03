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

package com.epishie.rehash.action

import com.epishie.rehash.bus.RxEventBus
import spock.lang.Specification

class ActionCreatorTest extends Specification {

    def actionBus = Mock(RxEventBus)

    def "createGetStoriesAction() puts correct action to bus"() {
        given:
        def actionCreator = new ActionCreator(actionBus)

        when:
        actionCreator.createGetStoriesAction(true);

        then:
        1 * actionBus.post( { it.isRefresh() } as GetStoriesAction)
    }
}
