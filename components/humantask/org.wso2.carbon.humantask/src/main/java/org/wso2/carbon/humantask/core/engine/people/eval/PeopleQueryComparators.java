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

package org.wso2.carbon.humantask.core.engine.people.eval;

import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * TODO
 */
public final class PeopleQueryComparators {
    private PeopleQueryComparators() {
    }

    public static Comparator<OrganizationalEntityDAO> peopleNameComparator() {
        return new Comparator<OrganizationalEntityDAO>() {
            @Override
            public int compare(OrganizationalEntityDAO o1,
                               OrganizationalEntityDAO o2) {
                Collator enCollator = Collator.getInstance(new Locale("en"));
                return enCollator.compare(o1.getName(), o2.getName());
            }
        };

    }

}
