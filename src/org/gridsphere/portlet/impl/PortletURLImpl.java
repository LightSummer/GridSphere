/*
 * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
 * @version $Id$
 */
package org.gridsphere.portlet.impl;

import org.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridsphere.services.core.portal.PortalConfigService;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * The <CODE>PortletURL</CODE> interface represents a URL
 * that reference the portlet itself.
 * <p/>
 * A PortletURL is created through the <CODE>RenderResponse</CODE>.
 * Parameters, a portlet mode, a window state and a security level
 * can be added to <CODE>PortletURL</CODE> objects. The PortletURL
 * must be converted to a String in order to embed it into
 * the markup generated by the portlet.
 * <p/>
 * There are two types of PortletURLs:
 * <ul>
 * <li>Action URLs, they are created with <CODE>RenderResponse.createActionURL</CODE>, and
 * trigger an action request followed by a render request.
 * <li>Render URLs, they are created with <CODE>RenderResponse.createRenderURL</CODE>, and
 * trigger a render request.
 * </ul>
 * <p/>
 * The string reprensentation of a PortletURL does not need to be a valid
 * URL at the time the portlet is generating its content. It may contain
 * special tokens that will be converted to a valid URL, by the portal,
 * before the content is returned to the client.
 */
public class PortletURLImpl implements PortletURL {

    private HttpServletResponse res = null;
    private HttpServletRequest req = null;
    private boolean isSecure = false;
    private Map<String, Object> store = null;
    private boolean redirect = false;
    private boolean encoding = true;
    private boolean isRender = false;
    private String label = null;

    private String layout = null;

    private PortalConfigService configService = null;

    private PortletMode mode = null;
    private WindowState state = null;

    protected PortletURLImpl() {
    }

    /**
     * Constructs a PortletURL from a servlet request and response
     *
     * @param req      the servlet request
     * @param res      the servlet response
     * @param isRender true if this is a render url, false if an action url
     */
    public PortletURLImpl(HttpServletRequest req, HttpServletResponse res, boolean isRender) {
        this.store = new HashMap<String, Object>();
        this.res = res;
        this.req = req;

        this.isSecure = req.isSecure();
        this.isRender = isRender;
        configService = (PortalConfigService) PortletServiceFactory.createPortletService(PortalConfigService.class, true);
    }

    /**
     * Indicates the window state the portlet should be in, if this
     * portlet URL triggers a request.
     * <p/>
     * A URL can not have more than one window state attached to it.
     * If more than one window state is set only the last one set
     * is attached to the URL.
     *
     * @param windowState the portlet window state
     * @throws WindowStateException if the portlet cannot switch to this state,
     *                              because the portal does not support this state, the portlet has not
     *                              declared in its deployment descriptor that it supports this state, or the current
     *                              user is not allowed to switch to this state.
     *                              The <code>PortletRequest.isWindowStateAllowed()</code> method can be used
     *                              to check if the portlet can set a given window state.
     * @see PortletRequest#isWindowStateAllowed
     */
    public void setWindowState(WindowState windowState)
            throws WindowStateException {
        if (windowState == null) throw new IllegalArgumentException("Window state cannot be null");
        boolean isSupported = false;
        PortalContext context = (PortalContext) req.getAttribute(SportletProperties.PORTAL_CONTEXT);
        Enumeration e = context.getSupportedWindowStates();
        while (e.hasMoreElements()) {
            WindowState supported = (WindowState) e.nextElement();
            if (supported.equals(windowState)) {
                isSupported = true;
                break;
            }
        }

        if (isSupported) {
            state = windowState;
        } else {
            throw new WindowStateException("Illegal window state", windowState);
        }

    }

