package org.wso2.carbon.bpmn.extensions.soap.impl;
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


import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the NodeList to store nodes.
 */
public class NodeList implements org.w3c.dom.NodeList {
    private List<Node> nodeList = new ArrayList<Node>();

    /**
     * Gets the node at the given index.
     *
     * @param index
     * @return node
     */
    @Override
    public Node item(int index) {
        return nodeList.get(index);
    }

    /**
     * Gets the size of the NodeList.
     *
     * @return length/size
     */
    @Override
    public int getLength() {
        return nodeList.size();
    }

    /**
     * Add a node to the NodeList.
     *
     * @param node
     */
    public void addNode(Node node) {
        nodeList.add(node);
    }

}
