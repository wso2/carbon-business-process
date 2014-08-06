package org.wso2.carbon.bpel.common;

import javax.wsdl.Binding;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility methods related to SOAP
 */
public final class SOAPHelper {
    private SOAPHelper() {
    }

    public static ExtensibilityElement getBindingExtension(Binding binding) {
        Collection bindings = new ArrayList();
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), HTTPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAP12Binding.class);
        if (bindings.size() == 0) {
            return null;
        } else if (bindings.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException("Multiple bindings: " + binding.getQName());
        } else {
            // retrieve the single element
            return (ExtensibilityElement) bindings.iterator().next();
        }
    }
}
