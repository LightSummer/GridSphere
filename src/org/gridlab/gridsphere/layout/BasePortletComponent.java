/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.layout;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.gridlab.gridsphere.portlet.PortletGroup;
import org.gridlab.gridsphere.portlet.PortletGroupFactory;
import org.gridlab.gridsphere.portlet.PortletMessage;
import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.impl.SportletProperties;
import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;

/**
 * <code>BasePortletComponent</code> represents an abstract portlet component with a particular
 * size, layout and theme and is subclasses by concrete portlet component instances.
 */
public abstract class BasePortletComponent extends BaseComponentLifecycle implements PortletComponent, Serializable {

    protected PortletComponent parent;
    protected String defaultWidth = new String();
    protected String width = new String();
    protected String label = new String();
    protected String name = new String();
    protected String theme = "default";
    protected String componentTheme = "";
    protected boolean isVisible = true;
    protected String roleString = PortletRole.GUEST.toString();
    protected String groupString = PortletGroupFactory.GRIDSPHERE_GROUP.getName();
    protected PortletRole requiredRole = PortletRole.GUEST;
    protected PortletGroup requiredGroup = PortletGroupFactory.GRIDSPHERE_GROUP;
    protected List listeners = null;
    protected StringBuffer bufferedOutput = new StringBuffer();

    /**
     * Initializes the portlet component. Since the components are isolated
     * after Castor unmarshalls from XML, the ordering is determined by a
     * passed in List containing the previous portlet components in the tree.
     *
     * @param list a list of component identifiers
     * @return a list of updated component identifiers
     * @see ComponentIdentifier
     */
    public List init(PortletRequest req, List list) {
        listeners = new Vector();
        defaultWidth = width;
        if (roleString != null) {
            try {
                requiredRole = PortletRole.toPortletRole(roleString);
            } catch (IllegalArgumentException e) {
                requiredRole = PortletRole.GUEST;
            }
        }
        if ((label == null) || label.equals("")) {
            return super.init(req, list);

        } else {
            this.COMPONENT_ID = list.size();
            componentIDStr = label;
            return list;
        }
    }

    /**
     * Returns the portlet component name
     *
     * @return the portlet component name
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the portlet component name
     *
     * @param name the portlet component name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the theme for on component annd its childs
     *
     * @return name of the componenttheme
     */
    public String getComponentTheme() {
        return componentTheme;
    }

    /**
     * Set the theme for the component and its childs.
     *
     * @param componentTheme
     */
    public void setComponentTheme(String componentTheme) {
        System.out.println("\n\n\n\n\n\n\n\n\n SET COMP THEME \n\n\n\n\n\n\n");
        this.componentTheme = componentTheme;
    }

    /**
     * Returns the componentTheme for usage directly in HTML/css
     *
     * @return formatted componentTheme to use with html/css
     */
    protected String getFormattedComponentTheme() {
        if (this.componentTheme.equals("")) {
            return this.componentTheme;
        } else {
            System.out.println("\n\n\n\n WE HAVE A COMP THEEME HERE!!!!!\n\n\n\n");
            return "-"+this.componentTheme;
        }
    }


    /**
     * Returns the portlet component label
     *
     * @return the portlet component label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the portlet component label
     *
     * @param label the portlet component label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @param roleString the required portlet role expresses as a <code>String</code>
     */
    public void setRequiredRoleAsString(String roleString) {
        this.roleString = roleString;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @return the required portlet role expresses as a <code>String</code>
     */
    public String getRequiredRoleAsString() {
        return roleString;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @param requiredRole the required portlet role expresses as a <code>String</code>
     */
    public void setRequiredRole(PortletRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    /**
     * Allows a required role to be associated with viewing this portlet
     *
     * @return the required portlet role expresses as a <code>PortletRole</code>
     */
    public PortletRole getRequiredRole() {
        return requiredRole;
    }

        /**
     * Allows a required group to be associated with viewing this portlet
     *
     * @param groupString the required portlet group expresses as a <code>String</code>
     */
    public void setRequiredGroupAsString(String groupString) {
        this.groupString = groupString;
    }

    /**
     * Allows a required group to be associated with viewing this portlet
     *
     * @return the required portlet group expresses as a <code>String</code>
     */
    public String getRequiredGroupAsString() {
        return groupString;
    }

    /**
     * Allows a required group to be associated with viewing this portlet
     *
     * @param requiredGroup the required portlet group expresses as a <code>String</code>
     */
    public void setRequiredGroup(PortletGroup requiredGroup) {
        this.requiredGroup = requiredGroup;
    }

    /**
     * Allows a required group to be associated with viewing this portlet
     *
     * @return the required portlet group expresses as a <code>PortletGroup</code>
     */
    public PortletGroup getRequiredGroup() {
        return requiredGroup;
    }

    /**
     * Sets the portlet component width
     *
     * @param width the portlet component width
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * Returns the portlet component width
     *
     * @return the portlet component width
     */
    public String getWidth() {
        return width;
    }

    /**
     * Returns the default portlet component width
     *
     * @return the default portlet component width
     */
    public String getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * When set to true the portlet component is visible and will be rendered
     *
     * @param isVisible if <code>true</code> portlet component is rendered,
     * <code>false</code> otherwise
     */
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Return true if the portlet component visibility is true
     *
     * @return the portlet component visibility
     */
    public boolean getVisible() {
        return isVisible;
    }

    /**
     * Sets the theme of this portlet component
     *
     * @param theme the theme of this portlet component
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Return the theme of this portlet component
     *
     * @return the theme of this portlet component
     */
    public String getTheme() {
        return theme;
    }

    public PortletComponent getParentComponent() {
        return parent;
    }

    public void setParentComponent(PortletComponent parent) {
        this.parent = parent;
    }

    public void remove(PortletComponent pc, PortletRequest req) {

    }

    /**
     * Performs an action on this portlet component
     *
     * @param event a gridsphere event
     * @throws PortletLayoutException if a layout error occurs during rendering
     * @throws IOException if an I/O error occurs during rendering
     */
    public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
        super.actionPerformed(event);
    }

    /**
     * Renders the portlet component
     *
     * @param event a gridsphere event
     * @throws PortletLayoutException if a layout error occurs during rendering
     * @throws IOException if an I/O error occurs during rendering
     */
    public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
        PortletRequest req = event.getPortletRequest();
        req.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);

    }

    public StringBuffer getBufferedOutput() {
        return bufferedOutput;
    }

    public void addComponentListener(PortletComponent component) {
        listeners.add(component);
    }

    public Object clone() throws CloneNotSupportedException {
            BasePortletComponent b = (BasePortletComponent)super.clone();
            b.width = this.width;
            b.isVisible = this.isVisible;
            b.name = this.name;
            b.theme = this.theme;
            b.label = this.label;
            b.roleString = this.roleString;
            b.requiredRole = ((this.requiredRole != null) ? (PortletRole)this.requiredRole.clone() : null);
            return b;
    }



        /* (non-Javadoc)
         * @see org.gridlab.gridsphere.layout.PortletComponent#messageEvent(java.lang.String, org.gridlab.gridsphere.portlet.PortletMessage, org.gridlab.gridsphere.portletcontainer.GridSphereEvent)
         */
        public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
                Iterator iter = listeners.iterator();
                while (iter.hasNext()) {
                        PortletComponent comp = (PortletComponent) iter.next();
                        comp.messageEvent(concPortletID, msg, event);
                }

        }

}
