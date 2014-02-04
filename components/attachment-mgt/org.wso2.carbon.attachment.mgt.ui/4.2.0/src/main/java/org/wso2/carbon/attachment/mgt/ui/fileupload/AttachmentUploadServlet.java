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

package org.wso2.carbon.attachment.mgt.ui.fileupload;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;

@Deprecated
/**
 * This impl was moved to human task bundle. So recommended to remove.
 */
public class AttachmentUploadServlet extends HttpServlet {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentUploadServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /*response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        AttachmentUploadClient client = new AttachmentUploadClient();
*/

    }

    /**
     * Get the inputstream from the request and generate a file from t at the tmp directory.
     */
    private File generateTemporaryFile(HttpServletRequest request) throws ServletException, IOException {
        log.warn("This code can be improved if reviewed thoroughly. Here we get the file from input stream and create" +
                 " a temp file at \"tmp\" directory. May be we can avoid this file creation.");
        String contentType = request.getContentType();
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
            DataInputStream inputStream = new DataInputStream(request.getInputStream());
            //we are taking the length of Content type data
            int formDataLength = request.getContentLength();
            byte dataBytes[] = new byte[formDataLength];
            int byteRead = 0;
            int totalBytesRead = 0;
            //this loop converting the uploaded file into byte code
            while (totalBytesRead < formDataLength) {
                byteRead = inputStream.read(dataBytes, totalBytesRead, formDataLength);
                totalBytesRead += byteRead;
            }
            String file = new String(dataBytes);
            //for saving the file name
            String saveFile = file.substring(file.indexOf("filename=\"") + 10);
            saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
            saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1, saveFile.indexOf("\""));
            int lastIndex = contentType.lastIndexOf("=");
            String boundary = contentType.substring(lastIndex + 1, contentType.length());
            int pos;
            //extracting the index of file
            pos = file.indexOf("filename=\"");
            pos = file.indexOf("\n", pos) + 1;
            pos = file.indexOf("\n", pos) + 1;
            pos = file.indexOf("\n", pos) + 1;
            int boundaryLocation = file.indexOf(boundary, pos) - 4;
            int startPos = ((file.substring(0, pos)).getBytes()).length;
            int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;

            //TODO log.warn("Hard-coded directory path:" + "\"tmp/\"");
            File tmpFile = new File("tmp" + File.separator + saveFile);
            FileOutputStream fileOut = new FileOutputStream(tmpFile);
            fileOut.write(dataBytes, startPos, (endPos - startPos));
            fileOut.flush();
            IOUtils.closeQuietly(fileOut);
            IOUtils.closeQuietly(inputStream);

            return tmpFile;
        } else {
            throw new ServletException("Content type of the request is not multipart/form-data");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
