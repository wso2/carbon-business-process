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

package org.wso2.carbon.humantask.core.engine.runtime.xpath;

import org.wso2.carbon.humantask.core.dao.PresentationParameterDAO;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.math.BigDecimal;

/**
 *
 *
 */
public final class XPathEvaluatorUtil {
    private XPathEvaluatorUtil() {
    }

    public static void evaluatePresentationParamXPath(PresentationParameterDAO param,
                                                      String expression, String expLang,
                                                      EvaluationContext evalCtx) {
        ExpressionLanguageRuntime expLangRuntime = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine().getExpressionLanguageRuntime(expLang);
        PresentationParameterDAO.Type type = param.getType();
        if (type == PresentationParameterDAO.Type.XSD_BOOL) {
            Boolean result = expLangRuntime.evaluateAsBoolean(expression, evalCtx);
            param.setValue(result.toString());
        } else if (type == PresentationParameterDAO.Type.XSD_STRING) {
            String result = expLangRuntime.evaluateAsString(expression, evalCtx);
            param.setValue(result);
        } else if (type == PresentationParameterDAO.Type.XSD_INT) {
            Number result = expLangRuntime.evaluateAsNumber(expression, evalCtx);
            param.setValue(Integer.toString(result.intValue()));
        } else if (type == PresentationParameterDAO.Type.XSD_DECIMALE) {
            Number result = expLangRuntime.evaluateAsNumber(expression, evalCtx);
            param.setValue(BigDecimal.valueOf(result.doubleValue()).toString());
        } else if (type == PresentationParameterDAO.Type.XSD_DOUBLE) {
            Number result = expLangRuntime.evaluateAsNumber(expression, evalCtx);
            param.setValue(Double.toString(result.doubleValue()));
        }
    }
}
