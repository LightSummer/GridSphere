/*
* @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
* @version $Id$
*/
package org.gridlab.gridsphere.provider.portlet.jsr;

import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.GuestUser;
import org.gridlab.gridsphere.portlet.jsrimpl.*;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.impl.SportletProperties;
import org.gridlab.gridsphere.portletcontainer.PortletRegistry;
import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRPortletWebApplicationImpl;
import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRApplicationPortletImpl;
import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletDefinition;
import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.Supports;
import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.CustomPortletMode;
import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.CustomWindowState;
import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
import org.gridlab.gridsphere.services.core.security.acl.impl.AccessControlManager;
import org.gridlab.gridsphere.services.core.registry.impl.PortletManager;

import javax.portlet.*;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.PrintWriter;
import java.util.*;

public class PortletServlet  extends HttpServlet
        implements Servlet, ServletConfig, Serializable, ServletContextListener,
        HttpSessionAttributeListener, HttpSessionListener, HttpSessionActivationListener {

    protected transient static PortletLog log = SportletLog.getInstance(PortletServlet.class);
    protected transient static PortletRegistry registry = null;

    protected JSRPortletWebApplicationImpl portletWebApp = null;
    protected String webAppName = null;

    PortletManager manager = PortletManager.getInstance();

    protected PortletContext portletContext = null;
    //protected PortalContext portalContext = null;

    protected Map portlets = null;

    protected Map portletConfigHash = null;

    /* require an acl service to get role info */
    private AccessControlManagerService aclService = null;

    private PortletPreferencesManager prefsManager = null;


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // load descriptor files
        log.debug("in init of PortletServlet");

        aclService = AccessControlManager.getInstance();

        //registry = PortletRegistry.getInstance();

        ServletContext ctx = config.getServletContext();

        portlets = new Hashtable();
        portletConfigHash = new Hashtable();

        portletWebApp = new JSRPortletWebApplicationImpl(ctx, "PortletServlet", Thread.currentThread().getContextClassLoader());
        //registry.addApplicationPortlet(appPortlet);

        Collection appPortlets = portletWebApp.getAllApplicationPortlets();
        Iterator it = appPortlets.iterator();
        while (it.hasNext()) {
            JSRApplicationPortletImpl appPortlet = (JSRApplicationPortletImpl)it.next();
            String portletClass = appPortlet.getPortletClassName();
            System.err.println("creating new instance of " + portletClass);
            try {
                // instantiate portlet classes
                Portlet portletInstance = (Portlet) Class.forName(portletClass).newInstance();
                portlets.put(portletClass, portletInstance);
                log.debug("Creating new portlet instance: " + portletClass);

                // put portlet web app in registry

            } catch (Exception e) {
                log.error("Unable to create jsr portlet instance: " + portletClass, e);
            }
        }

        /*
        PortletManager manager = PortletManager.getInstance();
        manager.initPortletWebApplication(webapp);
       */

        // create portlet context
        portletContext = new PortletContextImpl(ctx);

        prefsManager = PortletPreferencesManager.getInstance();
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        registry = PortletRegistry.getInstance();
        // If no portlet ID exists, this may be a command to init or shutdown a portlet instance

        // currently either all portlets are initailized or shutdown, not one individually...
        String method = (String) request.getAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);

        if (method.equals(SportletProperties.INIT)) {
            Set set = portlets.keySet();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                String portletClass = (String)it.next();
                Portlet portlet = (Portlet)portlets.get(portletClass);
                log.debug("in PortletServlet: service(): Initializing portlet " + portletClass);
                PortletDefinition portletDef = portletWebApp.getPortletDefinition(portletClass);
                if (portletDef == null) System.err.println("portlet def is null");
                PortletConfig portletConfig = new PortletConfigImpl(getServletConfig(), portletDef, Thread.currentThread().getContextClassLoader());
                try {
                    portlet.init(portletConfig);
                    portletConfigHash.put(portletClass, portletConfig);
                } catch (Exception e) {
                    log.error("in PortletServlet: service(): Unable to INIT portlet " + portletClass, e);
                    // PLT.5.5.2.1 Portlet that fails to initialize must not be placed in active service
                    it.remove();
                    //throw new ServletException(e);
                }
            }

            manager.addWebApp(portletWebApp);
            return;
        } else if (method.equals(SportletProperties.INIT_CONCRETE)) {
            // do nothing for concrete portlets
            return;
        } else if (method.equals(SportletProperties.DESTROY)) {
            Iterator it = portlets.keySet().iterator();
            while (it.hasNext()) {
                String portletClass = (String)it.next();
                Portlet portlet = (Portlet)portlets.get(portletClass);
                log.debug("in PortletServlet: service(): Destroying portlet " + portletClass);
                try {
                    portlet.destroy();
                } catch (RuntimeException e) {
                    log.error("Caught exception during portlet destroy", e);
                }
            }
            manager.removePortletWebApplication(portletWebApp);
            return;
        } else if (method.equals(SportletProperties.DESTROY_CONCRETE)) {
            // do nothing for concrete portlets
            return;
        }

        // There must be a portlet ID to know which portlet to service
        String portletClassName = (String) request.getAttribute(SportletProperties.PORTLETID);
        String compId = (String) request.getAttribute(SportletProperties.COMPONENT_ID);

        if (portletClassName == null) {
            // it may be in the request parameter
            portletClassName = request.getParameter(SportletProperties.PORTLETID);
            if (portletClassName == null) {
                log.debug("in PortletServlet: service(): No PortletID found in request!");
                return;
            }
        }

        log.debug("have a portlet id " + portletClassName + " component id= " + compId);
        Portlet portlet = (Portlet)portlets.get(portletClassName);

        JSRApplicationPortletImpl appPortlet =
                (JSRApplicationPortletImpl)registry.getApplicationPortlet(portletClassName);

        Supports[] supports = appPortlet.getSupports();
        CustomPortletMode[] customModes =  portletWebApp.getCustomPortletModes();
        CustomWindowState[] customStates = portletWebApp.getCustomWindowStates();


        // perform user conversion from gridsphere to JSR model
        User user = (User)request.getAttribute(SportletProperties.PORTLET_USER);
        Map userInfo = (Map)request.getAttribute(PortletRequest.USER_INFO);

        if (user instanceof GuestUser) {
            userInfo = null;
        } else {
            userInfo = new HashMap();
            userInfo.put("user.name", user.getUserName());
            userInfo.put("user.name.id", user.getID());
            userInfo.put("user.email", user.getEmailAddress());
            userInfo.put("user.name.given", user.getGivenName());
            userInfo.put("user.organization", user.getOrganization());
            userInfo.put("user.lastlogintime", new Long(user.getLastLoginTime()).toString());
            userInfo.put("user.name.full", user.getFullName());
            userInfo.put("user.namefamily", user.getFamilyName());
        }

        request.setAttribute(PortletRequest.USER_INFO, userInfo);

        // portlet preferences

        PortalContext portalContext = appPortlet.getPortalContext();
        request.setAttribute(SportletProperties.PORTAL_CONTEXT, portalContext);

        request.setAttribute(SportletProperties.PORTLET_CONFIG, portletConfigHash.get(portletClassName));

        if (portlet == null) {
            log.error("in PortletServlet: service(): No portlet matching " + portletClassName + " found!");
            return;
        }

        if (method.equals(SportletProperties.SERVICE)) {
            String action = (String) request.getAttribute(SportletProperties.PORTLET_ACTION_METHOD);
            if (action != null) {
                log.debug("in PortletServlet: action is not NULL");
                if (action.equals(SportletProperties.DO_TITLE)) {
                    RenderRequest renderRequest = new RenderRequestImpl(request, portalContext, portletContext, supports);
                    RenderResponse renderResponse = new RenderResponseImpl(request, response, portalContext);
                    renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                    renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
                    log.debug("in PortletServlet: do title " + portletClassName);
                    doTitle(portlet, renderRequest, renderResponse);
                } else {
                    PortletPreferences prefs = prefsManager.getPortletPreferences(appPortlet, user, Thread.currentThread().getContextClassLoader(), false);
                    request.setAttribute(SportletProperties.PORTLET_PREFERENCES, prefs);
                    ActionRequestImpl actionRequest = new ActionRequestImpl(request, portalContext, portletContext, supports);
                    ActionResponse actionResponse = new ActionResponseImpl(request, response, portalContext);
                    //setGroupAndRole(actionRequest, actionResponse);
                    log.debug("in PortletServlet: action handling portlet " + portletClassName);
                    try {
                        portlet.processAction(actionRequest, actionResponse);
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                    Map params = ((ActionResponseImpl)actionResponse).getRenderParameters();
                    String cid = (String)request.getAttribute(SportletProperties.COMPONENT_ID);
                    actionRequest.setAttribute("renderParams" + "_" + portletClassName + "_" + cid, params);
                    System.err.println("placing render params in attribute: " + "renderParams" + "_" + portletClassName + "_" + cid);
                    //actionRequest.clearParameters();
                    redirect(request, response, actionRequest, actionResponse, portalContext);
                }
            } else {
                PortletPreferences prefs = prefsManager.getPortletPreferences(appPortlet, user, Thread.currentThread().getContextClassLoader(), true);
                request.setAttribute(SportletProperties.PORTLET_PREFERENCES, prefs);

                RenderRequest renderRequest = new RenderRequestImpl(request, portalContext, portletContext, supports);
                RenderResponse renderResponse = new RenderResponseImpl(request, response, portalContext);
                renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
                //setGroupAndRole(renderRequest, renderResponse);
                log.debug("in PortletServlet: rendering  portlet " + portletClassName);
                if (renderRequest.getAttribute(SportletProperties.RESPONSE_COMMITTED) == null) {
                    try {
                        portlet.render(renderRequest, renderResponse);
                    } catch (UnavailableException e) {
                        log.error("in PortletServlet(): doRender() caught unavailable exception: ");
                        try {
                            portlet.destroy();
                        } catch (Exception d) {
                            log.error("in PortletServlet(): destroy caught unavailable exception: ", d);
                        }
                    } catch (Exception e) {
                        log.error("in PortletServlet(): doRender() caught exception: ", e);
                    }
                }
            }
            request.removeAttribute(SportletProperties.PORTLET_ACTION_METHOD);
        } else {
            log.error("in PortletServlet: service(): No " + SportletProperties.PORTLET_LIFECYCLE_METHOD + " found in request!");
        }
        request.removeAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
}

