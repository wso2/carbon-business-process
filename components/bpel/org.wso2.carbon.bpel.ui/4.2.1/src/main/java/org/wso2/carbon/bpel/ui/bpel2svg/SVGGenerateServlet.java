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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.BPELImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.impl.SVGImpl;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * SVG Generator
 */
public class SVGGenerateServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Log log = LogFactory.getLog(SVGGenerateServlet.class);
        HttpSession session = request.getSession(true);
        String pid = (String) request.getParameter("pid");
        ServletConfig config = getServletConfig();
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String processDef = null;
        ProcessManagementServiceClient client = null;
        SVGInterface svg  = null;
        String svgStr = null;
        ServletOutputStream sos = null;
        sos = response.getOutputStream();
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

            //base64Str = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAAQABADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD1i612KO/vo3mu7hku503xz6kqjEjDaBHEU+X7vykjiqt94hhFjcES3tsRGxE7XGqERcfeOYccdefSp7zw7aTajfyzWt3BJJdzuVjtdQdTmRjuBSYKd33vlAHNVL3wxZfYrjZaXtw/ltiFrPUgJDj7pJnwM9K9mPsdL3PHl7fW3Kf/2Q==";
            response.setContentType("image/svg+xml");
            //sos.write(Base64.decodeBase64(base64Str.getBytes()));
            svgStr = svg.generateSVGString();
            if (svgStr != null) {
                sos.write(svgStr.getBytes());
                sos.flush();
                sos.close();
            }
        } catch (Exception e) {
            log.error("SVG Generation Error", e);
            String errorSVG = "<svg version=\"1.1\"\n" +
                    "     xmlns=\"http://www.w3.org/2000/svg\"><text y=\"50\">Could not display SVG</text></svg>";
            sos.write(errorSVG.getBytes());
            sos.flush();
            sos.close();
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
