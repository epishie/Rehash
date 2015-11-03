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

package com.epishie.rehash.test;

import android.content.Context;

/**
 * Collection of utility methods for accessing {@link android.content.res.Resources} in instrumentation tests
 */
public class ResourceHelper {

    private static final String STRING = "string";
    private static final String ID = "id";

    /**
     * Returns the id of the string resource
     * @param context The context
     * @param name The name of the string resource
     * @return The id of the string resource
     */
    public static int getStringId(Context context, String name) {
        return getResourceId(context, STRING, name);
    }

    /**
     * Returns the id of the resource
     * @param context The context
     * @param name The name of the resource
     * @return The id of the resource
     */
    public static int getId(Context context, String name) {
        return getResourceId(context, ID, name);
    }

    private static int getResourceId(Context context, String type, String name) {
        return context.getResources().getIdentifier(name,
                type,
                context.getPackageName());
    }
}
