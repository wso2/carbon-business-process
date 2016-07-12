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

package org.wso2.carbon.bpel.ui.bpel2svg;

import org.wso2.carbon.bpel.ui.bpel2svg.impl.AssignImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.CompensateImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.CompensateScopeImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ElseIfImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ElseImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.EmptyImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ExitImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.FlowImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ForEachImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.IfImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.InvokeImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.OnAlarmImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.OnEventImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.OnMessageImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.PickImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ProcessImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ReThrowImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ReceiveImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.RepeatUntilImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ReplyImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ScopeImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.ThrowImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.WaitImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.WhileImpl;

/**
 * Manage the tag names, and the tag icon locations
 */
public class BPEL2SVGFactory {
    // Constants
    // START_TAGS
    public static final String ASSIGN_START_TAG = "assign";
    public static final String CATCH_START_TAG = "catch";
    public static final String CATCHALL_START_TAG = "catchAll";
    public static final String COMPENSATESCOPE_START_TAG = "compensateScope";
    public static final String COMPENSATE_START_TAG = "compensate";
    public static final String COMPENSATIONHANDLER_START_TAG = "compensationHandler";
    public static final String ELSE_START_TAG = "else";
    public static final String ELSEIF_START_TAG = "elseif";
    public static final String EVENTHANDLER_START_TAG = "eventHandlers";
    public static final String EXIT_START_TAG = "exit";
    public static final String FAULTHANDLER_START_TAG = "faultHandlers";
    public static final String FLOW_START_TAG = "flow";
    public static final String FOREACH_START_TAG = "forEach";
    public static final String IF_START_TAG = "if";
    public static final String INVOKE_START_TAG = "invoke";
    public static final String ONALARM_START_TAG = "onAlarm";
    public static final String ONEVENT_START_TAG = "onEvent";
    public static final String ONMESSAGE_START_TAG = "onMessage";
    public static final String PICK_START_TAG = "pick";
    public static final String PROCESS_START_TAG = "process";
    public static final String RECEIVE_START_TAG = "receive";
    public static final String REPEATUNTIL_START_TAG = "repeatUntil";
    public static final String REPLY_START_TAG = "reply";
    public static final String RETHROW_START_TAG = "rethrow";
    public static final String SCOPE_START_TAG = "scope";
    public static final String SEQUENCE_START_TAG = "sequence";
    public static final String SOURCE_START_TAG = "source";
    public static final String SOURCES_START_TAG = "sources";
    public static final String TARGET_START_TAG = "target";
    public static final String TARGETS_START_TAG = "targets";
    public static final String TERMINATIONHANDLER_START_TAG = "terminationHandler";
    public static final String THROW_START_TAG = "throw";
    public static final String WAIT_START_TAG = "wait";
    public static final String WHILE_START_TAG = "while";
    public static final String EMPTY_START_TAG = "empty";
    // END_TAGS
    public static final String ASSIGN_END_TAG = "/assign";
    public static final String CATCH_END_TAG = "/catch";
    public static final String CATCHALL_END_TAG = "/catchAll";
    public static final String COMPENSATESCOPE_END_TAG = "/compensateScope";
    public static final String COMPENSATE_END_TAG = "/compensate";
    public static final String COMPENSATIONHANDLER_END_TAG = "/compensationHandler";
    public static final String ELSE_END_TAG = "/else";
    public static final String ELSEIF_END_TAG = "/elseif";
    public static final String EVENTHANDLER_END_TAG = "/eventHandlers";
    public static final String EXIT_END_TAG = "/exit";
    public static final String FAULTHANDLER_END_TAG = "/faultHandlers";
    public static final String FLOW_END_TAG = "/flow";
    public static final String FOREACH_END_TAG = "/forEach";
    public static final String IF_END_TAG = "/if";
    public static final String INVOKE_END_TAG = "/invoke";
    public static final String ONMESSAGE_END_TAG = "/onMessage";
    public static final String ONALARM_END_TAG = "/onAlarm";
    public static final String ONEVENT_END_TAG = "/onEvent";
    public static final String PICK_END_TAG = "/pick";
    public static final String PROCESS_END_TAG = "/process";
    public static final String RECEIVE_END_TAG = "/receive";
    public static final String REPEATUNTIL_END_TAG = "/repeatUntil";
    public static final String REPLY_END_TAG = "/reply";
    public static final String RETHROW_END_TAG = "/rethrow";
    public static final String SCOPE_END_TAG = "/scope";
    public static final String SEQUENCE_END_TAG = "/sequence";
    public static final String SOURCE_END_TAG = "/source";
    public static final String SOURCES_END_TAG = "/sources";
    public static final String TARGET_END_TAG = "/target";
    public static final String TARGETS_END_TAG = "/targets";
    public static final String TERMINATIONHANDLER_END_TAG = "/terminationHandler";
    public static final String THROW_END_TAG = "/throw";
    public static final String WAIT_END_TAG = "/wait";
    public static final String WHILE_END_TAG = "/while";
    public static final String EMPTY_END_TAG = "/empty";

