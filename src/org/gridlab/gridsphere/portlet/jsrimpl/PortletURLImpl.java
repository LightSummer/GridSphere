
package org.gridlab.gridsphere.portlet.jsrimpl;

import org.gridlab.gridsphere.portlet.impl.SportletProperties;

import javax.portlet.WindowStateException;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.portlet.PortletModeException;
import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.net.URLEncoder;


/**
 * The <CODE>PortletURL</CODE> interface represents a URL
 * that reference the portlet itself.
 * <p>
 * A PortletURL is created through the <CODE>RenderResponse</CODE>.
 * Parameters, a portlet mode, a window state and a security level
 * can be added to <CODE>PortletURL</CODE> objects. The PortletURL
 * must be converted to a String in order to embed it into
 * the markup generated by the portlet.
 * <P>
 * There are two types of PortletURLs:
 * <ul>
 * <li>Action URLs, they are created with <CODE>RenderResponse.createActionURL</CODE>, and
 *     trigger an action request followed by a render request.
 * <li>Render URLs, they are created with <CODE>RenderResponse.createRenderURL</CODE>, and
 *     trigger a render request.
 * </ul>
 * <p>
 * The string reprensentation of a PortletURL does not need to be a valid
 * URL at the time the portlet is generating its content. It may contain
 * special tokens that will be converted to a valid URL, by the portal,
 * before the content is returned to the client.
 */
public class PortletURLImpl implements PortletURL {

    private HttpServletResponse res = null;
    private HttpServletRequest req = null;
    private boolean isSecure = false;
    private Map store = new HashMap();
    private boolean redirect = true;
    private String contextPath = null;

    /**
     * Constructs a PortletURL from a servlet request and response
     *
     * @param req the servlet request
     * @param res the servlet response
     */
    public PortletURLImpl(HttpServletRequest req, HttpServletResponse res) {
        this.store = new HashMap();
        this.res = res;
        this.req = req;
        this.contextPath = req.getContextPath();
        this.isSecure = req.isSecure();
        store.put(SportletProperties.COMPONENT_ID, (String) req.getAttribute(SportletProperties.COMPONENT_ID));
    }

    /**
     * Indicates the window state the portlet should be in, if this
     * portlet URL triggers a request.
     * <p>
     * A URL can not have more than one window state attached to it.
     * If more than one window state is set only the last one set
     * is attached to the URL.
     *
     * @param windowState
     *               the portlet window state
     *
     * @exception WindowStateException
     *                   if the portlet cannot switch to this state,
     *                   because the portal does not support this state, the portlet has not
     *                   declared in its deployment descriptor that it supports this state, or the current
     *                   user is not allowed to switch to this state.
     *                   The <code>PortletRequest.isWindowStateAllowed()</code> method can be used
     *                   to check if the portlet can set a given window state.
     * @see PortletRequest#isWindowStateAllowed
     */
    public void setWindowState(WindowState windowState)
            throws WindowStateException {
        store.put(SportletProperties.PORTLET_WINDOW, windowState.toString());
    }

    /**
     * Indicates the portlet mode the portlet must be in, if this
     * portlet URL triggers a request.
     * <p>
     * A URL can not have more than one portlet mode attached to it.
     * If more than one portlet mode is set only the last one set
     * is attached to the URL.
     *
     * @param portletMode
     *               the portlet mode
     *
     * @exception PortletModeException
     *                   if the portlet cannot switch to this mode,
     *                   because the portal does not support this mode, the portlet has not
     *                   declared in its deployment descriptor that it supports this mode for the current markup,
     *                   or the current user is not allowed to switch to this mode.
     *                   The <code>PortletRequest.isPortletModeAllowed()</code> method can be used
     *                   to check if the portlet can set a given portlet mode.
     * @see PortletRequest#isPortletModeAllowed
     */
    public void setPortletMode(PortletMode portletMode)
            throws PortletModeException {
        store.put(SportletProperties.PORTLET_MODE, portletMode.toString());
    }

    /**
     * Sets the given String parameter to this URL.
     * <p>
     * This method replaces all parameters with the given key.
     * <p>
     * The <code>PortletURL</code> implementation 'x-www-form-urlencoded' encodes
     * all  parameter names and values. Developers should not encode them.
     * <p>
     * A portlet container may prefix the attribute names internally
     * in order to preserve a unique namespace for the portlet.
     *
     * @param   name
     *          the parameter name
     * @param   value
     *          the parameter value
     *
     * @exception  IllegalArgumentException
     *                            if name or value are <code>null</code>.
     */
    public void setParameter(String name, String value) {
        if (name == null) throw new IllegalArgumentException("name is NULL");
        if (value == null) throw new IllegalArgumentException("value is NULL");
        store.put(name, value);
    }

