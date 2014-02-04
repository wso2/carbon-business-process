/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine;

/**
 * Exception that occurs while executing/processing a human task.
 */
public class HumanTaskException extends Exception {

    private static final long serialVersionUID = -2654729093407188437L;

    public HumanTaskException() {
        super();
    }

    public HumanTaskException(String s) {
        super(s);
    }

    public HumanTaskException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
