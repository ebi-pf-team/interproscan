package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

/**
 * @author Phil Jones
 *         Date: 13/04/12
 */
public abstract class AbstractDBService {

    protected static final String DATA_PATH_JAVA_OPTION = "berkely.db.data";

    protected String setPath(String pathProperty) {
        if (pathProperty.contains(DATA_PATH_JAVA_OPTION)) {
            String dataPath = System.getProperty(DATA_PATH_JAVA_OPTION);
            if (dataPath == null || dataPath.isEmpty()) {
                throw new IllegalStateException("This server has been configured to lookup a Java option " + DATA_PATH_JAVA_OPTION + " however this option has not been set.  This should be set in the Tomcat Controller Script, e.g. JAVA_OPTS=\"$JAVA_OPTS -D" + DATA_PATH_JAVA_OPTION + "=${CATALINA_BASE}/deploy/data:/${TOMCAT_HOSTNAME##*-}\"");
            }
            return pathProperty.replace(DATA_PATH_JAVA_OPTION, dataPath);
        } else return pathProperty;
    }
}
