/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class FileUtils {
    private FileUtils() {
    }

    /**
     * Recursively collect all Files in the given directory and all its
     * subdirectories, applying the given FileFilter. The FileFilter is also applied to the given rootDirectory.
     * As a result the rootDirectory might be in the returned list.
     * <p>
     * Returned files are ordered lexicographically but for each directory, files come before its sudirectories.
     * For instance:<br/>
     * test<br/>
     * test/alpha.txt<br/>
     * test/zulu.txt<br/>
     * test/a<br/>
     * test/a/alpha.txt<br/>
     * test/z<br/>
     * test/z/zulu.txt<br/>
     * <p>
     * instead of:<br/>
     * test<br/>
     * test/a<br/>
     * test/a/alpha.txt<br/>
     * test/alpha.txt<br/>
     * test/z<br/>
     * test/z/zulu.txt<br/>
     * test/zulu.txt<br/>
     *
     * @param rootDirectory
     *          the top level directory used for the search
     * @param filter
     *          a FileFilter used for accepting/rejecting individual entries
     * @return a List of found Files
     */
    public static List<File> directoryEntriesInPath(File rootDirectory, FileFilter filter) {
        if (rootDirectory == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        if (!rootDirectory.exists()) {
            throw new IllegalArgumentException("File does not exist!");
        }

        ArrayList<File> collectedFiles = new ArrayList<File>(32);

        if (rootDirectory.isFile()) {
            if ((filter == null) || ((filter.accept(rootDirectory)))) {
                collectedFiles.add(rootDirectory);
            }
            return collectedFiles;
        }

        FileUtils.directoryEntriesInPath(collectedFiles, rootDirectory, filter);
        return collectedFiles;
    }

    private static void directoryEntriesInPath(List<File> collectedFiles, File parentDir, FileFilter filter) {
        if ((filter == null) || ((filter.accept(parentDir)))) {
            collectedFiles.add(parentDir);
        }

        File[] allFiles = parentDir.listFiles();
        if (allFiles != null) {
            TreeSet<File> dirs = new TreeSet<File>();
            TreeSet<File> acceptedFiles = new TreeSet<File>();
            for (File f : allFiles) {
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    if ((filter == null) || ((filter.accept(f)))) {
                        acceptedFiles.add(f);
                    }
                }
            }
            collectedFiles.addAll(acceptedFiles);
            for (File currentFile : dirs) {
                FileUtils.directoryEntriesInPath(collectedFiles, currentFile, filter);
            }
        }
    }
}
