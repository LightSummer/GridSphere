/*
 * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @team sonicteam
 * @version $Id$
 */

package org.gridlab.gridsphere.portletcontainer.impl.descriptor;

import org.gridlab.gridsphere.core.persistence.castor.descriptor.ConfigParamList;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portletcontainer.ConcretePortletConfig;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * The <code>ConcreteSportletConfig</code> provides an implementation of
 * <code>ConcretePortletConfig</code> that uses Castor for XMl to Java
 * data bindings.
 * <p>
 * The <code>ConcreteSportletConfig</code> provides concrete portlet
 * configuration information.
 */
public class ConcreteSportletConfig implements ConcretePortletConfig {

    private String defaultLocale = new String();
    private List languageList = new ArrayList();
    private String name = new String();
    private RequiredRole accessRestrictions = new RequiredRole();
    private ConfigParamList configParamList = new ConfigParamList();

    /**
     * Constructs an instance of ConcreteSportletConfig.
     */
    public ConcreteSportletConfig() {
    }

    /**
     * gets the default locale of a portlet
     *
     * @return the default locale of the portlet
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the default locale of a portlet
     *
     * @param defaultLocale the default locale of the portlet
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Returns the language info of a portlet
     *
     * @return language info of the portlet
     */
    public List getLanguageList() {
        return languageList;
    }

    /**
     * sets the language info list of a portlet
     *
     * @param languageList the language info list of the portlet
     */
    public void setLanguageList(ArrayList languageList) {
        this.languageList = languageList;
    }

    /**
     * gets the name of the portlet
     *
     * @return the name of the portlet
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the portlet
     *
     * @param name name of the portlet
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Used internally by Castor. Clients should use #getConfigParams
     * instead
     * <p>
     * Returns the configuration parameters of the portlet
     *
     * @return the configuration parameters of the portlet
     */
    public List getConfigParamList() {
        return configParamList.getConfigParamList();
    }

    /**
     * Used internally by Castor.
     * <p>
     * Sets the configuration parameters of the portlet
     *
     * @param configParamList the configuration parameters of the portlet
     */
    public void setConfigParamList(ArrayList configParamList) {
        this.configParamList.setConfigParamList(configParamList);
    }

    /**
     * Returns the configuration parameters of the portlet
     *
     * @return the configuration parameters of the portlet
     */
    public Hashtable getConfigAttributes() {
        return configParamList.getConfigParams();
    }

    /**
     * Sets the configuration parameters of the portlet
     *
     * @param configAttrs the configuration parameters of the portlet
     */
    public void setConfigAttributes(Hashtable configAttrs) {
        this.configParamList.setConfigParams(configAttrs);
    }

    /**
     * Used internally by Castor. Clients should use #getConcretePortletScope
     * and #getRequiredRole instead
     * <p>
     * Sets the access restrictions for this portlet
     */
    public void setAccessRestrictions(RequiredRole accessRestrictions) {
        this.accessRestrictions = accessRestrictions;
    }

    /**
     * Used internally by Castor. Clients should use #getConcretePortletScope
     * and #getRequiredRole instead
     * <p>
     * Returns the access restrictions for this portlet
     */
    public RequiredRole getAccessRestrictions() {
        return accessRestrictions;
    }

    /**
     * Returns the accessibility scope of this portlet
     *
     * @return the accessibility scope of this portlet
     */
    public ConcretePortletConfig.Scope getConcretePortletScope() {
        return accessRestrictions.getScope();
    }

    /**
     * Sets the accessibility scope of this portlet
     *
     * @param scope the accessibility scope of this portlet
     */
    public void setConcretePortletScope(ConcretePortletConfig.Scope scope) {
        this.accessRestrictions.setScope(scope);
    }

    /**
     * Returns the required portlet role necessary to access this portlet
     *
     * @return the required portlet role necessary to access this portlet
     */
    public PortletRole getRequiredRole() {
        return accessRestrictions.getPortletRole();
    }

    /**
     * Sets the required portlet role necessary to access this portlet
     *
     * @param role the required portlet role necessary to access this portlet
     */
    public void setRequiredRole(PortletRole role) {
        accessRestrictions.setPortletRole(role);
    }

}

