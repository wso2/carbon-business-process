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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.io.CachedOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;
import java.io.*;
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
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
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

        byte[] boundary = boundaryString.getBytes(encoding);
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
                printHeaders(header);
            }


            multipartStream.readBodyData(byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();

            nextPart = multipartStream.readBoundary();
        }

        return byteArray;
    }


    public static Map<String, String> processContentDispositionHeader(String headerValue) {

        boolean debugEnabled = log.isDebugEnabled();
        if (!headerValue.endsWith(";")) {
            headerValue += ";";
        }

        byte[] headerValueByteArray = headerValue.trim().substring("form_data".length() + 1).getBytes();

        int beginIndex = 0;
        int length = 0;
        String key = null;
        String value;
        boolean keyFound = false;
        boolean valueFound = false;

        Map<String, String> contentDispositionHeaderMap = new HashMap<String, String>();

        for (byte byte1 : headerValueByteArray) {

            ++length;

            if (!keyFound) {

                if (byte1 == '=') {

                    keyFound = true;
                    key = new String(headerValueByteArray, beginIndex, length - 1).trim();
                    beginIndex += length;
                    length = 0;
                    if (debugEnabled) {
                        log.debug("KEY:" + key);
                    }
                }
            } else {
                if (byte1 == '\n' || byte1 == '\r' || byte1 == ';') {

                    value = new String(headerValueByteArray, beginIndex, length - 1);
                    value = value.replaceAll("\"", "").trim();
                    keyFound = false;
                    beginIndex += length;
                    length = 0;
                    if (debugEnabled) {
                        log.debug("header value:" + value);
                    }

                    contentDispositionHeaderMap.put(key, value);
                    key = null;
                    value = null;
                }
            }
        }
        return contentDispositionHeaderMap;
    }


    public static OutputStream getAttachmentStream(InputStream inputStream) throws IOException {

        if (inputStream != null) {
            CachedOutputStream cachedOutputStream = new CachedOutputStream();
            IOUtils.copy(inputStream, cachedOutputStream);
            cachedOutputStream.close();

            return cachedOutputStream.getOut();
        }

        return null;
    }

    public static void printHeaders(String header) {

        boolean debugEnabled = log.isDebugEnabled();

        byte[] headerArrayByte = header.getBytes();

        if(debugEnabled){
            log.debug("==============Headers:==========================");
            log.debug(header);
        }

        int beginIndex = 0;
        int length = 0;

        String headerString = null;
        String headerValue = null;
        boolean headerFound = false;
        Map<String, String> headerMap = new HashMap<String, String>();
        for (byte headerByte : headerArrayByte) {

            ++length;

            if (!headerFound) {

                if (headerByte == ':') {
                    headerFound = true;
                    headerString = new String(headerArrayByte, beginIndex, length - 1);
                    beginIndex += length;
                    length = 0;
                    if(log.isDebugEnabled()){
                        log.debug("Header:" + headerString);
                    }
                }
            } else {

                if (headerByte == '\n' || headerByte == '\r') {
                    headerValue = new String(headerArrayByte, beginIndex, length - 1);
                    if(log.isDebugEnabled()){
                        log.debug("header value:" + headerValue);
                    }
                    headerFound = false;
                    beginIndex += length;
                    length = 0;

                    headerMap.put(headerString, headerValue);
                    headerString = null;
                    headerValue = null;
                }
            }

        }
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
        return newValue2.substring(firstOccurance + 1, secondOccurnace);
    }


    public void processHeaders(String header, List<String> keyList) {

       /* Content-Disposition: form-data; name="file"; filename="buffer_pool"
        Content-Type: application/octet-stream*/


    }

    /**
     * This function will check whether the HTTP Request is with Content-Type with application/json
     * @param httpServletRequest : httpServeletRequest
     * @return : true if the Content-Type is application/json, false otherwise
     */
    public static boolean isApplicationJsonRequest(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getContentType().trim().startsWith(MediaType.APPLICATION_JSON);
    }

    /**
     * This function will check whether the HTTP Request is with Content-Type with application/xml
     * @param httpServletRequest : httpServeletRequest
     * @return : true if the Content-Type is application/xml, false otherwise
     */
    public static boolean isApplicationXmlRequest(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getContentType().trim().startsWith(MediaType.APPLICATION_XML);
    }
}
