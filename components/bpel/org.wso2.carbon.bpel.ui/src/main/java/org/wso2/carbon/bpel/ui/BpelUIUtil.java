/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpel.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.types.BpelDefinition;
import org.wso2.carbon.bpel.stub.mgt.types.EnableEventListType;
import org.wso2.carbon.bpel.stub.mgt.types.EndpointRef_type0;
import org.wso2.carbon.bpel.stub.mgt.types.InstanceStatus;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.ScopeEventType;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Some common UI functionalities, used in org.wso2.carbon.bpel.ui
 */
public final class BpelUIUtil {
    private static final String DISABLED = "disabled";
    private static final String CHECKED = "checked";
    private static Log log = LogFactory.getLog("bpel.ui");

    private BpelUIUtil() {
    }

    /**
     * Truncates a given text to the size specified. Appends "..." to the end.
     *
     * @param text
     * @param maxSize
     * @return Truncated string
     */
    public static String truncateString(String text, int maxSize) {
        long strLength = text.length();
        if (strLength > maxSize) {
            if (log.isDebugEnabled()) {
                log.debug("Limiting variable size of the instance to:" + maxSize);
            }
            return text.substring(0, maxSize) + " ...";
        }
        return text;
    }

    public static String encodeHTML(String aText) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '&') {
                result.append("&amp;");
//            } else if (character == '\"') {
//                result.append("&quot;");
//            } else if (character == '\t') {
//                addCharEntity(9, result);
//            } else if (character == '!') {
//                addCharEntity(33, result);
//            } else if (character == '#') {
//                addCharEntity(35, result);
//            } else if (character == '$') {
//                addCharEntity(36, result);
//            } else if (character == '%') {
//                addCharEntity(37, result);
//            } else if (character == '\'') {
//                addCharEntity(39, result);
//            } else if (character == '(') {
//                addCharEntity(40, result);
//            } else if (character == ')') {
//                addCharEntity(41, result);
//            } else if (character == '*') {
//                addCharEntity(42, result);
//            } else if (character == '+') {
//                addCharEntity(43, result);
//            } else if (character == ',') {
//                addCharEntity(44, result);
//            } else if (character == '-') {
//                addCharEntity(45, result);
//            } else if (character == '.') {
//                addCharEntity(46, result);
//            } else if (character == '/') {
//                addCharEntity(47, result);
//            } else if (character == ':') {
//                addCharEntity(58, result);
//            } else if (character == ';') {
//                addCharEntity(59, result);
//            } else if (character == '=') {
//                addCharEntity(61, result);
//            } else if (character == '?') {
//                addCharEntity(63, result);
//            } else if (character == '@') {
//                addCharEntity(64, result);
//            } else if (character == '[') {
//                addCharEntity(91, result);
//            } else if (character == '\\') {
//                addCharEntity(92, result);
//            } else if (character == ']') {
//                addCharEntity(93, result);
//            } else if (character == '^') {
//                addCharEntity(94, result);
//            } else if (character == '_') {
//                addCharEntity(95, result);
//            } else if (character == '`') {
//                addCharEntity(96, result);
//            } else if (character == '{') {
//                addCharEntity(123, result);
//            } else if (character == '|') {
//                addCharEntity(124, result);
//            } else if (character == '}') {
//                addCharEntity(125, result);
//            } else if (character == '~') {
//                addCharEntity(126, result);
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static String prettyPrint(String rawXML) {
        String tRawXML = rawXML;
        tRawXML = tRawXML.replaceAll("\n|\\r|\\f|\\t", "");
        tRawXML = tRawXML.replaceAll("> +<", "><");
        //InputStream xmlIn = new ByteArrayInputStream(tRawXML.getBytes());
        //XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
        //tRawXML = xmlPrettyPrinter.xmlFormat();
//        rawXML = rawXML.replaceAll("\n", "<br>");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OMElement elem;
        try {
            elem = AXIOMUtil.stringToOM(tRawXML);
            XMLPrettyPrinter.prettify(elem, stream);
        } catch (XMLStreamException e) {
            log.error("Error processing the XML message " + e);
        } catch (Exception e) {
            log.error("Error processing the XML message " + e);
        }

        return new String(stream.toByteArray(), Charset.defaultCharset()).trim();
    }

    public static Map<String, QName> getEndpointRefsMap(ProcessInfoType info) {
        EndpointRef_type0[] endpoints = info.getEndpoints().getEndpointRef();

        Map<String, QName> pLinkServiceMap = new TreeMap<String, QName>();
        for (EndpointRef_type0 ref : endpoints) {
            pLinkServiceMap.put(ref.getPartnerLink(), ref.getService());
        }
        return pLinkServiceMap;
    }

    public static Map<String, EndpointRef_type0> getEndpointReferences(ProcessInfoType info) {
        EndpointRef_type0[] endpoints = info.getEndpoints().getEndpointRef();

        Map<String, EndpointRef_type0> pLinkEprMap = new TreeMap<String, EndpointRef_type0>();
        for (EndpointRef_type0 eprRef : endpoints) {
            pLinkEprMap.put(eprRef.getPartnerLink(), eprRef);
        }

        return pLinkEprMap;
    }

    public static String getUndeployLink(String packageName) {
        return "process_list.jsp?operation=undeploy&packageName=" + packageName;
    }

    public static String getRetireLink(String pid, String filter, String order, int pageNumber, String packageName) {
        String encodedPid = pid;
        try {
            encodedPid = URLEncoder.encode(pid, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding the url " + pid + e);
        }
        String link = "process_list.jsp?operation=retire&processID=" + encodedPid + "&filter=" + filter +
                "&order=" + order + "&pageNumber=" + pageNumber + "&retiredPackageName=" + packageName;

        return link;
    }

    public static String getActivateLink(String pid, String filter, String order, int pageNumber) {
        String encodedPid = pid;
        try {
            encodedPid = URLEncoder.encode(pid, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding the url " + pid + e);
        }

        String link = "process_list.jsp?operation=activate&processID=" + encodedPid + "&filter=" + filter +
                "&order=" + order + "&pageNumber=" + pageNumber;

        return link;
    }

    public static QName stringToQName(String pid) {
        int endURI = pid.indexOf('}');
        return new QName(pid.substring(1, endURI), pid.substring(endURI + 1));
    }

    /*private String[] getServiceLocations(ProcessInfoType info) {
        String[] serviceLocations = info.getEndpoints().getEndpointRef()[0].getServiceLocations().getServiceLocation();
        //Removing the processEPR

        return null;
    }*/

    public static String getProcessDefinition(ProcessInfoType info) {
        BpelDefinition definition = info.getDefinitionInfo().getDefinition();
        return definition.getExtraElement().toString();
    }

    public static String getInstanceOperations(InstanceStatus status, String iid) {
        ResourceBundle rsc = ResourceBundle.getBundle("org.wso2.carbon.bpel.ui.i18n.Resources");
        StringBuilder strBuilder = new StringBuilder();
        if (status.equals(InstanceStatus.ACTIVE)) {
            generateActiveActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.COMPLETED)) {
            generateCompletedActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.SUSPENDED)) {
            generateSuspendedActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.TERMINATED)) {
            generateTerminatedActionLinks(strBuilder, rsc, iid);
            // } else if (status.equals(InstanceStatus.ERROR)) {
            //    generateErrorActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.FAILED)) {
            // For failed instances only delete operation is available.
            generateTerminatedActionLinks(strBuilder, rsc, iid);
        }

        return strBuilder.toString();
    }

    private static void generateActiveActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                  String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateSuspendLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateTerminateLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

    private static void generateCompletedActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                     String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

    private static void generateSuspendedActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                     String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateResumeLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateTerminateLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

    private static void generateTerminatedActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                      String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

