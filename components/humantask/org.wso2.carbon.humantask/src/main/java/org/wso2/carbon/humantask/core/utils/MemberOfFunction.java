/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.humantask.core.utils;

/**
 * Interface used for defining object filters/selectors, classes that are used
 * to determine whether a given object belong in a set.
 * <p/>
 * <p>
 * Created on Feb 4, 2004 at 4:48:55 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public abstract class MemberOfFunction<E> implements UnaryFunction<E, Boolean> {

    /**
     * A unary function that tests whether an element is the member of a set.
     *
     * @param o element to test
     * @return <code>true</code> if element is a member
     */
    public abstract boolean isMember(E o);

    /**
     * Implementation of {@link UnaryFunction} method defering to
     * {@link #isMember(E)}.
     *
     * @param x element to test
     * @return {@ref Boolean.TRUE} if isMemeber returns <code>true</code>,
     *         <code>false</code> otherwise
     */
    public final Boolean apply(E x) {
        return isMember(x) ? Boolean.TRUE : Boolean.FALSE;
    }
}
