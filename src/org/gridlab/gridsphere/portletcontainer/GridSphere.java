/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.SportletConfig;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.services.PortletRegistryService;
import org.gridlab.gridsphere.services.ServletParsingService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;


public class GridSphere extends HttpServlet {

    private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(GridSphere.class);
    private static Properties props = new Properties();

    private static SportletServiceFactory factory = SportletServiceFactory.getInstance();

    private static PortletRegistryService registryService = null;
    private static ServletParsingService parseService = null;

    private static Collection abstractPortlets = new HashSet();
    private static Collection registeredPortlets = null;
    private static PortletConfig portletConfig;

    private static boolean firstDoGet = true;


    public final void init(ServletConfig config) throws ServletException {
        super.init(config);

        synchronized (this.getClass()) {
            try {
                configure(config);
                portletConfig = new SportletConfig(config);
                log.info("init success");

            } catch (Exception e) {
                System.err.println("GridSphere: init() failed: " + e.getMessage());
            }
        }
    }

    protected static synchronized void configure(ServletConfig config) throws Exception {

        FileInputStream fistream = null;
        ServletContext context = config.getServletContext();

        log.info("configure() in GridSphere");
        log.info("Application Server info:");
        log.info(context.getServerInfo() + context.getMajorVersion() + context.getMinorVersion());

        String appRoot = context.getRealPath("");
        String serviceProps = config.getInitParameter("PortletServices.properties");
        String propsPath = appRoot + "/" + serviceProps;
        try {
            fistream = new FileInputStream(propsPath);
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Unable to find properties file: " + propsPath);
            System.err.println("Make sure location is specified in server.xml " + propsPath);
        }
        props.load(fistream);

        // Start services

        parseService =
                (ServletParsingService) factory.createPortletService(ServletParsingService.class, props, config, true);
        registryService =
                (PortletRegistryService) factory.createPortletService(PortletRegistryService.class, props, config, true);


        // read portlet.xml and retrieve portlet info
        log.info("configure() in GridSphere");


    }

    /**
     * Provide user login. Calls login() method on all portlets that are
     * in a users profile
     * For now, just login all known portlets
     */
    public final void doLogin(PortletRequest req, PortletResponse res) {
        Iterator it = abstractPortlets.iterator();
        while (it.hasNext()) {
            AbstractPortlet ab = (AbstractPortlet) it.next();
            ab.login(req);
        }
    }

    /**
     * Log a user out of the portal. Calls logout() method on all portlets that are
     * in a users profile
     * For now, just logout of all known portlets
     */
    public final void doLogoff(PortletRequest req, PortletResponse res) {
        Iterator it = abstractPortlets.iterator();
        while (it.hasNext()) {
            AbstractPortlet ab = (AbstractPortlet) it.next();
            ab.logout(req.getPortletSession());
        }
    }

    public final void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        // Get available portlets from PortletRegistry the first time the portal is accessed
        if (firstDoGet) {
            Iterator it = registryService.getRegisteredPortlets().iterator();
            while (it.hasNext()) {
                RegisteredPortlet regPortlet = (RegisteredPortlet) it.next();
                System.err.println("portlet name: " + regPortlet.getPortletName());

                AbstractPortlet abPortlet = regPortlet.getActivePortlet();
                abstractPortlets.add(abPortlet);
                // PortletConfig portletConfig = regPortlet.getPortletConfig();
                PortletSettings portletSettings = regPortlet.getPortletSettings();

                abPortlet.init(portletConfig);
                abPortlet.initConcrete(portletSettings);
            }

            firstDoGet = false;
        }


        // Some prototyping here to perform tasks of portlet container

        // Need to translate servlet objects into portlet counterparts--
        PortletRequest portletRequest;
        PortletResponse portletResponse;

        portletRequest = parseService.getPortletRequest(req);
        portletResponse = parseService.getPortletResponse(res);

        // Now we want to display a portlet!

        // These are the following eight calls that get invoked during the portlet lifecycle

        // create a new instance -- maybe instances are maintained by PortletRegistryService
        // then all instances are retrieved.
        //PortletAdapter helloPortlet = new HelloWorld();




        // Hmmm... need some kind of tags for identifying login and logout actions -- these are special so
        // portlet.login/logout get called-- also need to keep track of user logged in, so we can invoke
        // portlet.service method.
        // Cheap hack is to check some kind of request/session attribute
        // would rather have some isLoggedIn/Off method...
        // possibly since we maintain user objects as sportletUserImpls

        // This should be done during first GET after other portlets (servlets) have registered to PortletRegistry service
        //helloPortlet.init(portletConfig);
        //helloPortlet.initConcrete(portletSettings);

        // This should be called during a portlet login after the user logs in. (after obtaining a valid user object from
        // the LoginSportlet

        //helloPortlet.login(portletRequest);

        // For now, use cheesy request parameters.. not sure what else...
        String login = (String) portletRequest.getAttribute(GridSphereProperties.Login);
        String logoff = (String) portletRequest.getAttribute(GridSphereProperties.Logoff);
        if (login != null) {
            doLogin(portletRequest, portletResponse);
        }

        if (logoff != null) {
            doLogoff(portletRequest, portletResponse);
        }


        // This gets called right here in servlets's service method-- just invokes portlet service method of the active portlet
        // Hmm... may invoke service of all methods if caching etc. is up to each portlet. definitely want to cache!!
        // Hmm.. what if include is really slow and so even if jsp handles caching it doesn't matter? We'll see...
        //helloPortlet.service(portletRequest, portletResponse);

        PrintWriter out = portletResponse.getWriter();
        //ServletOutputStream out = portletResponse.getOutputStream();
        out.println("<html><body>");

        Iterator it = abstractPortlets.iterator();
        while (it.hasNext()) {
            AbstractPortlet ab = (AbstractPortlet) it.next();

            // First execute portlet business logic
            ab.execute(portletRequest);
            out.println("<b>portlet</b>");
            // Second forward to presentation logic
            ab.service(portletRequest, portletResponse);
        }

        out.println("</body></html>");
        //helloPortlet.logout(portletRequest.getPortletSession());
        //helloPortlet.destroyConcrete(portletSettings);
        //helloPortlet.destroy(portletConfig);

        //portletConfig.getContext().include("/jsp/hello.jsp", portletRequest, portletResponse);
    }

    public final void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doGet(req, res);
    }

    /**
     * Return the servlet info.
     *
     * @return a string with the servlet information.
     */
    public final String getServletInfo() {
        return "GridSphere Servlet 0.9";
    }

    public final void destroy() {
        // Shut down all Portlet Services.
        log.info("Shutting down GridSphere");

        // Destroy all portlets
        Iterator abstractIt = abstractPortlets.iterator();
        Iterator registeredIt = abstractPortlets.iterator();
        while (abstractIt.hasNext() && registeredIt.hasNext()) {
            AbstractPortlet ab = (AbstractPortlet) abstractIt.next();
            RegisteredPortlet reg = (RegisteredPortlet) registeredIt.next();
            ab = reg.getActivePortlet();
            PortletConfig pConfig = reg.getPortletConfig();
            PortletSettings pSettings = reg.getPortletSettings();
            ab.destroy(pConfig);
            ab.destroyConcrete(pSettings);
        }

        SportletServiceFactory.getInstance().shutdownServices();
        System.gc();
    }

}