    /**
     * Indicates the portlet mode the portlet must be in, if this
     * portlet URL triggers a request.
     * <p/>
     * A URL can not have more than one portlet mode attached to it.
     * If more than one portlet mode is set only the last one set
     * is attached to the URL.
     *
     * @param portletMode the portlet mode
     * @throws PortletModeException if the portlet cannot switch to this mode,
     *                              because the portal does not support this mode, the portlet has not
     *                              declared in its deployment descriptor that it supports this mode for the current markup,
     *                              or the current user is not allowed to switch to this mode.
     *                              The <code>PortletRequest.isPortletModeAllowed()</code> method can be used
     *                              to check if the portlet can set a given portlet mode.
     * @see PortletRequest#isPortletModeAllowed
     */
    public void setPortletMode(PortletMode portletMode)
            throws PortletModeException {
        if (portletMode == null) throw new IllegalArgumentException("Portlet mode cannot be null");
        Set allowedModes = (Set) req.getAttribute(SportletProperties.ALLOWED_MODES);
        if (allowedModes.contains(portletMode.toString())) {
            // hack to handle config mode
            if (portletMode.toString().equals("config")) portletMode = new PortletMode("config");
            mode = portletMode;
        } else {
            throw new PortletModeException("Illegal portlet mode", portletMode);
        }
    }

    /**
     * Sets the given String parameter to this URL.
     * <p/>
     * This method replaces all parameters with the given key.
     * <p/>
     * The <code>PortletURL</code> implementation 'x-www-form-urlencoded' encodes
     * all  parameter names and values. Developers should not encode them.
     * <p/>
     * A portlet container may prefix the attribute names internally
     * in order to preserve a unique namespace for the portlet.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @throws IllegalArgumentException if name or value are <code>null</code>.
     */
    public void setParameter(String name, String value) {
        if ((name == null) || !(name instanceof String))
            throw new IllegalArgumentException("name must be a non-null string");
        if (value == null) throw new IllegalArgumentException("value is NULL");
        if (isRender) {
            store.put(SportletProperties.RENDER_PARAM_PREFIX + name, value);
        } else {
            store.put(name, value);
        }
    }

    /**
     * Sets the given String array parameter to this URL.
     * <p/>
     * This method replaces all parameters with the given key.
     * <p/>
     * The <code>PortletURL</code> implementation 'x-www-form-urlencoded' encodes
     * all  parameter names and values. Developers should not encode them.
     * <p/>
     * A portlet container may prefix the attribute names internally
     * in order to preserve a unique namespace for the portlet.
     *
     * @param name   the parameter name
     * @param values the parameter values
     * @throws IllegalArgumentException if name or values are <code>null</code>.
     */
    public void setParameter(String name, String[] values) {
        if ((name == null) || !(name instanceof String))
            throw new IllegalArgumentException("name must be a non-null string");
        if (values == null) throw new IllegalArgumentException("values is NULL");
        if (values.length == 0) throw new IllegalArgumentException("values is NULL");

        if (isRender) {
            store.put(SportletProperties.RENDER_PARAM_PREFIX + name, values);
        } else {
            store.put(name, values);
        }
    }

