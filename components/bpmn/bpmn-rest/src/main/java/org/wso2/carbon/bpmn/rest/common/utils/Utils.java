/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.bpmn.rest.common.utils;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    protected static final String DEFAULT_ENCODING = "UTF-8";

    private static final Log log = LogFactory.getLog(Utils.class);


    public static String resolveContentType(String resourceName) {
        String contentType = null;
        if (resourceName != null && !resourceName.isEmpty()) {
            String lowerResourceName = resourceName.toLowerCase();

            if (lowerResourceName.endsWith("png")) {
                contentType = "image/png";
            } else if (lowerResourceName.endsWith("xml") || lowerResourceName.endsWith("bpmn")) {
                contentType = "text/xml";
            }
        }
        return contentType;
    }

    public static String getClassResource(Class<?> klass) {
        return klass.getClassLoader().getResource(
                klass.getName().replace('.', '/') + ".class").toString();
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        }
    }

    public static byte[] processMultiPartFile(HttpServletRequest httpServletRequest, String contentMessage) throws
            IOException {
        //Content-Type: multipart/form-data; boundary="----=_Part_2_1843361794.1448198281814"

        String encoding = httpServletRequest.getCharacterEncoding();

        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
            httpServletRequest.setCharacterEncoding(encoding);
        }

        byte[] requestBodyArray = IOUtils.toByteArray(httpServletRequest.getInputStream());
        if (requestBodyArray == null || requestBodyArray.length == 0) {
            throw new ActivitiIllegalArgumentException("No :" + contentMessage + "was found in request body.");
        }

        String requestContentType = httpServletRequest.getContentType();

        StringBuilder contentTypeString = new StringBuilder();
        contentTypeString.append("Content-Type: " + requestContentType);
        contentTypeString.append("\r");
        contentTypeString.append(System.getProperty("line.separator"));

        byte[] contentTypeArray = contentTypeString.toString().getBytes(encoding);

        byte[] aggregatedRequestBodyByteArray = new byte[contentTypeArray.length + requestBodyArray.length];

        System.arraycopy(contentTypeArray, 0, aggregatedRequestBodyByteArray, 0, contentTypeArray.length);
        System.arraycopy(requestBodyArray, 0, aggregatedRequestBodyByteArray, contentTypeArray
                .length, requestBodyArray.length);

        boolean debugEnabled = log.isDebugEnabled();


        int index = requestContentType.indexOf("boundary");

        if (index <= 0) {
            throw new ActivitiIllegalArgumentException("boundary tag not found in the request header.");
        }
        String boundaryString = requestContentType.substring(index + "boundary=".length());
        boundaryString = boundaryString.replaceAll("\"", "").trim();

         if (debugEnabled) {
            log.debug("----------Content-Type:-----------\n" + httpServletRequest.getContentType());
            log.debug("\n\n\n\n");
            log.debug("\n\n\n\n----------Aggregated Request Body:-----------\n" + new String(aggregatedRequestBodyByteArray));
            log.debug("boundaryString:" + boundaryString);
        }

        byte[] boundary = boundaryString.getBytes();
        ByteArrayInputStream content = new ByteArrayInputStream(aggregatedRequestBodyByteArray);
        MultipartStream multipartStream = new MultipartStream(content, boundary, aggregatedRequestBodyByteArray.length,
                null);

        boolean nextPart = multipartStream.skipPreamble();
        if (debugEnabled) {
            log.debug(nextPart);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byteArray = null;
        // Get first file in the map, ignore possible other files
        while (nextPart) {
            //
            if (debugEnabled) {

                String header = multipartStream.readHeaders();
                System.out.println("==============Headers:==========================");
                System.out.println(header);
            }
            multipartStream.readBodyData(byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();

            nextPart = multipartStream.readBoundary();
        }

        return byteArray;
    }

    public static Map<String, String> populateRequestParams(List<String> propertiesList, UriInfo uriInfo) {
        Map<String, String> requestParams = new HashMap<>();
        for (String property : propertiesList) {
            String value = uriInfo.getQueryParameters().getFirst(property);

            if (value != null) {
                requestParams.put(property, value);
            }
        }
        return requestParams;
    }

    public static Map<String, String> prepareCommonParameters(Map<String, String> requestParams, UriInfo uriInfo) {
        String start = uriInfo.getQueryParameters().getFirst("start");
        if (start != null) {
            requestParams.put("start", start);
        }

        String size = uriInfo.getQueryParameters().getFirst("size");
        if (size != null) {
            requestParams.put("size", size);
        }

        String order = uriInfo.getQueryParameters().getFirst("order");
        if (order != null) {
            requestParams.put("order", order);
        }

        String sort = uriInfo.getQueryParameters().getFirst("sort");
        if (sort != null) {
            requestParams.put("sort", sort);
        }

        return requestParams;
    }

    public static String getValues(String fullString, String key) {

        int index = fullString.indexOf(key);
        String newValue2 = fullString.substring(index + key.length() + 1);
        int firstOccurance = newValue2.indexOf("\"");
        int secondOccurnace = newValue2.indexOf("\"", firstOccurance + 1);

        //System.out.println(.replaceAll("\"",""));
        return newValue2.substring(firstOccurance + 1, secondOccurnace);
    }


    public void processHeaders(String header, List<String> keyList){

       /* Content-Disposition: form-data; name="file"; filename="buffer_pool"
        Content-Type: application/octet-stream*/


    }

}
