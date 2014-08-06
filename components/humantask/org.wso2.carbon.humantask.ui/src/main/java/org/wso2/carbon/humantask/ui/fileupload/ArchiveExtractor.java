/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public final class ArchiveExtractor {
    private static Log log = LogFactory.getLog(ArchiveExtractor.class);

    private ArchiveExtractor() {
    }

    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        out.close();
    }

    public static void extract(File file, String destination) throws Exception {
        try {
            ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Extracting directory " + entry.getName());
                    }

                    File tempDir = new File(destination, entry.getName());

                    if ( !tempDir.getParentFile().exists() && !(tempDir.getParentFile().mkdirs())) {
                        String errMsg = "Error occurred while creating directory: " +
                                tempDir.getParentFile() + " while extracting the archive: " + file.getName();
                        log.error(errMsg);
                        throw new Exception(errMsg);
                    }

                    if ( !tempDir.exists() && !(tempDir.mkdir())) {
                        String errMsg = "Error occurred while creating directory: " +
                                entry.getName() + " while extracting the archive: " + file.getName();
                        log.error(errMsg);
                        throw new Exception(errMsg);
                    }
                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Extracting file " + entry.getName());
                }

                File destFile = new File(destination, entry.getName());
                if (!destFile.getParentFile().exists() && !(destFile.getParentFile().mkdirs())) {
                    String errMsg = "Creating directory: " +
                            destFile.getParentFile().getName() +
                            " while extracting the archive: " + file.getName();
                    log.error(errMsg);
                    throw new Exception(errMsg);
                }
                copyInputStream(zipStream, new BufferedOutputStream(new FileOutputStream(destFile)));
            }

            zipStream.close();
        } catch (IOException e) {
            log.error("Error occurred during archive extracting.", e);
            throw new Exception("Error occurred during archive extracting.", e);
        }
    }


}