    /**
     * Sets a parameter map for this URL.
     * <p/>
     * All previously set parameters are cleared.
     * <p/>
     * The <code>PortletURL</code> implementation 'x-www-form-urlencoded' encodes
     * all  parameter names and values. Developers should not encode them.
     * <p/>
     * A portlet container may prefix the attribute names internally,
     * in order to preserve a unique namespace for the portlet.
     *
     * @param parameters Map containing parameter names for
     *                   the render phase as
     *                   keys and parameter values as map
     *                   values. The keys in the parameter
     *                   map must be of type String. The values
     *                   in the parameter map must be of type
     *                   String array (<code>String[]</code>).
     * @throws IllegalArgumentException if parameters is <code>null</code>, if
     *                                  any of the key/values in the Map are <code>null</code>,
     *                                  if any of the keys is not a String, or if any of
     *                                  the values is not a String array.
     */
    public void setParameters(java.util.Map parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters is NULL");
        }
        // All previously set parameters are cleared.
        this.store = new HashMap<String, Object>();
        for (Object key : parameters.keySet()) {
            if (key == null) throw new IllegalArgumentException("a parameters key is NULL");
            if (key instanceof String) {
                Object values = parameters.get(key);
                if (values == null) throw new IllegalArgumentException("a parameters value is NULL");
                if (!(values instanceof String[])) {
                    throw new IllegalArgumentException("a parameters value element must be a string array");
                }
                this.setParameter((String) key, (String[]) values);
            } else {
                throw new IllegalArgumentException("parameter key must be a string");
            }

        }
    }

    public void setAction(String action) {
        store.put(SportletProperties.DEFAULT_PORTLET_ACTION, action);
    }

    public void setRender(String render) {
        store.put(SportletProperties.DEFAULT_PORTLET_RENDER, render);
    }

    /**
     * Sets a label for this link, which will overwrite the component id
     *
     * @param label the link label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the layout id that identifies a layout descriptor to target
     *
     * @param layout the layout id that identifies a layout descriptor to target
     */
    public void setLayout(String layout) {
        this.layout = layout;
    }

    /**
     * Indicated the security setting for this URL.
     * <p/>
     * Secure set to <code>true</code> indicates that the portlet requests
     * a secure connection between the client and the portlet window for
     * this URL. Secure set to <code>false</code> indicates that the portlet
     * does not need a secure connection for this URL. If the security is not
     * set for a URL, it will stay the same as the current request.
     *
     * @param secure true, if portlet requests to have a secure connection
     *               between its portlet window and the client; false, if
     *               the portlet does not require a secure connection.
     * @throws PortletSecurityException if the run-time environment does
     *                                  not support the indicated setting
     */
    public void setSecure(boolean secure) throws PortletSecurityException {
        this.isSecure = secure;
    }

    public void setEncoding(boolean encoding) {
        this.encoding = encoding;
    }

    public boolean isEncoding() {
        return encoding;
    }

    /**
     * Returns the portlet URL string representation to be embedded in the
     * markup.<br>
     * Note that the returned String may not be a valid URL, as it may
     * be rewritten by the portal/portlet-container before returning the
     * markup to the client.
     *
     * @return the encoded URL as a string
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        String port = null;
        if (req.isSecure() || isSecure || (req.getAttribute(SportletProperties.SSL_REQUIRED) != null)) {
            s.append("https://");
            port = configService.getProperty(PortalConfigService.PORTAL_SECURE_PORT);
        } else {
            s.append("http://");
            port = configService.getProperty(PortalConfigService.PORTAL_PORT);
        }
        String hostname = configService.getProperty(PortalConfigService.PORTAL_HOST);

        if (hostname == null || hostname.equals("")) hostname = req.getServerName();
        s.append(hostname);
        if (!port.equals("80") && (!port.equals("443"))) {
            s.append(":");
            s.append((!port.equals("")) ? port : String.valueOf(req.getServerPort()));
        }

        // if underlying window state is floating then set it in the URI
        if (req.getAttribute(SportletProperties.FLOAT_STATE) != null) {
            store.put(SportletProperties.PORTLET_WINDOW, "FLOATING");
        }
        String contextPath = "/" + configService.getProperty("gridsphere.deploy");
        // handle ROOT context
        if (contextPath.equals("/")) contextPath = "";
        String servletPath = "/" + configService.getProperty("gridsphere.context");
        //String servletPath = req.getServletPath();
        String url = contextPath + servletPath;

        String cid = (String) req.getAttribute(SportletProperties.COMPONENT_ID);
        /*
          This bit of jiggery is here only for the LayoutManager portlet currently.
          A special param SportletProperties.EXTRA_QUERY_INFO can be used to stuff
          some extra params into every portal generated url
         */
        String extraQuery = (String) req.getAttribute(SportletProperties.EXTRA_QUERY_INFO);
        if (extraQuery != null) {
            StringTokenizer st = new StringTokenizer(extraQuery, "&");
            while (st.hasMoreTokens()) {
                String cmd = (String) st.nextElement();
                //System.err.println("cmd= " + cmd);

                if (cmd.startsWith(SportletProperties.COMPONENT_ID)) {
                    store.put(SportletProperties.COMPONENT_ID_2, cid);
                    cid = cmd.substring(SportletProperties.COMPONENT_ID.length() + 1);
                } else if (cmd.startsWith(SportletProperties.DEFAULT_PORTLET_ACTION)) {
                    String action = (String) store.get(SportletProperties.DEFAULT_PORTLET_ACTION);
                    store.put(SportletProperties.DEFAULT_PORTLET_ACTION_2, action);
                    store.put(SportletProperties.DEFAULT_PORTLET_ACTION, cmd.substring(SportletProperties.DEFAULT_PORTLET_ACTION.length() + 1));

                }
            }
        }

        String layoutId = layout;
        if (layoutId == null) {
            layoutId = (String) req.getAttribute(SportletProperties.LAYOUT_PAGE);
        }
        if (layoutId != null) {
            //System.err.println("layoutId=" + layoutId);
            url += "/" + layoutId;
            //String compVar = (String)req.getAttribute(SportletProperties.COMPONENT_ID_VAR);
            //if (compVar == null) compVar = SportletProperties.COMPONENT_ID;

            /*
             // if a label exists, use it instead
            if (label != null) {
                cid = label;
            } else{
                cid = (String)req.getAttribute(SportletProperties.COMPONENT_ID);
            }
            */
            // if a label exists, use it instead
            if (label != null) cid = label;

            if (cid != null) {
                url += "/" + cid;
                if (mode != null) url += "/m/" + mode.toString();
                if (state != null) url += "/s/" + state.toString();
                String action = (String) store.get(SportletProperties.DEFAULT_PORTLET_ACTION);
                if (action != null) {
                    store.remove(SportletProperties.DEFAULT_PORTLET_ACTION);
                    url += "/a/" + action;
                }
                String render = (String) store.get(SportletProperties.DEFAULT_PORTLET_RENDER);
                if (render != null) {
                    store.remove(SportletProperties.DEFAULT_PORTLET_RENDER);
                    url += "/r/" + render;
                }
            }
            //System.err.println("url=" + layoutId);
        }
        ///////////// JASON ADDED ABOVE
        Set set = store.keySet();
        if (!set.isEmpty()) {
            // add question mark
            url += "?";

            Iterator it = set.iterator();
            boolean firstParam = true;
            try {
                while (it.hasNext()) {
                    if (!firstParam) url += "&";
                    String name = (String) it.next();

                    String encname = null;
                    encname = URLEncoder.encode(name, "UTF-8");

                    Object val = store.get(name);
                    if (val instanceof String[]) {
                        String[] vals = (String[]) val;
                        for (int j = 0; j < vals.length - 1; j++) {
                            String encvalue = URLEncoder.encode(vals[j], "UTF-8");
                            url += encname + "=" + encvalue + "&";
                        }
                        String encvalue = URLEncoder.encode(vals[vals.length - 1], "UTF-8");
                        url += encname + "=" + encvalue;
                    } else if (val instanceof String) {
                        String aval = (String) store.get(name);
                        if ((aval != null) && (!aval.equals(""))) {
                            String encvalue = URLEncoder.encode(aval, "UTF-8");
                            url += encname + "=" + encvalue;
                        } else {
                            url += encname;
                        }
                    }
                    firstParam = false;
                }

            } catch (UnsupportedEncodingException e) {
                System.err.println("Unable to support UTF-8 encoding!");
            }

            if (encoding) {
                if (redirect) {
                    url = res.encodeRedirectURL(url);
                } else {
                    url = res.encodeURL(url);
                }
            }
        }
        s.append(url);
        //System.err.println("created URL= " + s.toString());
        return s.toString();
    }

}
