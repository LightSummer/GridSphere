/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.services.core.layout;

import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.portlet.service.PortletService;
import org.gridlab.gridsphere.layout.PortletTab;
import org.gridlab.gridsphere.layout.PortletPage;

import java.util.List;

/**
 * The <code>LayoutManagerService</code> manages users layouts
 */
public interface LayoutManagerService extends PortletService {

    //public String[] getPortletClassNames(PortletRequest req);

    public void setTheme(PortletRequest req, String theme);

    public String getTheme(PortletRequest req);

    public PortletPage getPortletPage(PortletRequest req);

    public void addPortletTab(PortletRequest req, PortletTab tab);

    public void removePortlets(PortletRequest req, List portletClassNames);

    public List getAllPortletNames(PortletRequest req);

    public void reloadPage(PortletRequest req);

    public String[] getTabNames(PortletRequest req);

    public void setTabNames(PortletRequest req, String[] tabNames);

    public String[] getSubTabNames(PortletRequest req, String tabName);

    public void setSubTabNames(PortletRequest req, String tabName, String[] subTabNames);

    public String[] getFrameClassNames(PortletRequest req, String subTabName);

    public void setFrameClassNames(PortletRequest req, String subTabName, String[] frameNames);

    public void removeTab(String tabName);

    public void removeFrame(String frameClassName);

}