//    private static void generateErrorActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
//                                                 String iid) {
//        generateTDStart(strBuilder);
//        generateStartTag(strBuilder);
//        generateTerminateLink(strBuilder, rsc, iid);
//        generateEndTag(strBuilder);
//        generateStartTag(strBuilder);
//        generateDeleteLink(strBuilder, rsc, iid);
//        generateEndTag(strBuilder);
//        generateTDEnd(strBuilder);
//    }

    private static void generateSuspendLink(StringBuilder strBuilder, ResourceBundle rsc,
                                            String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "suspend");
        strBuilder.append("&operation=suspend'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/suspend.gif);'>");
        generateOnSuspendEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("suspend"));
        strBuilder.append("</a>");
    }

    private static void generateResumeLink(StringBuilder strBuilder, ResourceBundle rsc,
                                           String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "resume");
        strBuilder.append("&operation=resume'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/resume.gif);'>");
        generateOnResumeEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("resume"));
        strBuilder.append("</a>");
    }

    private static void generateTerminateLink(StringBuilder strBuilder, ResourceBundle rsc,
                                              String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "terminate");
        strBuilder.append("&operation=terminate'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/terminate.gif);'>");
        generateOnTerminateEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("terminate"));
        strBuilder.append("</a>");
    }

    private static void generateDeleteLink(StringBuilder strBuilder, ResourceBundle rsc,
                                           String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "delete");
        strBuilder.append("&operation=delete'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/delete.gif);'>");
        generateOnDeleteEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("delete"));
        strBuilder.append("</a>");
    }

    private static void generateOnDeleteEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instancedelete");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onDelete);})();</script>");
    }

    private static void generateOnResumeEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instanceresume");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onResume);})();</script>");
    }

    private static void generateOnSuspendEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instancesuspend");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onSuspend);})();</script>");
    }

    private static void generateOnTerminateEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instanceterminate");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onTerminate);})();</script>");
    }


    private static void generateFirstPartOfTheLink(StringBuilder strBuilder, String iid,
                                                   String operation) {
        strBuilder.append("<a id='instance");
        strBuilder.append(operation);
        strBuilder.append(iid);
        strBuilder.append("' href='list_instances.jsp?iid=");
        strBuilder.append(iid);
    }

    private static void generateStartTag(StringBuilder strBuilder) {
        //strBuilder.append("<td style='border-right:0px;border-left:0px;'>");
        strBuilder.append("[ ");
    }

    private static void generateEndTag(StringBuilder strBuilder) {
        strBuilder.append(" ] ");
    }

    private static void generateTDEnd(StringBuilder strBuilder) {
        strBuilder.append("</td>");
    }

    private static void generateTDStart(StringBuilder strbuilder) {
        strbuilder.append("<td style=\"white-space: nowrap\">");
    }

    public static void logWarn(String message, Exception e) {
        if (log.isWarnEnabled()) {
            log.warn(message, e);
        }
    }

    public static String generateRetireLinkForProcessInfoPage(String processId) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("process_info.jsp?Pid=");
        strBuilder.append(processId);
        strBuilder.append("&operation=retire");

        return strBuilder.toString();
    }

    public static String generateActivateLinkForProcessInfoPage(String processId) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("process_info.jsp?Pid=");
        strBuilder.append(processId);
        strBuilder.append("&operation=activate");

        return strBuilder.toString();
    }

    /**
     * when scope events defined in deploy.xml is changed in runtime by DD Editor this method is used to get the
     * updated events list
     * assigned with each scope event. It is got as a string array with the data about the events selected and
     * categories into (5 for each) groups. Then these events list is again written back to the corresponding scope
     * event
     */
    public static void updateScopeEvents(String[] selecttype, List<String> scopeNames,
                                         DeploymentDescriptorUpdater deployDescriptorUpdater) {
        ArrayList<String> valueArray = selecttype != null ?
                new ArrayList<String>(Arrays.asList(selecttype)) : new ArrayList<String>();
        ListIterator<String> it = valueArray.listIterator();
        while (it.hasNext()) {
            String nextVal = it.next();
            if (!nextVal.equalsIgnoreCase("0") && it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        String[] allEnabledEvents = valueArray.toArray(new String[valueArray.size()]);
        ScopeEventType[] scopeEventTypes = new ScopeEventType[scopeNames.size()];
        for (int j = 0; j < scopeNames.size(); j++) {
            ScopeEventType scopeEventType = new ScopeEventType();
            scopeEventType.setScope(scopeNames.get(j));
            String[] events = new String[5];
            System.arraycopy(allEnabledEvents, 0, events, 0, 5);
            ArrayList<String> actualEventsList = new ArrayList<String>();
            for (String event : events) {
                if (!event.equalsIgnoreCase("0")) {
                    actualEventsList.add(event);
                }
            }
            String[] eventsArray = actualEventsList.toArray(new String[actualEventsList.size()]);

            EnableEventListType enableEventListType = new EnableEventListType();
            enableEventListType.setEnableEvent(eventsArray);
            scopeEventType.setEnabledEventList(enableEventListType);

            scopeEventTypes[j] = scopeEventType;
        }
        deployDescriptorUpdater.setScopeEvents(scopeEventTypes);

    }
}
