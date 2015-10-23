/**
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.bpel.ui.bpel2svg.impl.BPELImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.SVGImpl;
import org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generate the PNG out of the SVG. May be useful in browsers which doesn't support SVG
 */
public class PNGGenarateServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Log log = LogFactory.getLog(PNGGenarateServlet.class);
        HttpSession session = request.getSession(true);
        String pid = (String) request.getParameter("pid");
        ServletConfig config = getServletConfig();
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String processDef;
        ProcessManagementServiceClient client;
        SVGInterface svg;
        String svgStr;
        try {
            client = new ProcessManagementServiceClient(cookie, backendServerURL, configContext,
                    request.getLocale());
            processDef = client.getProcessInfo(QName.valueOf(pid)).getDefinitionInfo().getDefinition().getExtraElement().toString();

            BPELInterface bpel = new BPELImpl();
            OMElement bpelStr = bpel.load(processDef);
            bpel.processBpelString(bpelStr);

            LayoutManager layoutManager = BPEL2SVGFactory.getInstance().getLayoutManager();
            layoutManager.setVerticalLayout(true);
            layoutManager.layoutSVG(bpel.getRootActivity());

            svg = new SVGImpl();
            svg.setRootActivity(bpel.getRootActivity());

            response.setContentType("image/png");
            ServletOutputStream sos = response.getOutputStream();

            byte[] pngBytes = svg.toPNGBytes();
            if (pngBytes != null) {
                sos.write(pngBytes);
                sos.flush();
                sos.close();
            }
        } catch (Exception e) {
            log.error("PNG Generation Error", e);
        }

    }

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
