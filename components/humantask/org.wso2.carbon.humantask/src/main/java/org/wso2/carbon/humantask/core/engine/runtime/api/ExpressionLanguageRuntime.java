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

package org.wso2.carbon.humantask.core.engine.runtime.api;

import org.w3c.dom.Node;
import org.wso2.carbon.humantask.core.utils.Duration;

import java.util.Calendar;
import java.util.List;

/**
 * The expression language runtime for evaluation operations.
 */
public interface ExpressionLanguageRuntime {

    /**
     * Evaluate XPath expression
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return Return List of selected nodes or string
     */
    List evaluate(String exp, EvaluationContext evalCtx);

    /**
     * Evaluate given XPath string and returns result as a string
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return String
     */
    String evaluateAsString(String exp, EvaluationContext evalCtx);

    /**
     * Evaluate given XPath string and returns the result as Date
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return Calendar
     */
    Calendar evaluateAsDate(String exp, EvaluationContext evalCtx);

    /**
     * Evaluate given XPath string and returns the result as Date
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return Duration
     */
    Duration evaluateAsDuration(String exp, EvaluationContext evalCtx);

    /**
     * Evaluate given XPath string and returns the result as boolean
     *
     * @param exp     XPath expression
     * @param evalCtx Evaluation context containing all the required context information
     * @return boolean
     */
    boolean evaluateAsBoolean(String exp, EvaluationContext evalCtx);

    /**
     * Evaluate given XPath and returns the results as a java.lang.Number
     *
     * @param exp     XPath expression
     * @param evalCtx Evaluation context containing all the required context information
     * @return Number
     */
    Number evaluateAsNumber(String exp, EvaluationContext evalCtx);

    /**
     * Evaluate the expression returns an Element
     *
     * @param exp      Expresion
     * @param partName Name of the part
     * @param evalCtx  EvaluationContext
     * @return Part as an Node
     */
    Node evaluateAsPart(String exp, String partName, EvaluationContext evalCtx);
}
