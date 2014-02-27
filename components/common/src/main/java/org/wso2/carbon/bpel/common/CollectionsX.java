/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Collections utility class
 */
public final class CollectionsX {


    // private constructor in-order to disable instantiation
    private CollectionsX() {}

    public static <C extends Collection<T>, S, T extends S> C filter(C dest, Collection<S> src, Class<T> t) {
        return filter(dest, src.iterator(), t);
    }

    public static <C extends Collection<T>, S, T extends S> C filter(C newList, Iterator<S> iterator, Class<T> t) {
        while (iterator.hasNext()) {
            S next = iterator.next();
            if (t.isAssignableFrom(next.getClass())) {
                newList.add((T) next);
            }
        }
        return newList;
    }

    /**
     * Filter a collection by member class.
     *
     * @param src    source collection
     * @param aClass requested class
     * @return collection consisting of the members of the input that are
     *         assignable to the given class
     */
    public static <T> Collection<T> filter(Collection src, final Class<T> aClass) {
        return filter(new ArrayList<T>(src.size()), src.iterator(), aClass);
    }
}
