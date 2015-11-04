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

package com.epishie.rehash.event;

import com.epishie.rehash.bus.RxEventBus;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxReplayEventBus extends RxEventBus {
    private final Subject<Object, Object> bus = new SerializedSubject<>(ReplaySubject.create());

    public void post(Object o) {
        bus.onNext(o);
    }

    public <T> Observable<T> events(Class<T> klass) {
        return events().ofType(klass);
    }

    public Observable<Object> events() {
        return bus.asObservable();
    }
}
