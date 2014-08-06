/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.deployer.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.deployer.services.types.UploadedFileItem;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.*;

/**
 * BPEL package uploader admin service
 */
public class BPELUploader extends AbstractAdmin {
    private static Log log = LogFactory.getLog(BPELUploader.class);

    public void uploadService(UploadedFileItem[] fileItems) throws AxisFault {


        //First lets filter for jar resources
        ConfigurationContext configurationContext = getConfigContext();
        String repo = configurationContext.getAxisConfiguration().getRepository().getPath();


        if (CarbonUtils.isURL(repo)) {
            throw new AxisFault("URL Repositories are not supported: " + repo);
        }

        //Writting the artifacts to the proper location
        String bpelDirectory = repo + File.separator + BPELConstants.BPEL_REPO_DIRECTORY;

        String bpelTemp = CarbonUtils.getCarbonHome() + BPELConstants.BPEL_PACKAGE_TEMP_DIRECTORY;
        File bpelTempDir = new File(bpelTemp);
        if (!bpelTempDir.exists() && !bpelTempDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + bpelTempDir.getAbsolutePath());
        }

        File bpelDir = new File(bpelDirectory);
        if (!bpelDir.exists() && !bpelDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + bpelDir.getAbsolutePath());
        }

        for (UploadedFileItem uploadedFile : fileItems) {
            String fileName = uploadedFile.getFileName();

            if (fileName == null || fileName.equals("")) {
                throw new AxisFault("Invalid file name. File name is not available");
            }

            if (uploadedFile.getFileType().equals(BPELConstants.BPEL_PACKAGE_EXTENSION)) {
                try {
                    writeResource(uploadedFile.getDataHandler(), bpelTemp, fileName, bpelDir);
                } catch (IOException e) {
                    throw new AxisFault("IOError: Writing resource failed.", e);
                }
            } else {
                throw new AxisFault("Invalid file type : " + uploadedFile.getFileType() + " ." +
                                    BPELConstants.BPEL_PACKAGE_EXTENSION +
                                    " file type is expected");
            }
        }

    }

    private void writeResource(DataHandler dataHandler, String destPath, String fileName,
                               File bpelDest) throws IOException {
        File tempDestFile = new File(destPath, fileName);
        FileOutputStream fos = null;
        File destFile = new File(bpelDest, fileName);
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
