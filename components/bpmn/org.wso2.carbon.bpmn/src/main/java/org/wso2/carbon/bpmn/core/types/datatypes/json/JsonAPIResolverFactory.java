/*
 *     Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.carbon.bpmn.core.types.datatypes.json;


import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.scripting.Resolver;
import org.activiti.engine.impl.scripting.ResolverFactory;

/**
 * JsonAPIResolverFactory is responsible to create JsonAPIResolver's
 * when requested by org.activiti.engine.impl.scripting.ScriptBindingsFactory#createResolvers()
 */
public class JsonAPIResolverFactory implements ResolverFactory{

    @Override
    public Resolver createResolver(VariableScope variableScope) {
        return new JsonAPIResolver();
    }

}
