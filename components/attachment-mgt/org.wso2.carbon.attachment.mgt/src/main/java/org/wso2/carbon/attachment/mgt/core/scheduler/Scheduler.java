/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.core.scheduler;

import java.util.concurrent.Callable;

public interface Scheduler {
    /**
     * Execute a {@link java.util.concurrent.Callable} in a transactional context. If the callable
     * throws an exception, then the transaction will be rolled back, otherwise
     * the transaction will commit.
     *
     * @param <T>         return type
     * @param transaction transaction to execute
     * @return result
     * @throws Exception
     */
    <T> T execTransaction(Callable<T> transaction) throws Exception;
}