/*
protected void setGroupAndRole(PortletRequest request, PortletResponse response) {
String ctxPath = this.getServletContext().getRealPath("");
int i = ctxPath.lastIndexOf(File.separator);
String groupName = ctxPath.substring(i+1);

PortletGroup group = aclService.getGroupByName(groupName);
if (group == null)
group = PortletGroupFactory.createPortletGroup(groupName);

PortletRole role = aclService.getRoleInGroup(request.getUser(), group);

log.debug("Setting Group: " + group.toString() + " Role: " + role.toString());

request.setAttribute(SportletProperties.PORTLET_GROUP, group);
request.setAttribute(SportletProperties.PORTLET_ROLE, role);
}
*/

    protected void doTitle(Portlet portlet, RenderRequest request, RenderResponse response) {
        try {
            ResourceBundle resBundle = ((GenericPortlet)portlet).getPortletConfig().getResourceBundle(request.getLocale());
            String title = resBundle.getString("javax.portlet.title");
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println(title);
        } catch (IOException e) {
            log.error("printing title failed", e);
        }
    }



    protected void doRender(Portlet portlet, RenderRequest request, RenderResponse response) {
        PortletMode mode = request.getPortletMode();
        if (mode == null) {
            mode = PortletMode.VIEW;
            //request.setPortletMode(mode);
        }
        log.debug("Displaying mode: " + mode);
        try {
            portlet.render(request, response);
        } catch (Exception e) {
            log.error("in PortletServlet(): doRender() caught exception in mode: " + mode, e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doGet(req, res);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doPut(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doPost(req, res);
    }

    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doTrace(req, res);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doDelete(req, res);
    }

    public void destroy() {
        portletWebApp.destroy();
        super.destroy();
        //portletManager.destroyPortletWebApplication(portletWebApp);
    }


    protected void redirect(HttpServletRequest servletRequest,
                            HttpServletResponse servletResponse,
                            ActionRequest actionRequest,
                            ActionResponse actionResponse, PortalContext portalContext)
            throws IOException {
        String location = null;
        if (actionResponse instanceof ActionResponseImpl) {
            ActionResponseImpl aResponse = (ActionResponseImpl) actionResponse;
            location = aResponse.getRedirectLocation();

            if (location != null) {
                /*
                if (location.indexOf("://") < 0 ) {

                    PortletURLImpl redirectUrl = new PortletURLImpl(servletRequest, servletResponse, portalContext);
                    //TODO: don't send changes in case of exception -> PORTLET:SPEC:17

                    // get the changings of this portlet entity that might be set during action handling
                    // change portlet mode
                    redirectUrl.setContextPath(actionRequest.getContextPath());
                    try {
                        if (aResponse.getChangedPortletMode() != null) {
                            redirectUrl.setPortletMode(aResponse.getChangedPortletMode());
                        } else {
                            redirectUrl.setPortletMode(actionRequest.getPortletMode());
                        }
                    } catch (PortletModeException e) {
                        e.printStackTrace();
                    }

                    // change window state
                    try {
                        if (aResponse.getChangedWindowState() != null) {
                            redirectUrl.setWindowState(aResponse.getChangedWindowState());
                        } else {
                            redirectUrl.setWindowState(actionRequest.getWindowState());
                        }

                    } catch (WindowStateException e) {
                        e.printStackTrace();
                    }
                    // get render parameters
                    Map renderParameter = aResponse.getRenderParameters();
                    redirectUrl.setParameter(SportletProperties.COMPONENT_ID, (String) servletRequest.getParameter(SportletProperties.COMPONENT_ID));
                    redirectUrl.setParameters(renderParameter);
                    System.err.println("redirecting url " +  redirectUrl.toString());
                    location = servletResponse.encodeRedirectURL(redirectUrl.toString());
                }
                */
                javax.servlet.http.HttpServletResponse redirectResponse = servletResponse;
                while (redirectResponse instanceof javax.servlet.http.HttpServletResponseWrapper) {
                    redirectResponse = (javax.servlet.http.HttpServletResponse)
                            ((javax.servlet.http.HttpServletResponseWrapper) redirectResponse).getResponse();
                }

                System.err.println("redirecting to location= " + location);
                redirectResponse.sendRedirect(location);
            }
        }


    }

    /**
     * Record the fact that a servlet context attribute was added.
     *
     * @param event The session attribute event
     */
    public void attributeAdded(HttpSessionBindingEvent event) {

        log.debug("attributeAdded('" + event.getSession().getId() + "', '" +
                event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that a servlet context attribute was removed.
     *
     * @param event The session attribute event
     */
    public void attributeRemoved(HttpSessionBindingEvent event) {

        log.debug("attributeRemoved('" + event.getSession().getId() + "', '" +
                event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that a servlet context attribute was replaced.
     *
     * @param event The session attribute event
     */
    public void attributeReplaced(HttpSessionBindingEvent event) {

        log.debug("attributeReplaced('" + event.getSession().getId() + "', '" +
                event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that this ui application has been destroyed.
     *
     * @param event The servlet context event
     */
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        log.debug("contextDestroyed()");
        log.debug("contextName: " + ctx.getServletContextName());
        log.debug("context path: " + ctx.getRealPath(""));

    }


    /**
     * Record the fact that this ui application has been initialized.
     *
     * @param event The servlet context event
     */
    public void contextInitialized(ServletContextEvent event) {
        log.debug("contextInitialized()");
        ServletContext ctx = event.getServletContext();
        log.debug("contextName: " + ctx.getServletContextName());
        log.debug("context path: " + ctx.getRealPath(""));
    }

    /**
     * Record the fact that a session has been created.
     *
     * @param event The session event
     */
    public void sessionCreated(HttpSessionEvent event) {
        log.debug("sessionCreated('" + event.getSession().getId() + "')");
        //sessionManager.sessionCreated(event);
    }


    /**
     * Record the fact that a session has been destroyed.
     *
     * @param event The session event
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        //sessionManager.sessionDestroyed(event);
        //loginService.sessionDestroyed(event.getSession());
        log.debug("sessionDestroyed('" + event.getSession().getId() + "')");
        HttpSession s = event.getSession();

        //HttpSession session = event.getSession();
        //User user = (User) session.getAttribute(SportletProperties.PORTLET_USER);
        //System.err.println("user : " + user.getUserID() + " expired!");
        //PortletLayoutEngine engine = PortletLayoutEngine.getDefault();
        //engine.removeUser(user);
        //engine.logoutPortlets(event);
    }

    /**
     * Record the fact that a session has been created.
     *
     * @param event The session event
     */
    public void sessionDidActivate(HttpSessionEvent event) {
        log.debug("sessionDidActivate('" + event.getSession().getId() + "')");
        //sessionManager.sessionCreated(event);
    }


    /**
     * Record the fact that a session has been destroyed.
     *
     * @param event The session event
     */
    public void sessionWillPassivate(HttpSessionEvent event) {
        //sessionManager.sessionDestroyed(event);
        //loginService.sessionDestroyed(event.getSession());
        log.debug("sessionWillPassivate('" + event.getSession().getId() + "')");
        //HttpSession session = event.getSession();
        //User user = (User) session.getAttribute(SportletProperties.USER);
        //System.err.println("user : " + user.getUserID() + " expired!");
        //PortletLayoutEngine engine = PortletLayoutEngine.getDefault();
        //engine.removeUser(user);
        //engine.logoutPortlets(event);
    }
}










































