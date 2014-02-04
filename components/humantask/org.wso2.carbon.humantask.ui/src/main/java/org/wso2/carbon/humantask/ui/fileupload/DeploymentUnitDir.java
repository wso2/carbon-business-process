/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.humantask.ui.fileupload;

import java.io.File;

/**
 * Container providing various functions on the deployment directory.
 */
public class DeploymentUnitDir {
    private String name;
    private File duDirectory;

    DeploymentUnitDir(File dir) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory " + dir + " does not exist!");
        }

        duDirectory = dir;
        name = dir.getName();
        File descriptorFile = new File(duDirectory, "htconfig.xml");

        if (!descriptorFile.exists()) {
            throw new IllegalArgumentException("Directory " + dir +
                    " does not contain a htconfig.xml file!");
        }
    }

    public int hashCode() {
        return duDirectory.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof DeploymentUnitDir &&
                ((DeploymentUnitDir) obj).getDeployDir().getAbsolutePath().equals(getDeployDir().
                        getAbsolutePath());
    }

    public File getDeployDir() {
        return duDirectory;
    }

    /*   public Set<QName> getProcessNames() {
        return _processInfo.keySet();
    }*/

    public String toString() {
        return "{DeploymentUnit " + name + "}";
    }
}
