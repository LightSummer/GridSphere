/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.core.persistence;

import org.gridlab.gridsphere.core.persistence.*;
import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerRdbmsImpl;
import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerXmlImpl;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;

import java.util.*;

public class PersistenceManagerFactory {

    protected transient static PortletLog log = SportletLog.getInstance(PersistenceManagerFactory.class);

    protected static Map databases = new HashMap();

    private PersistenceManagerFactory() {
        log.info("Entering PersistenceManagerFactory");
    }

    public static synchronized PersistenceManagerRdbms createGridSphereRdbms() {

        String databaseConfigFile = GridSphereConfig.getProperty(GridSphereConfigProperties.DATABASE_CONFIG);
        String databaseName = GridSphereConfig.getProperty(GridSphereConfigProperties.DATABASE_NAME);

        if (!databases.containsKey(databaseName)) {
            PersistenceManagerRdbms pm = new PersistenceManagerRdbmsImpl(databaseName, databaseConfigFile);
            databases.put(databaseName, pm);
        }
        return (PersistenceManagerRdbms)databases.get(databaseName);
    }

    public static synchronized PersistenceManagerRdbms createProjectPersistenceManagerRdbms(String databaseName) {
        if (!databases.containsKey(databaseName)) {
            String databaseConfigFile = GridSphereConfig.getProperty(GridSphereConfigProperties.GRIDSPHERE_HOME) +
                    databaseName + "/conf/database.xml";
            PersistenceManagerRdbms pm = new PersistenceManagerRdbmsImpl(databaseName, databaseConfigFile);
            databases.put(databaseName, pm);
        }
        return (PersistenceManagerRdbms)databases.get(databaseName);
    }

    public static synchronized PersistenceManagerRdbms createPersistenceManagerRdbms(String databaseName, String databaseConfigFile) {
        if (!databases.containsKey(databaseName)) {

            PersistenceManagerRdbms pm = new PersistenceManagerRdbmsImpl(databaseName, databaseConfigFile);
            databases.put(databaseName, pm);
        }
        return (PersistenceManagerRdbms)databases.get(databaseName);
    }

    /**
     * Returns an instance of a PersistenceManagerXML from a descriptor and mapping URL
     *
     * @param descriptorURL the descriptor location
     * @param mappingURL the mapping location
     * @return an instance of PersistenceManagerXmlImpl
     */
    public static PersistenceManagerXml createPersistenceManagerXml(String descriptorURL, String mappingURL) {
        return new PersistenceManagerXmlImpl(descriptorURL, mappingURL);
    }

}
