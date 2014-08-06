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

package org.wso2.carbon.attachment.mgt.servlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.core.service.AttachmentManagerService;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This will provide the raw resource so that it could be accessed as via a valid URL.
 */
public class AttachmentDownloadServlet extends HttpServlet {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentDownloadServlet.class);

    /**
     * Logic that will be executed for a get request.
     *
     * @param request  the HTTP Servlet request.
     * @param response the HTTP Servlet response.
     * @throws ServletException if an error occurred.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        String attachmentUniqueID = url.substring(url.lastIndexOf("/") + 1);

        InputStream contentStream = null;
        ServletOutputStream servletOutputStream = null;
        try {
            TAttachment fileAttachment = getFileFromUniqueID(url);

            response.setHeader("Content-Disposition", "attachment; filename=" + fileAttachment.getName());
            response.setContentType(fileAttachment.getContentType());
            contentStream = fileAttachment.getContent().getInputStream();

            servletOutputStream = response.getOutputStream();

            IOUtils.copy(contentStream, servletOutputStream);

            servletOutputStream.flush();
        } catch (AttachmentMgtException e) {
            throw new ServletException(e.getLocalizedMessage(),e);
        } finally {
            IOUtils.closeQuietly(contentStream);
            IOUtils.closeQuietly(servletOutputStream);
        }


    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Generate an {@code InputStream} for the attachment specified by the {@code attachmentURL}
     *
     *
     * @param attachmentURL an unique url value which can be used to retrieve an attachment
     * @return {@code InputStream} relevant to the particular {@code attachmentURL}
     * @throws AttachmentMgtException
     */
    private TAttachment getFileFromUniqueID(String attachmentURL) throws AttachmentMgtException {
        AttachmentManagerService service = new AttachmentManagerService();
        try {
            TAttachment attachment = service.getAttachmentInfoFromURL(attachmentURL);
            return attachment;
        } catch (AttachmentMgtException e) {
            log.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

}
