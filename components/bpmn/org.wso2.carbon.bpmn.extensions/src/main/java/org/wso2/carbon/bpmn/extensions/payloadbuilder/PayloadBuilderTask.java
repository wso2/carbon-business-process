/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.payloadbuilder;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.el.JuelExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service Task for payload which are in JSON/XML format
 */
public class PayloadBuilderTask implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(PayloadBuilderTask.class);

    private JuelExpression input;
    private String output;
    private FixedValue outputVariable;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if (input != null) {
            output = input.getValue(delegateExecution).toString();
        }

        if (outputVariable != null) {
            String outVarName = outputVariable.getValue(delegateExecution).toString();
            delegateExecution.setVariable(outVarName, output);
        }
    }
}
