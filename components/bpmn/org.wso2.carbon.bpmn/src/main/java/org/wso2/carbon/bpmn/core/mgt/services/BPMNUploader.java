/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.core.mgt.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.mgt.model.UploadedFileItem;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * BPMN package uploader admin service
 */
public class BPMNUploader extends AbstractAdmin {
    private static Log log = LogFactory.getLog(BPMNUploader.class);

    public void uploadService(UploadedFileItem[] fileItems) throws AxisFault {
        //First lets filter for jar resources
        ConfigurationContext configurationContext = getConfigContext();
        String repo = configurationContext.getAxisConfiguration().getRepository().getPath();


        if (CarbonUtils.isURL(repo)) {
            throw new AxisFault("URL Repositories are not supported: " + repo);
        }

        //Writing the artifacts to the proper location
        String bpmnDirectory = repo + File.separator + BPMNConstants.BPMN_REPO_NAME;

        String bpmnTemp = CarbonUtils.getCarbonHome() + BPMNConstants.BPMN_PACKAGE_TEMP_DIRECTORY;
        File bpmnTempDir = new File(bpmnTemp);
        if (!bpmnTempDir.exists() && !bpmnTempDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + bpmnTempDir.getAbsolutePath());
        }

        File bpmnDir = new File(bpmnDirectory);
        if (!bpmnDir.exists() && !bpmnDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + bpmnDir.getAbsolutePath());
        }

        for (UploadedFileItem uploadedFile : fileItems) {
            String fileName = uploadedFile.getFileName();

            if (fileName == null || fileName.equals("")) {
                throw new AxisFault("Invalid file name. File name is not available");
            }

            if (uploadedFile.getFileType().equals(BPMNConstants.BPMN_PACKAGE_EXTENSION)) {
                try {
                    writeResource(uploadedFile.getDataHandler(), bpmnTemp, fileName, bpmnDir);
                } catch (IOException e) {
                    throw new AxisFault("IOError: Writing resource failed.", e);
                }
            } else {
                throw new AxisFault("Invalid file type : " + uploadedFile.getFileType() + " ." +
                                    BPMNConstants.BPMN_PACKAGE_EXTENSION +
                                    " file type is expected");
            }
        }

    }

    private void writeResource(DataHandler dataHandler, String destPath, String fileName,
                               File bpmnDest) throws IOException {
        File tempDestFile = new File(destPath, fileName);
        FileOutputStream fos = null;
        File destFile = new File(bpmnDest, fileName);
        try {
            fos = new FileOutputStream(tempDestFile);
            /* File stream is copied to a temp directory in order handle hot deployment issue
               occurred in windows */
            dataHandler.writeTo(fos);
            FileUtils.copyFile(tempDestFile, destFile);
        } catch (FileNotFoundException e) {
            log.error("Cannot find the file", e);
            throw e;
        } catch (IOException e) {
            log.error("IO error.");
            throw e;
        } finally {
            close(fos);
        }

        boolean isDeleted = tempDestFile.delete();
        if (!isDeleted) {
            log.warn("temp file: " + tempDestFile.getAbsolutePath() +
                     " deletion failed, scheduled deletion on server exit.");
            tempDestFile.deleteOnExit();
        }
    }

    public static void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            log.warn("Can't close file streams.", e);
        }
    }
}
