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
package org.wso2.carbon.bpel.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Basic archive extracting operations
 */
public final class ArchiveExtractor {
    private static Log log = LogFactory.getLog(ArchiveExtractor.class);

    private ArchiveExtractor() {
    }

    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        } finally {
            out.close();
        }
    }

    public static void extract(File file, String destination) throws Exception {
        ZipInputStream zipStream = null;
        try {

            zipStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Extracting directory " + entry.getName());
                    }

                    //Ignore hidden directories
                    if (entry.getName().startsWith(".")) {
                        continue;
                    }

                    File dir = new File(destination, entry.getName());
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
                    }

                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Extracting file " + entry.getName());
                }

                File destFile = new File(destination, entry.getName());
                if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                    throw new IOException("Fail to create the directory: " +
                            destFile.getParentFile().getAbsolutePath());
                }
                copyInputStream(zipStream, new BufferedOutputStream(new FileOutputStream(destFile)));
            }

        } catch (IOException e) {
            log.error("Error occurred during archive extracting.", e);
            throw new Exception("Error occurred during archive extracting.", e);
        } finally {
            if (zipStream != null) {
                zipStream.close();
            }
        }
    }


}