    /**
     * Sets the given String array parameter to this URL.
     * <p>
     * This method replaces all parameters with the given key.
     * <p>
     * The <code>PortletURL</code> implementation 'x-www-form-urlencoded' encodes
     * all  parameter names and values. Developers should not encode them.
     * <p>
     * A portlet container may prefix the attribute names internally
     * in order to preserve a unique namespace for the portlet.
     *
     * @param   name
     *          the parameter name
     * @param   values
     *          the parameter values
     *
     * @exception  IllegalArgumentException
     *                            if name or values are <code>null</code>.
     */
    public void setParameter(String name, String[] values) {
        if (name == null) throw new IllegalArgumentException("name is NULL");
        if (values == null) throw new IllegalArgumentException("values is NULL");
        if (values.length == 0) throw new IllegalArgumentException("values is NULL");
        store.put(name, values);
    }

    /**
     * Sets a parameter map for this URL.
     * <p>
     * All previously set parameters are cleared.
     * <p>
     * The <code>PortletURL</code> implementation 'x-www-form-urlencoded' encodes
     * all  parameter names and values. Developers should not encode them.
     * <p>
     * A portlet container may prefix the attribute names internally,
     * in order to preserve a unique namespace for the portlet.
     *
     * @param  parameters   Map containing parameter names for
     *                      the render phase as
     *                      keys and parameter values as map
     *                      values. The keys in the parameter
     *                      map must be of type String. The values
     *                      in the parameter map must be of type
     *                      String array (<code>String[]</code>).
     *
     * @exception	IllegalArgumentException
     *                      if parameters is <code>null</code>, if
     *                      any of the key/values in the Map are <code>null</code>,
     *                      if any of the keys is not a String, or if any of
     *                      the values is not a String array.
     */
    public void setParameters(java.util.Map parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters is NULL");
        }
        Iterator it = parameters.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if (key == null) throw new IllegalArgumentException("a parameters key is NULL");
            if (key instanceof String) {
                Object values = parameters.get(key);
                if (values == null) throw new IllegalArgumentException("a parameters value is NULL");
                if (!(values instanceof String[]) && (!(values instanceof String))) {
                    throw new IllegalArgumentException("a parameters value element must be a string or string array");
                }
                this.setParameter((String)key, (String[])values);
            }

        }
    }


    /**
     * Indicated the security setting for this URL.
     * <p>
     * Secure set to <code>true</code> indicates that the portlet requests
     * a secure connection between the client and the portlet window for
     * this URL. Secure set to <code>false</code> indicates that the portlet
     * does not need a secure connection for this URL. If the security is not
     * set for a URL, it will stay the same as the current request.
     *
     * @param  secure  true, if portlet requests to have a secure connection
     *                 between its portlet window and the client; false, if
     *                 the portlet does not require a secure connection.
     *
     * @throws PortletSecurityException  if the run-time environment does
     *                                   not support the indicated setting
     */
    public void setSecure(boolean secure) throws PortletSecurityException {
        this.isSecure = secure;
    }


    /**
     * Returns the portlet URL string representation to be embedded in the
     * markup.<br>
     * Note that the returned String may not be a valid URL, as it may
     * be rewritten by the portal/portlet-container before returning the
     * markup to the client.
     *
     * @return   the encoded URL as a string
     */
    public String toString () {
        StringBuffer s = new StringBuffer();
        if (req.isSecure() || isSecure) {
            s.append("https://");
        } else {
            s.append("http://");
        }
        s.append(req.getServerName() + ":" + req.getServerPort());

        String url = contextPath;
        String newURL;
        Set set = store.keySet();
        if (!set.isEmpty()) {
            // add question mark
            url = contextPath + contextPath + "?";
        } else {
            return contextPath + url;
        }
        boolean firstParam = true;
        Iterator it = set.iterator();
        //try {
        while (it.hasNext()) {
            if (!firstParam)
                url += "&";
            String name = (String)it.next();

            String encname = URLEncoder.encode(name);
            Object val = store.get(name);
            if (val instanceof String) {

            } else if (val instanceof String[]) {
                String[] vals = (String[])val;
                for (int j = 0; j < vals.length; j++) {
                    String encvalue = URLEncoder.encode(vals[j]);
                    url += encname + "=" + encvalue;
                }
            } else if (val instanceof String) {
                String aval = (String) store.get(name);
                if ((aval != null) && (aval != "")) {
                    String encvalue = URLEncoder.encode(aval);
                    url += encname + "=" + encvalue;
                } else {
                    url += encname;
                }
            }
            firstParam = false;
        }
        /*
        } catch (UnsupportedEncodingException e) {
        System.err.println("Unable to support UTF-8 encoding!");
        }*/
        if (redirect) {
            newURL = res.encodeRedirectURL(url);
        } else {
            newURL = res.encodeURL(url);
        }
        s.append(newURL);
        return s.toString();
    }

}
