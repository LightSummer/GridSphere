/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.services.container.registry.impl;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.SportletSettings;
import org.gridlab.gridsphere.portlet.impl.SportletData;
import org.gridlab.gridsphere.portlet.impl.SportletGroup;
import org.gridlab.gridsphere.portlet.impl.SportletConfig;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.services.container.registry.PortletRegistryService;
import org.gridlab.gridsphere.services.container.registry.PortletUserRegistryService;
import org.gridlab.gridsphere.services.container.registry.PortletRegistryServiceException;
import org.gridlab.gridsphere.services.security.acl.AccessControlService;
import org.gridlab.gridsphere.portletcontainer.ConcretePortlet;
import org.gridlab.gridsphere.portletcontainer.ApplicationPortlet;
import org.gridlab.gridsphere.portletcontainer.descriptor.Owner;

import javax.servlet.ServletConfig;
import javax.servlet.UnavailableException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.io.File;

/**
 * Manages user portlet instances characterized by a user's list of concrete portlet ID's and the persistent UserData
 * of each one
 */
public class PortletUserRegistryServiceImpl implements PortletUserRegistryService, PortletServiceProvider {

    private static PortletServiceFactory factory = SportletServiceFactory.getInstance();
    private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletUserRegistryServiceImpl.class);

    private static PortletRegistryService registryService = null;
    private static AccessControlService aclService = null;

    private String rootPath = null;

    private ServletConfig config = null;

    //private Map userPortlets = new Hashtable();

    // A Vector of UserPortlets objects
    private List userPortlets = new Vector();


    public class UserPortlets {

        // the user ID
        private String userID = "";

        // the list of UserPortletInfos
        private List portlets = new Vector();

        public class UserPortletInfo {

            private String portletID;
            private SportletData sportletData;

            public SportletData getSportletData() {
                return sportletData;
            }

            public void setPortletData(SportletData sportletData) {
                this.sportletData = sportletData;
            }

            public String getPortletID() {
                return portletID;
            }

            public void setPortletID(String portletID) {
                this.portletID = portletID;
            }
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        public void setUserPortletInfo(List portlets) {
            this.portlets = portlets;
        }

        public List getUserPortletInfo() {
            return portlets;
        }

        public void addPortlet(UserPortletInfo userPortletInfo) {
            portlets.add(userPortletInfo);
        }

        public void removePortlet(String portletID) {
            portlets.remove(portletID);
        }

        public void removePortlet(UserPortletInfo userPortletInfo) {
            if (portlets.contains(userPortletInfo))
                portlets.remove(userPortletInfo);
        }
    }

    /**
     * The init method is responsible for parsing portlet.xml and creating ConcretePortlet objects based on the
     * entries. The RegisteredPortlets are managed by the PortletRegistryService.
     */
    public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
        log.info("in init()");
        // Need a portlet registry service
        this.config = config.getServletConfig();
        rootPath = config.getServletConfig().getServletContext().getRealPath("");
        try {
            registryService = PortletRegistryServiceImpl.getInstance();
        } catch (PortletRegistryServiceException e) {
            throw new PortletServiceUnavailableException("Unable to get instance of PortletRegistryService");
        }
        try {
            aclService = (AccessControlService) factory.createPortletService(AccessControlService.class, config.getServletConfig(), true);
        } catch (PortletServiceException e) {
            throw new PortletServiceUnavailableException("Unable to get instance of AccessControlService");
        }
    }

    public void destroy() {
        log.info("in destroy()");
    }

    /**
     * Login retrieves the user's PortletData for their list of portlets
     */
    public void loginPortlets(PortletRequest request) {
        log.info("in login()");

        User user = request.getUser();
        List usersPortlets = new Vector();
        // based on user.getID() get their UserPortlet and then getPortlets
        Iterator it = usersPortlets.iterator();
        while (it.hasNext()) {
            String portletID = (String) it.next();
            AbstractPortlet portlet = registryService.getActivePortlet(portletID);
            portlet.login(request);
        }
    }

    /**
     * Logout serializes user's PortletData of their list of portlets
     */
    public void logoutPortlets(PortletRequest request) {
        log.info("in logout()");

        // based on user.getID() get their UserPortlet and then getPortlets
        List usersPortlets = new Vector();
        Iterator it = usersPortlets.iterator();
        while (it.hasNext()) {
            String portletID = (String) it.next();
            AbstractPortlet portlet = registryService.getActivePortlet(portletID);
            portlet.logout(request.getPortletSession());
        }
    }


    public void reloadPortlets() throws PortletRegistryServiceException {
        registryService.loadPortlets();
        initializePortlets();
    }

    public void initializePortlets() {

        Iterator it = registryService.getLocalConcretePortlets().iterator();
        try {
            while (it.hasNext()) {
                ConcretePortlet concretePortlet = (ConcretePortlet) it.next();

                System.err.println("portlet name: " + concretePortlet.getPortletName());
                AbstractPortlet abPortlet = concretePortlet.getAbstractPortlet();
                PortletConfig portletConfig = getPortletConfig(config, concretePortlet.getConcretePortletAppID());
                PortletSettings portletSettings = concretePortlet.getSportletSettings();
                abPortlet.init(portletConfig);
                abPortlet.initConcrete(portletSettings);
            }
        } catch (UnavailableException e) {
            log.error("Caught Unavailable exception: ", e);
        }
    }

    public void shutdownPortlets() {
        // Shut down all PortletInfo Services.
        log.info("Shutting down GridSphere");

        // Destroy all portlets
        Iterator it = registryService.getLocalConcretePortlets().iterator();
        while (it.hasNext()) {
            ConcretePortlet concPortlet = (ConcretePortlet)it.next();
            AbstractPortlet ab = concPortlet.getAbstractPortlet();
            PortletSettings portletSettings = concPortlet.getSportletSettings();
            PortletConfig portletConfig = getPortletConfig(config, concPortlet.getConcretePortletAppID());
            ab.destroyConcrete(portletSettings);
            ab.destroy(portletConfig);
        }
    }

    /**
     * Returns the collection of registered portlets for a user, null if none exists
     *
     * @return the registered portlets
     */
    public List getPortlets(PortletRequest request) {
        String uid = request.getUser().getID();
        return null;
    }

    public void addPortlet(PortletRequest request, String concretePortletID) {
        String uid = request.getUser().getID();

    }

    public void removePortlet(PortletRequest request, String concretePortletID) {
        String uid = request.getUser().getID();
    }



    /*
    public List getSupportedModes(PortletRequest request, String concretePortletID) {
        User user = request.getUser();

        ConcretePortlet concretePortlet = registryService.getConcretePortlet(concretePortletID);
        ApplicationPortlet appPortlet = registryService.getApplicationPortlet(concretePortletID);

        List modeList = appPortlet.;


        Owner owner = regPortlet.getPortletOwner();
        PortletRole ownerRole = owner.getRole();
        PortletGroup ownerGroup = owner.getGroup();
        if (modeList.contains(Portlet.Mode.CONFIGURE)) {
            try {
                if (!aclService.hasRoleInGroup(user, ownerGroup, ownerRole))
                    modeList.remove(Portlet.Mode.CONFIGURE);
            } catch (PortletServiceException e) {
                log.error("Unable to get access control service", e);
            }
        }
        return modeList;
    }
    */

    public PortletConfig getPortletConfig(ServletConfig servletConfig, String concretePortletID) {
        int index = concretePortletID.lastIndexOf(".");
        String appPortletID = concretePortletID.substring(0, index);
        System.err.println(appPortletID);
        ApplicationPortlet appPortlet = registryService.getApplicationPortlet(appPortletID);
        return new SportletConfig(servletConfig, appPortlet.getPortletApplication());
    }

    public PortletSettings getPortletSettings(PortletRequest request, String concretePortletID) {
        User user = request.getUser();

        ConcretePortlet concretePortlet = registryService.getConcretePortlet(concretePortletID);
        SportletSettings sportletSettings = concretePortlet.getSportletSettings();

        Owner owner = concretePortlet.getPortletOwner();

        Portlet.Mode mode = request.getMode();

        if (mode == Portlet.Mode.CONFIGURE) {
            PortletRole ownerRole = owner.getRole();
            PortletGroup ownerGroup = owner.getGroup();
            try {
                if ((aclService.isSuperUser(user)) || (aclService.hasRoleInGroup(user, ownerGroup, ownerRole))) {
                    sportletSettings = concretePortlet.getSportletSettings();
                    sportletSettings.enableConfigurePermission(true);
                }
            }catch (PortletServiceException e) {
                log.error("Unable to get access control service", e);
            }
        }
        return sportletSettings;
    }

    public PortletData getPortletData(PortletRequest request, String concretePortletID) {
        User user = request.getUser();

        // get SportletData from userPortlets
        SportletData data = (SportletData)userPortlets.get(0);
        Portlet.Mode mode = request.getMode();

        if (mode == Portlet.Mode.EDIT) {
            data.enableConfigurePermission(true);
        }
        return data;
    }

}