    public static final String SINGLE_LINE_END_TAG = "/>";
    public static final int TEXT_ADJUST = 10;
    private static volatile BPEL2SVGFactory instance = null;
    // Properties
    //Variable with the source of the images/icons
    public String iconSource = "images/bpel2svg";
    public LayoutManager layoutManager = null;
    // Icon Extension for the activity icons
    private String iconExtension = ".png";

    /**
     * @return instance of a BPEL2SVGFactory
     */
    public static BPEL2SVGFactory getInstance() {
        if (instance == null) {
            instance = new BPEL2SVGFactory();
        }
        return instance;
    }

    /**
     * @return instance of LayoutManager
     */
    public LayoutManager getLayoutManager() {
        if (layoutManager == null) {
            layoutManager = new LayoutManager();
        }
        return layoutManager;
    }

    /**
     * Sets the layoutManager
     *
     * @param layoutManager
     */
    public void setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    //Getter and Setter of the icon extension of the activity icons

    /**
     * Gets the extension of the activity icon
     *
     * @return String with the extension of the activity icon
     */
    public String getIconExtension() {
        return iconExtension;
    }

    /**
     * Sets the extension of the activity icon
     *
     * @param iconExtension extension of the activity icon
     */
    public void setIconExtension(String iconExtension) {
        this.iconExtension = iconExtension;
    }

    /**
     * Gets the start icon path of each activity
     *
     * @param activity String with the activity type/name
     * @return String with the start icon path relevant to each activity according to the activity type/name
     */
    public String getIconPath(String activity) {
        String iconPath = null;
        if (activity != null) {
            if (activity.equalsIgnoreCase(AssignImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ASSIGN_ICON;
            } else if (activity.equalsIgnoreCase(EmptyImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.EMPTY_ICON;
            } else if (activity.equalsIgnoreCase(ElseIfImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ELSEIF_ICON;
            } else if (activity.equalsIgnoreCase(ElseImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ELSE_ICON;
            } else if (activity.equalsIgnoreCase(CompensateImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.COMPENSATE_ICON;
            } else if (activity.equalsIgnoreCase(CompensateScopeImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.COMPENSATESCOPE_ICON;
            } else if (activity.equalsIgnoreCase(ExitImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.EXIT_ICON;
            } else if (activity.equalsIgnoreCase(FlowImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.FLOW_ICON;
            } else if (activity.equalsIgnoreCase(ForEachImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.FOREACH_ICON;
            } else if (activity.equalsIgnoreCase(IfImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.IF_ICON;
            } else if (activity.equalsIgnoreCase(InvokeImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.INVOKE_ICON;
            } else if (activity.equalsIgnoreCase(OnAlarmImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ONALARM_ICON;
            } else if (activity.equalsIgnoreCase(OnEventImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ONEVENT_ICON;
            } else if (activity.equalsIgnoreCase(OnMessageImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ONMESSAGE_ICON;
            } else if (activity.equalsIgnoreCase(PickImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.PICK_ICON;
            } else if (activity.equalsIgnoreCase(ProcessImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.START_ICON;
            } else if (activity.equalsIgnoreCase(ReceiveImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.RECEIVE_ICON;
            } else if (activity.equalsIgnoreCase(RepeatUntilImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.REPEATUNTIL_ICON;
            } else if (activity.equalsIgnoreCase(ReplyImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.REPLY_ICON;
            } else if (activity.equalsIgnoreCase(ReThrowImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.RETHROW_ICON;
            } else if (activity.equalsIgnoreCase(ScopeImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.SCOPE_ICON;
            } else if (activity.equalsIgnoreCase(ThrowImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.THROW_ICON;
            } else if (activity.equalsIgnoreCase(WaitImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.WAIT_ICON;
            } else if (activity.equalsIgnoreCase(WhileImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.WHILE_ICON;
            }
        }

        return iconPath;
    }

    /**
     * Gets the end icon path of each activity
     *
     * @param activity String with the activity type/name
     * @return String with the end icon path relevant to each activity according to the activity type/name
     */
    public String getEndIconPath(String activity) {
        String iconPath = null;
        if (activity != null) {
            if (activity.equalsIgnoreCase(FlowImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.FLOW_ICON;
            } else if (activity.equalsIgnoreCase(ForEachImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ENDFOREACH_ICON;
            } else if (activity.equalsIgnoreCase(IfImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ENDIF_ICON;
            } else if (activity.equalsIgnoreCase(PickImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.PICK_ICON;
            } else if (activity.equalsIgnoreCase(ProcessImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.END_ICON;
            } else if (activity.equalsIgnoreCase(RepeatUntilImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ENDREPEATUNTIL_ICON;
            } else if (activity.equalsIgnoreCase(ScopeImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.SCOPE_ICON;
            } else if (activity.equalsIgnoreCase(WhileImpl.class.getName())) {
                iconPath = BPEL2SVGIcons.ENDWHILE_ICON;
            }
        }
        return iconPath;
    }
    //Getter and Setter of the image source of the activity icons

    /**
     * Gets the source of the activity icon
     *
     * @return String with the source of the activity icon
     */
    public String getIconSource() {
        return iconSource;
    }

    /**
     * Sets the source of the activity icon
     *
     * @param iconSource source of the activity icon
     */
    public void setIconSource(String iconSource) {
        this.iconSource = iconSource;
    }
}
