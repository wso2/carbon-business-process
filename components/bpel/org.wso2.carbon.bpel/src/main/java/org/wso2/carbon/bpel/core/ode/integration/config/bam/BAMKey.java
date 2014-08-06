package org.wso2.carbon.bpel.core.ode.integration.config.bam;

/**
 * Represents the From elements in the bam publisher for BPEL
 * <p/>
 * <Data>
 *  <Key name="NCName1" type="meta|correlation|payload">
 *      <From variable="myVar" part="partName"/>
 *  </Key>
 *  <Key name="NCName2" type="meta|correlation|payload">
 *      <From variable="myVar2"/>
 *  </Key>
 *  <Key name="NCName3" type="meta|correlation|payload">
 *      <From variable="myVar3">
 *          <Query>XPath expression</Query>
 *      </From>
 *  </Key>
 *  <Key name="NCName4" type="meta|correlation|payload">
 *      <From variable="myVar4" part="partName">
 *          <Query>XPath expression</Query>
 *      </From>
 *  </Key>
 *  <Key name="NCName5" type="meta|correlation|payload">
 *      <From>XPath expression</From>
 *  </Key>
 * </Data>
 */
public class BAMKey {
    private String name;
    private BAMKeyType type;
    private String variable;
    private String part;
    private String query;
    private String expression;

    public BAMKey(String name, BAMKeyType type) {
        this.name = name;
        this.type = type;
    }

    public BAMKey(String name, String variable, BAMKeyType type) {
        this.name = name;
        this.variable = variable;
        this.type = type;
    }

    public BAMKey(String name, String variable, String part, BAMKeyType type) {
        this.name = name;
        this.variable = variable;
        this.part = part;
        this.type = type;
    }

    public BAMKey(String name, String variable, String part, String query, BAMKeyType type) {
        this.name = name;
        this.variable = variable;
        this.part = part;
        this.query = query;
        this.type = type;
    }

    public String getVariable() {
        return variable;
    }

    public String getPart() {
        return part;
    }

    public String getQuery() {
        return query;
    }

    public String getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public BAMKeyType getType() {
        return type;
    }

    public enum BAMKeyType {
        META, CORRELATION, PAYLOAD, NONE
    }
}
