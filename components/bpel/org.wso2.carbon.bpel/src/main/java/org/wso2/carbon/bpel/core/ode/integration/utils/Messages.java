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

package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.io.File;
import java.text.MessageFormat;
import java.net.MalformedURLException;

public final class Messages {
    private Messages() {
    }

    public static String msgRegistryInitializationFailure() {
        return "Process Store creation failed due to Registry creation failure.";
    }

    public static String msgDeployStarting(File deploymentUnitDirectory) {
        return format("Starting deployment of processes from directory \"{0}\". ", deploymentUnitDirectory);
    }

    public static String msgDeployFailCompileErrors(CompilationException ce) {
        if (ce != null) {
            return format("Deploy failed; {0}", ce.getMessage());
        } else {
            return format("Deploy failed; BPEL compilation errors.");
        }
    }

    public static String msgDeployFailDuplicateDU(String name) {
        return format("Deploy failed; Deployment Unit \"{0}\" already deployed!", name);
    }

    public static String msgDeployFailDuplicatePID(QName processId, String name) {
        return format("Deploy failed; process \"{0}\" already deployed!", processId);
    }

    public static String msgDeployFailedProcessNotFound(QName pid, String du) {
        return format("Deploy failed; process \"{0}\" not found in deployment unit \"{1}\".", pid, du);
    }

    public static String msgProcessDeployed(Object dir, QName processId) {
        return format("Process {1} deployed from \"{1}\".", dir, processId);
    }

    public static String msgProcessUndeployed(QName process) {
        return format("Process {0} has been undeployed.", process);
    }

    public static String msgProcessNotFound(QName pid) {
        return format("Process {0} not found. ", pid);
    }

    public static String msgAddingDeploymentUnitToRegistryFailed(String name, RegistryException e){
        return format("Adding deployment unit {0} to regstry failed;{1}", name, e.getMessage());
    }

    public static String msgDatabaseOutOfSyncForDU(String du){
        return format("Database out of synch for DU {0}", du);
    }

    public static String msgRemoteRegistryConfigurationInvalid(){
        return "Remote Registry Configuration Invalid.";
    }

    public static String msgMalformedRegistryURL(MalformedURLException me){
        return format("Malformed Registry URL; {0}", me.getMessage());
    }

    public static String msgRegistryTypeUnkwon(String type){
        return format("Unknow Registry Type: {0}", type);
    }

    public static String msgResourceDoesNotExistsForRecentDU(String name){
        return format("Resource does not exists for in regsitry for the recently added DU: {0}", name);
    }

    public static String msgUnsupportedConfigurationElement(String name){
        return format("Config element {0} not supported.", name);        
    }

    public static String msgServiceDescLocationNotFound(){
        return "Service Description Location not found";
    }

    public static String msgPolicyLocationNotFound(){
        return "Policy Location not found";
    }

    public static String msgUrlNotFoundInAddress(){
        return "Address element found without url attribute.";
    }

    public static String msgHttpOptionValueNotFound(String option){
        return format("Http Option {0} value not found.", option);
    }

    public static String msgElementAttributeValueNotFound(String element, String attribute){
        return format("{0} element's attribute {1} not found. Invalid config element.", element, attribute);
    }

    public static String msgConnectionTimeOutWrongNumberFormat(String number){
        return format("Wrong number format in connection timeout value: {0}", number);
    }

    public static String msgSocketTimeOutWrongNumberFormat(String number){
        return format("Wrong number format in socket timeout value: {0}", number);
    }

    public static String msgNumberFormatExceptionInMaxRedirects(){
        return "Number format exception occurred while parsing max redirects.";
    }

    public static String msgNoHostForProxy(){
        return "No host for proxy.";
    }

    public static String msgNumberFormatExceptionInPort(){
        return "Number format exception in port value.";
    }

    public static String msgExceptionDuringPackageConfigRead(){
        return "Error occurred during package config file read. The package configuration will not be available to BPEL engine.";
    }

    public static String msgPackageConfigurationFileNotFound(){
        return "Package configuration file not found. The package configuration will not be available to BPEL engine.";
    }

    public static String msgErrorSendingMessageToAxisForODEMex(String partnerRoleMessageExchange){
        return format("Error sending message to Axis2 for ODE mex {0}", partnerRoleMessageExchange);
    }

    public static String msgServiceDefinitionNotFound(String service){
        return format("Service definition not found for service {0}", service);
    }

    public static String msgBindingNotFound(String service, String port){
        return format("Binding not found for service {0} and port {1}", service, port);
    }

    public static String msgServicePortNotFound(String service, String port){
        return format("Service port not found for service {0} and port {1}", service, port);
    }

    public static String msgBindingNotSupported(String service, String port){
        return format("Binding type not supported for service {0} and port {1}", service, port);
    }

    private static String format(String message) {
        return message;
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        } else {
            return new MessageFormat(message).format(args);
        }
    }
}
