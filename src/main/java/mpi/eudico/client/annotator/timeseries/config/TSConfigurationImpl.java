package mpi.eudico.client.annotator.timeseries.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;


/**
 * Default implementation of TSConfiguration.
 *
 * @author Han Sloetjes
 */
public class TSConfigurationImpl implements TSConfiguration {
    /** the properties */
    protected Properties properties;

    /** a map for configuration or track objects */
    protected HashMap objectMap;

    /**
     * Creates a new TSConfigurationImpl instance
     */
    public TSConfigurationImpl() {
        properties = new Properties();
        objectMap = new HashMap();
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#setProperty(java.lang.String,
     *      java.lang.String)
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#getProperty(java.lang.String)
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#removeProperty(java.lang.String)
     */
    public Object removeProperty(String key) {
        return properties.remove(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#propertyNames()
     */
    public Enumeration propertyNames() {
        return properties.propertyNames();
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#putObject(java.lang.Object,
     *      java.lang.Object)
     */
    public void putObject(Object key, Object value) {
        objectMap.put(key, value);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#getObject(java.lang.Object)
     */
    public Object getObject(Object key) {
        return objectMap.get(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#removeObject(java.lang.Object)
     */
    public Object removeObject(Object key) {
        return objectMap.remove(key);
    }

    /**
     * @see mpi.eudico.server.timeseries.TSConfiguration#objectKeys()
     */
    public Set objectKeySet() {
        return objectMap.keySet();
    }
}
