///*
// * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.wso2.carbon.bpmn.extensions;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import javax.xml.stream.XMLStreamException;
//
///**
// *
// */
//public class BPMNRestExtensionHolder {
//
//    private static final Logger log = LoggerFactory.getLogger(BPMNRestExtensionHolder.class);
//
//    private static BPMNRestExtensionHolder bpmnRestHolder = new BPMNRestExtensionHolder();
//    private RESTInvoker restInvoker = null;
//
//    private BPMNRestExtensionHolder() {
//        synchronized (this) {
//            try {
//                restInvoker = new RESTInvoker();
//            } catch (IOException | XMLStreamException e) {
//                log.error("Unable to initialize RestInvoker.", e);
//            }
//        }
//    }
//
//
//    public static BPMNRestExtensionHolder getInstance() {
//        return bpmnRestHolder;
//    }
//
//    public synchronized RESTInvoker getRestInvoker() {
//        return restInvoker;
//    }
//
//}
