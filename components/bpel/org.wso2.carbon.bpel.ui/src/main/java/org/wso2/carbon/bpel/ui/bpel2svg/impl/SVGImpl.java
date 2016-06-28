/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.ui.bpel2svg.impl;

import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.svg.SVGDocument;
import org.wso2.carbon.bpel.ui.bpel2svg.ProcessInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Handles the SVG generation
 */
public class SVGImpl implements SVGInterface {

    private ProcessInterface rootActivity = null;
    private Log log = LogFactory.getLog(SVGImpl.class);
    private String svgStr = null;

    private SVGDocument svgDoc = null;

    //Getter and Setter for the rootActivity

    /**
     * Gets the root activity of the process i.e. Process Activity
     *
     * @return root activity of the process i.e. Process Activity
     */
    public ProcessInterface getRootActivity() {
        return rootActivity;
    }

    /**
     * Sets the root activity of the process i.e. Process Activity
     *
     * @param rootActivity root activity of the process i.e. Process Activity
     */
    public void setRootActivity(ProcessInterface rootActivity) {
        this.rootActivity = rootActivity;
    }

    /*
    * Return the SVG graph as a SVG string
    * */
    public String generateSVGString() {
        try {
            //Define a String writer
            StringWriter writer = new StringWriter();
            //Get the SVG document from the root activity i.e. the Process Activity
            SVGDocument svgDoc = getRootActivity().getSVGDocument();
            if (svgDoc != null) {
                this.svgDoc = svgDoc;
            }
            //Method wrapper for SVGTranscoder.
            DOMUtilities.writeDocument(svgDoc, writer);
            writer.close();
            svgStr = writer.toString();
            return svgStr;
        } catch (IOException ioe) {
            log.error("Error Generating SVG String", ioe);
            return null;
        }
    }

    /*
    * Return the image as a base64 encoded string of a PNG
    * */
    public String toPNGBase64String() {
        // Create a JPEG transcoder
        JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
        // Set the transcoding hints.
        jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8));
        // Create the transcoder input.
        String inputString = getHeaders() + generateSVGString();  //svgDoc is set from generateSVGString()
        Reader stringReader = new StringReader(inputString);
        TranscoderInput transcoderInput2 = new TranscoderInput(stringReader);
        // Create the transcoder output.
        OutputStream osByteArray = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(osByteArray);
        try {
            jpegTranscoder.transcode(transcoderInput2, transcoderOutput);
        } catch (TranscoderException e) {
            log.error("JPEGTranscoder error", e);
            return null;
        }
        try {
            osByteArray.flush();
        } catch (IOException e) {
            log.error("Error while flushing OutputStreamByteArray", e);
            return null;
        }
        // Base64 encoding from byte array to string
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) osByteArray).toByteArray()), Charset
                .defaultCharset());
        return base64;
    }

    /*
    * Return the image as a byte array of a PNG
    * */
    public byte[] toPNGBytes() {
        // Create a JPEG transcoder
        JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
        // Set the transcoding hints.
        jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(0.8));
        // Create the transcoder input.
        String inputString = getHeaders() + generateSVGString();    //svgDoc is set from generateSVGString()
        Reader stringReader = new StringReader(inputString);
        TranscoderInput transcoderInput2 = new TranscoderInput(stringReader);
        // Create the transcoder output.
        OutputStream osByteArray = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(osByteArray);
        try {
            jpegTranscoder.transcode(transcoderInput2, transcoderOutput);
        } catch (TranscoderException e) {
            log.error("JPEGTranscoder transcode error", e);
            return new byte[0];
        }

        try {
            osByteArray.flush();
        } catch (IOException e) {
            log.error("Error while flushing OutputStreamByteArray", e);
            return new byte[0];
        }
        return ((ByteArrayOutputStream) osByteArray).toByteArray();
    }

    /**
     * @return String with the header of the SVG document
     */
    public String getHeaders() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<!DOCTYPE svg PUBLIC '" +
                SVGConstants.SVG_PUBLIC_ID + "' '" + SVGConstants.SVG_SYSTEM_ID + "'>\n\n";
    }
}
