/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.runtime.xpath;

import net.sf.saxon.value.DurationValue;
import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.core.utils.Duration;
import org.wso2.carbon.humantask.core.utils.ISO8601DateParser;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.util.*;

/**
 * Implementation for XPath 2.0
 */
public class XPathExpressionRuntime implements ExpressionLanguageRuntime {

    private static final Log log = LogFactory.getLog(ExpressionLanguageRuntime.class);

    /**
     * Xpath 2 name space
     */
    public static final String ns = XPath2Constants.WSHT_EXP_LANG_XPATH20;
    private static final String JAVAX_XML_XPATH_XPATH_FACTORY = "javax.xml.xpath.XPathFactory:";
    private static final String NET_SF_SAXON_XPATH_XPATH_FACTORY_IMPL = "net.sf.saxon.xpath.XPathFactoryImpl";


    /**
     * Evaluate XPath expression
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return Return List of selected nodes or string
     */
    @Override
    public List evaluate(String exp, EvaluationContext evalCtx) {
        List result;
        Object someRes;
        try {
            someRes = evaluate(exp, evalCtx, XPathConstants.NODESET);
        } catch (Exception e) {
            someRes = evaluate(exp, evalCtx, XPathConstants.STRING);
        }
        if (someRes instanceof List) {
            result = (List) someRes;
            if (log.isDebugEnabled()) {
                log.debug("Returned list of size " + result.size());
            }

            if ((result.size() == 1) && !(result.get(0) instanceof Node)) {
                // Dealing with a Java class
                Object simpleType = result.get(0);
                // Dates get a separate treatment as we don't want to call toString on them
                String textVal;
                if (simpleType instanceof Date) {
                    textVal = ISO8601DateParser.format((Date) simpleType);
                } else if (simpleType instanceof DurationValue) {
                    textVal = ((DurationValue) simpleType).getStringValue();
                } else {
                    textVal = simpleType.toString();
                }

                // Wrapping in a document
                Document document = DOMUtils.newDocument();
                // Giving our node a parent just in case it's an LValue expression
                Element wrapper = document.createElement("wrapper");
                Text text = document.createTextNode(textVal);
                wrapper.appendChild(text);
                document.appendChild(wrapper);
                result = Collections.singletonList(text);
            }
        } else if (someRes instanceof NodeList) {
            NodeList retVal = (NodeList) someRes;
            if (log.isDebugEnabled()) {
                log.debug("Returned node list of size " + retVal.getLength());
            }
            result = new ArrayList(retVal.getLength());
            for (int m = 0; m < retVal.getLength(); ++m) {
                Node val = retVal.item(m);
                if (val.getNodeType() == Node.DOCUMENT_NODE) {
                    val = ((Document) val).getDocumentElement();
                }
                result.add(val);
            }
        } else if (someRes instanceof String) {
            // Wrapping in a document
            Document document = DOMUtils.newDocument();
            Element wrapper = document.createElement("wrapper");
            Text text = document.createTextNode((String) someRes);
            wrapper.appendChild(text);
            document.appendChild(wrapper);
            result = Collections.singletonList(text);
        } else {
            result = null;
        }

        return result;
    }


    private Object evaluate(String exp, EvaluationContext evalCtx, QName type) {
        try {
            XPathFactory xpf = new XPathFactoryImpl();
            JaxpFunctionResolver funcResolve = new JaxpFunctionResolver(evalCtx);
            xpf.setXPathFunctionResolver(funcResolve);
            XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();
            xpe.setXPathFunctionResolver(funcResolve);
            xpe.setBackwardsCompatible(true);
            xpe.setNamespaceContext(evalCtx.getNameSpaceContextOfTask());
            XPathExpression xpathExpression = xpe.compile(exp);
            Node contextNode = evalCtx.getRootNode() == null ? DOMUtils.newDocument() : evalCtx.getRootNode();

            Object evalResult = xpathExpression.evaluate(contextNode, type);

            if (evalResult != null && log.isDebugEnabled()) {
                log.debug("Expression " + exp + " generate result " + evalResult + " - type=" +
                        evalResult.getClass().getName());
                if (evalCtx.getRootNode() != null) {
                    log.debug("Was using context node " + DOMUtils.domToString(evalCtx.getRootNode()));
                }
            }

            return evalResult;
        } catch (XPathFactoryConfigurationException e) {
            log.error("Exception occurred while creating XPathFactory.", e);
            throw new XPathProcessingException("Exception occurred while creating XPathFactory.", e);
        } catch (XPathExpressionException e) {
            String msg = "Error evaluating XPath expression: " + exp;
            log.error(msg, e);
            throw new XPathProcessingException(msg, e);
        } catch (ParserConfigurationException e) {
            String msg = "XML Parsing error.";
            log.error(msg, e);
            throw new XPathProcessingException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new XPathProcessingException(e.getMessage(), e);
        }
    }

    /**
     * Evaluate given XPath string and returns result as a string
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return String
     */
    @Override
    public String evaluateAsString(String exp, EvaluationContext evalCtx) {
        return (String) evaluate(exp, evalCtx, XPathConstants.STRING);
    }

