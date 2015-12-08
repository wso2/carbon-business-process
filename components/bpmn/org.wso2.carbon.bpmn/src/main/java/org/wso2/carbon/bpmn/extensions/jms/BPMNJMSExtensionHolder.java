package org.wso2.carbon.bpmn.extensions.jms;

/**
 * Created by dilini on 11/30/15.
 */
public class BPMNJMSExtensionHolder {

    private static BPMNJMSExtensionHolder bpmnJmsHolder = new BPMNJMSExtensionHolder();

    private JMSInvoker jmsInvoker;

    private BPMNJMSExtensionHolder(){}

    public static BPMNJMSExtensionHolder getInstance(){ return bpmnJmsHolder; }

    public synchronized JMSInvoker getJMSInvoker(){ return jmsInvoker; }

    public void setJmsInvoker(JMSInvoker jmsInvoker){ this.jmsInvoker = jmsInvoker; }
}
