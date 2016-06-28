/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpel.ui.bpel2svg;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.BPELImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.SVGImpl;
import org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

/**
 * SVG Generator
 */
public class SVGGenerateServlet extends HttpServlet {

    static final long serialVersionUID = 42L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * Handles the HTTP process request which creates the SVG graph for a bpel process
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Log log = LogFactory.getLog(SVGGenerateServlet.class);
        HttpSession session = request.getSession(true);
        //Get the bpel process id
        String pid = (String) request.getParameter("pid");
        ServletConfig config = getServletConfig();
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String processDef = null;
        ProcessManagementServiceClient client = null;
        SVGInterface svg = null;
        String svgStr = null;
        ServletOutputStream sos = null;
        sos = response.getOutputStream();
        try {
            client = new ProcessManagementServiceClient(cookie, backendServerURL, configContext,
                    request.getLocale());
            //Gets the bpel process definition needed to create the SVG from the processId
            processDef = client.getProcessInfo(QName.valueOf(pid)).getDefinitionInfo().getDefinition()
                    .getExtraElement().toString();

            BPELInterface bpel = new BPELImpl();
            //Converts the bpel process definition to an omElement which is how the AXIS2 Object Model (AXIOM)
            // represents an XML document
            OMElement bpelStr = bpel.load(processDef);

            /**
             * Process the OmElement containing the bpel process definition
             * Process the subactivites of the bpel process by iterating through the omElement
             * */
            bpel.processBpelString(bpelStr);

            //Create a new instance of the LayoutManager for the bpel process
            LayoutManager layoutManager = BPEL2SVGFactory.getInstance().getLayoutManager();
            //Set the layout of the SVG to vertical
            layoutManager.setVerticalLayout(true);
            //Get the root activity i.e. the Process Activity
            layoutManager.layoutSVG(bpel.getRootActivity());

            svg = new SVGImpl();
            //Set the root activity of the SVG i.e. the Process Activity
            svg.setRootActivity(bpel.getRootActivity());
            //Set the content type of the HTTP response as "image/svg+xml"
            response.setContentType("image/svg+xml");
            //Get the SVG graph created for the process as a SVG string
            svgStr = svg.generateSVGString();
            //Checks whether the SVG string generated contains a value
            if (svgStr != null) {
                // stream to write binary data into the response
                sos.write(svgStr.getBytes(Charset.defaultCharset()));
                sos.flush();
                sos.close();
            }
        } catch (ProcessManagementException e) {
            log.error("SVG Generation Error", e);
            String errorSVG = "<svg version=\"1.1\"\n" +
                    "     xmlns=\"http://www.w3.org/2000/svg\"><text y=\"50\">Could not display SVG</text></svg>";
            sos.write(errorSVG.getBytes(Charset.defaultCharset()));
            sos.flush();
            sos.close();
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the
    // code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServletException ex) {
            Logger.getLogger(PNGGenarateServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServletException ex) {
            Logger.getLogger(PNGGenarateServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