    /**
     * Evaluate given XPath string and returns the result as Date
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return Calendar
     */
    @Override
    public Calendar evaluateAsDate(String exp, EvaluationContext evalCtx) {
        List literal = toList(evaluate(exp, evalCtx));
        if (literal.size() == 0) {
            throw new IllegalArgumentException("No results for expression:" + exp);
        }
        if (literal.size() > 1) {
            throw new IllegalArgumentException("Multiple results for expression:" + exp);
        }

        Object date = literal.get(0);
        if (date instanceof Calendar) {
            return (Calendar) date;
        }
        if (date instanceof Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) date);
            return cal;
        }
        if (date instanceof Element) {
            date = ((Element) date).getTextContent();
        }

        if (date instanceof Text) {
            date = ((Text) date).getTextContent();
        }

        try {
            return ISO8601DateParser.parseCal(date.toString());
        } catch (Exception ex) {
            String errmsg = "Invalid date format: " + literal;
            log.error(errmsg, ex);
            throw new IllegalArgumentException(errmsg, ex);
        }
    }

    /**
     * Evaluate given XPath string and returns the result as Date
     *
     * @param exp     XPath expression string
     * @param evalCtx Evaluation context containing all the required context information
     * @return Duration
     */
    @Override
    public Duration evaluateAsDuration(String exp, EvaluationContext evalCtx) {
        String literal = exp;
        if (!Duration.isValidExpression(exp)) {
            literal = this.evaluateAsString(exp, evalCtx);
            //TODO exp should be evaluateAsString in the first place. If it is not an xpath then it should return the
            // exp string itself
        }

        try {
            return new Duration(literal);
        } catch (Exception ex) {
            String errmsg = "Invalid duration: " + exp;
            //String errmsg = "Invalid duration: " + literal;
            log.error(errmsg, ex);
            throw new IllegalArgumentException(errmsg, ex);
        }
    }

    /**
     * Evaluate given XPath string and returns the result as boolean
     *
     * @param exp     XPath expression
     * @param evalCtx Evaluation context containing all the required context information
     * @return boolean
     */
    @Override
    public boolean evaluateAsBoolean(String exp, EvaluationContext evalCtx) {
        return (Boolean) evaluate(exp, evalCtx, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate given XPath and returns the results as a java.lang.Number
     *
     * @param exp     XPath expression
     * @param evalCtx Evaluation context containing all the required context information
     * @return Number
     */
    @Override
    public Number evaluateAsNumber(String exp, EvaluationContext evalCtx) {
        return (Number) evaluate(exp, evalCtx, XPathConstants.NUMBER);
    }

    /**
     * Evaluate the expression returns an OMElement
     *
     * @param exp      Expresion
     * @param partName Name of the part
     * @param evalCtx  EvaluationContext
     * @return Part as an Node
     */
    @Override
    public Node evaluateAsPart(String exp, String partName, EvaluationContext evalCtx) {
        Document document = DOMUtils.newDocument();
        Node node = document.createElement(partName);

        List<Node> nodeList = evaluate(exp, evalCtx);

        if (nodeList.size() == 0) {
            String errMsg = "0 nodes selected for the expression: " + exp;
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        } else if (nodeList.size() > 1) {
            String errMsg = "More than one nodes are selected for the expression: " + exp;
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }

        Node partNode = nodeList.get(0);
        replaceElement((Element) node, (Element) partNode);

        return node;
    }

    private Element replaceElement(Element lval, Element src) {
        Document doc = lval.getOwnerDocument();
        NodeList nl = src.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            lval.appendChild(doc.importNode(nl.item(i), true));
        }
        NamedNodeMap attrs = src.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            Attr attr = (Attr) attrs.item(i);
            if (!attr.getName().startsWith("xmlns")) {
                lval.setAttributeNodeNS((Attr) doc.importNode(attrs.item(i), true));
                // Case of qualified attribute values, we're forced to add corresponding namespace declaration manually
                int colonIdx = attr.getValue().indexOf(":");
                if (colonIdx > 0) {
                    String prefix = attr.getValue().substring(0, colonIdx);
                    String attrValNs = src.lookupPrefix(prefix);
                    if (attrValNs != null) {
                        lval.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:" + prefix, attrValNs);
                    }
                }
            }
        }

        return lval;
    }

    /**
     * Somewhat eases the pain of dealing with both Lists and Nodelists by converting either
     * passed as parameter to a List.
     *
     * @param nl a NodeList or a List
     * @return a List
     */
    public static List<Node> toList(Object nl) {
        if (nl == null) {
            return null;
        }
        if (nl instanceof List) {
            return (List<Node>) nl;
        }

        NodeList cnl = (NodeList) nl;
        LinkedList<Node> ll = new LinkedList<Node>();
        for (int m = 0; m < cnl.getLength(); m++) {
            ll.add(cnl.item(m));
        }
        return ll;
    }
}
