/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlets.core;

import org.gridlab.gridsphere.event.ActionEvent;
import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlets.core.tomcat.TomcatManagerWrapper;
import org.gridlab.gridsphere.portlets.core.tomcat.TomcatWebAppResult;
import org.gridlab.gridsphere.provider.event.FileFormEvent;
import org.gridlab.gridsphere.provider.event.FileFormException;
import org.gridlab.gridsphere.provider.event.impl.FileFormEventImpl;
import org.gridlab.gridsphere.services.core.registry.PortletManagerService;

import javax.servlet.UnavailableException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The PortletApplicationManager is a wrapper for the Tomcat manager webapp in 4.1.X which allows dynamic
 * ui application management and hence dynamic portlet management. This class needs to be adapted for
 * other servlet containers.
 */
public class PortletApplicationManager extends AbstractPortlet {

    private PortletManagerService portletManager = null;
    private TomcatManagerWrapper tomcat = TomcatManagerWrapper.getInstance();

    public void init(PortletConfig config) throws UnavailableException {
        super.init(config);
    }

    public void actionPerformed(ActionEvent event) throws PortletException {
        DefaultPortletAction action = event.getAction();
        PortletRequest req = event.getPortletRequest();
        PortletResponse res = event.getPortletResponse();

        User user = event.getPortletRequest().getUser();

        try {
            portletManager = (PortletManagerService)getConfig().getContext().getService(PortletManagerService.class, user);
        } catch (PortletServiceUnavailableException e) {
            throw new PortletException("PortletRegistry service unavailable! ", e);
        } catch (PortletServiceNotFoundException e) {
            throw new PortletException("PortletRegistryService not found! ", e);
        }

        Map params = action.getParameters();
        String operation = (String)params.get("operation");
        String appName = (String)params.get("context");
        TomcatWebAppResult result = null;
        try {
            if (action.getName().equals("list")) {
                result = tomcat.getWebAppList();
            } else if (action.getName().equals("install")) {
                log.debug("In actionPerformed doing an install");
                FileFormEvent fileformEvent = new FileFormEventImpl(event);
                String portletWar = null;

                try {
                    portletWar = fileformEvent.saveFile(PortletManagerService.WEB_APPLICATION_PATH);
                } catch (FileFormException ffe) {
                    log.error("Unable to save file from form: " + ffe.getMessage());
                }

                /* Remove old portlet web app if it exists */
                int idx = -1;
                String webAppContext = null;
                String webAppName = null;
                List webappsList = portletManager.getPortletWebApplicationNames();
                if ((idx = portletWar.lastIndexOf(".")) > 0) {
                    webAppName = portletWar.substring(0, idx);
                    System.err.println(webAppName);
                    if ((idx = webAppName.lastIndexOf("/")) > 0) {
                        webAppContext = webAppName.substring(idx);
                        webAppName = webAppContext.substring(1);
                        if (webappsList.contains(webAppName)) {
                            portletManager.removePortletWebApplication(webAppName, req, res);
                            result = tomcat.removeWebApp(webAppName);
                        }
                    }
                }

                //String portletWar = warFile.getAbsolutePath();
                if (webAppContext != null) {
                    //result = tomcat.installWebApp(webAppContext, portletWar);
                    //result = tomcat.startWebApp(webAppName);
                    portletManager.installPortletWebApplication(webAppName, req, res);
                }
            } else if ((operation != null) && (appName!= null)) {
                if (operation.equals("start")) {
                    result = tomcat.startWebApp(appName);
                    portletManager.installPortletWebApplication(appName, req, res);
                } else if (operation.equals("stop")) {
                    portletManager.destroyPortletWebApplication(appName, req, res);
                    result = tomcat.stopWebApp(appName);
                } else if (operation.equals("reload")) {
                    result = tomcat.reloadWebApp(appName);
                    portletManager.initPortletWebApplication(appName, req, res);
                } else if (operation.equals("remove")) {
                    portletManager.removePortletWebApplication(appName, req, res);
                    result = tomcat.removeWebApp(appName);
                } else if (operation.equals("deploy")) {
                    result = tomcat.deployWebApp(appName);
                    portletManager.installPortletWebApplication(appName, req, res);
                } else if (operation.equals("undeploy")) {
                    result = tomcat.undeployWebApp(appName);
                    portletManager.removePortletWebApplication(appName, req, res);
                }
            }
        } catch (IOException e) {
            log.error("Caught IOException!", e);
        }
        req.setAttribute("result", result);
        if (result != null) System.err.println("result: " + result.getReturnCode() + " " + result.getDescription());
    }

    public void doView(PortletRequest request, PortletResponse response) throws PortletException, IOException {
        /*
        List webapps = portletManager.getPortletWebApplications();
        for (int i = 0; i < webapps.size(); i++) {
            System.err.println("webapp " + i + " " + webapps.get(i));
        }

        TomcatWebAppResult result = tomcat.getWebAppList();
        List allwebapps = result.getWebAppDescriptions();
        System.err.println(result.getReturnCode() + " : " + result.getDescription());
        request.setAttribute("result", result);
        getPortletConfig().getContext().include("/jsp/list.jsp", request, response);
        */
        if (request.getAttribute("result") != null) {
            getPortletConfig().getContext().include("/jsp/list.jsp", request, response);
        } else {
            getPortletConfig().getContext().include("/jsp/display.jsp", request, response);
        }
    }

}
